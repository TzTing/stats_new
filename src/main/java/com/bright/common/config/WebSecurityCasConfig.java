package com.bright.common.config;

import com.bright.common.handler.CasAuthenticationSuccessHandler;
import com.bright.common.handler.LogoutRemoveUserHandler;
import com.bright.common.properties.CasProperties;
import com.bright.common.security.*;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

/**
 * @Author txf
 * @Date 2022/7/27 10:56
 * @Description
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableWebSecurity
public class WebSecurityCasConfig extends WebSecurityConfigurerAdapter {

    private CasProperties casProperties;
    private CasAuthenticationEntryPointImpl casAuthenticationEntryPoint;
    private CasUserDetailsServiceImpl casUserDetailsService;


    private TokenAuthenticationFilter tokenAuthenticationFilter;

    @Autowired
    public WebSecurityCasConfig(CasProperties casProperties, CasAuthenticationEntryPointImpl casAuthenticationEntryPoint, CasUserDetailsServiceImpl casUserDetailsService, TokenAuthenticationFilter tokenAuthenticationFilter) {
        this.casProperties = casProperties;
        this.casAuthenticationEntryPoint = casAuthenticationEntryPoint;
        this.casUserDetailsService = casUserDetailsService;
        this.tokenAuthenticationFilter = tokenAuthenticationFilter;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {


        http// 添加自定义的基于token的登录过滤器
                .addFilterBefore(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                //配置安全策略
                .authorizeRequests()
                .antMatchers("/login", "/send").permitAll()
                .antMatchers("/static/**").permitAll()
                .antMatchers("/baseData/getReportSituation").permitAll()
//                .antMatchers("/**").permitAll()
                .anyRequest().authenticated() //所有请求都要验证
                .and()
                .csrf().disable()
                .logout().permitAll() //logout不需要验证
                .and()
                .headers().frameOptions().disable()
                .and()
                .cors()
                .and().formLogin(); //使用form表单登录

        http.exceptionHandling().authenticationEntryPoint(casAuthenticationEntryPoint)
                .and()
                .addFilter(casAuthenticationFilter())
                .addFilterAfter(onlineUserFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(casLogoutFilter(), LogoutFilter.class)
                .addFilterBefore(singleSignOutFilter(), CasAuthenticationFilter.class);



        //退出登录
        /*http.logout().logoutUrl("logout.do")
                .logoutSuccessUrl("/login")
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout.do", "GET"))
                .logoutSuccessHandler(new LogoutSuccessHandler() {
                    @Override
                    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                        // System.out.println("onLogoutSuccess");
                        response.sendRedirect("logout.html");
                    }
                })
                .addLogoutHandler(new LogoutHandler() {
                    @Override
                    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
                        System.out.println("logout");
                    }
                })
                .invalidateHttpSession(true)
                .deleteCookies("token_token");*/
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        super.configure(auth);
        auth.authenticationProvider(casAuthenticationProvider());
    }

    @Bean
    public ServiceProperties serviceProperties() {
        ServiceProperties serviceProperties = new ServiceProperties();
        serviceProperties.setService(casProperties.getAppServerUrl() + casProperties.getAppLoginUrl());
        serviceProperties.setSendRenew(false);
        serviceProperties.setAuthenticateAllArtifacts(true);
        return serviceProperties;
    }

    @Bean
    public CasAuthenticationFilter casAuthenticationFilter() throws Exception {
        CasAuthenticationFilter casAuthenticationFilter = new CasAuthenticationFilter();
        casAuthenticationFilter.setServiceProperties(serviceProperties());
        casAuthenticationFilter.setFilterProcessesUrl(casProperties.getAppLoginUrl());
        casAuthenticationFilter.setAuthenticationManager(authenticationManager());
        casAuthenticationFilter.setAuthenticationSuccessHandler(new CasAuthenticationSuccessHandler(casProperties.getAppHomeUrl()));
        casAuthenticationFilter.setSessionAuthenticationStrategy(sessionAuthenticationStrategy());
        return casAuthenticationFilter;
    }

    @Bean
    public CasAuthenticationProviderExt casAuthenticationProvider() {
        CasAuthenticationProviderExt casAuthenticationProvider = new CasAuthenticationProviderExt();
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setAuthenticationUserDetailsService(casUserDetailsService);
        casAuthenticationProvider.setKey("casAuthenticationProviderKey");
        return casAuthenticationProvider;
    }

    @Bean
    public Cas20ProxyTicketValidatorExt cas20ServiceTicketValidator() {
        Cas20ProxyTicketValidatorExt cas20ServiceTicketValidator = new Cas20ProxyTicketValidatorExt(casProperties.getCasServerUrl());
        //内网登陆
//        Cas20ProxyTicketValidatorExt cas20ServiceTicketValidator = new Cas20ProxyTicketValidatorExt(casProperties.getCasServerInnerHost());
        cas20ServiceTicketValidator.setEncoding("UTF-8");
        return cas20ServiceTicketValidator;
    }

    @Bean
    public SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        CasSessionAuthenticationStrategy sessionAuthenticationStrategy = new CasSessionAuthenticationStrategy();
        return sessionAuthenticationStrategy;
    }

    @Bean
    public SingleSignOutFilter singleSignOutFilter() {
        SingleSignOutFilter singleSignOutFilter = new SingleSignOutFilter();
        singleSignOutFilter.setIgnoreInitConfiguration(true);
        return singleSignOutFilter;
    }

    @Bean
    public LogoutFilter casLogoutFilter() {
        LogoutFilter logoutFilter = new LogoutFilter(casProperties.getCasServerLogoutUrl() + "?service=" + casProperties.getAppHomeUrl(),
                new SecurityContextLogoutHandler(), logoutRemoveUserHandler());
        logoutFilter.setFilterProcessesUrl(casProperties.getAppLogoutUrl());
        return logoutFilter;
    }

    @Bean
    public OnlineUserFilter onlineUserFilter() {
        OnlineUserFilter onlineUserFilter = new OnlineUserFilter(casProperties);
        return onlineUserFilter;
    }

    @Bean
    public LogoutRemoveUserHandler logoutRemoveUserHandler() {
        LogoutRemoveUserHandler logoutRemoveUserHandler = new LogoutRemoveUserHandler();
        return logoutRemoveUserHandler;
    }

    @Bean
    public GrantedAuthorityDefaults grantedAuthorityDefaults() {
        return new GrantedAuthorityDefaults("");
    }
}
