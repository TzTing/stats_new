package com.bright.common.config;

import com.bright.common.handler.CasAuthenticationSuccessHandler;
import com.bright.common.handler.LogoutRemoveUserHandler;
import com.bright.common.properties.CasProperties;
import com.bright.common.security.CasAuthenticationEntryPointImpl;
import com.bright.common.security.CasSessionAuthenticationStrategy;
import com.bright.common.security.CasUserDetailsServiceImpl;
import com.bright.common.security.OnlineUserFilter;
import org.jasig.cas.client.session.SingleSignOutFilter;
import org.jasig.cas.client.validation.Cas20ServiceTicketValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.cas.ServiceProperties;
import org.springframework.security.cas.authentication.CasAuthenticationProvider;
import org.springframework.security.cas.web.CasAuthenticationFilter;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.core.GrantedAuthorityDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionFixationProtectionStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

    @Autowired
    public WebSecurityCasConfig(CasProperties casProperties, CasAuthenticationEntryPointImpl casAuthenticationEntryPoint, CasUserDetailsServiceImpl casUserDetailsService) {
        this.casProperties = casProperties;
        this.casAuthenticationEntryPoint = casAuthenticationEntryPoint;
        this.casUserDetailsService = casUserDetailsService;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests() //配置安全策略
                .antMatchers("/login", "/send").permitAll()
                .antMatchers("/static/**").permitAll()
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
    public CasAuthenticationProvider casAuthenticationProvider() {
        CasAuthenticationProvider casAuthenticationProvider = new CasAuthenticationProvider();
        casAuthenticationProvider.setServiceProperties(serviceProperties());
        casAuthenticationProvider.setTicketValidator(cas20ServiceTicketValidator());
        casAuthenticationProvider.setAuthenticationUserDetailsService(casUserDetailsService);
        casAuthenticationProvider.setKey("casAuthenticationProviderKey");
        return casAuthenticationProvider;
    }

    @Bean
    public Cas20ServiceTicketValidator cas20ServiceTicketValidator() {
        Cas20ServiceTicketValidator cas20ServiceTicketValidator = new Cas20ServiceTicketValidator(casProperties.getCasServerUrl());
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
