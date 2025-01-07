package com.bright.common.security;

import com.alibaba.fastjson.JSON;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.repository.second.UserRepository;
import com.bright.stats.service.NavigateService;
import com.bright.stats.util.RSA;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Example;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * <p> Project: stats - CustomTokenAuthenticationFilter </p>
 *
 * 使用token登陆的过滤器
 * @author Tz
 * @version 1.0.0
 * @date 2024/07/02 10:34
 * @since 1.0.0
 */
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private UserDetailsService userDetailsService;
    private UserRepository userRepository;
    private NavigateService navigateService;

    /**
     * 私钥
     */
    private String privateKey = "MIIBVAIBADANBgkqhkiG9w0BAQEFAASCAT4wggE6AgEAAkEAgt+UjQqJlVhKgL+7KlNoQ3RS9pWBTYWV7KlHVvMXzUxdMye+ceMWEgZeJ2VyP5O/mfk7VUly4YhqV7ouEOntoQIDAQABAkAQnM7fhGnogjea1jus6L+AaICreljafU2FqAs9N0hbL9n3S9qtVnajLQQhbxVwpk0SktPml3LFuHxWDgO2eiVBAiEA3w8+domLKKG0U3k8hDiHPfIxZ1TDGxHhGM4ITCrespkCIQCWMz3gYa1C98NG09mhv+jpJguDlFjhVIXtmf5kHVEASQIhAN1g0Lpuidkam9CYq3ICdF8LhvKp0jWEiiXHHb8ScRFJAiBOLd3HLRBCCzRZaWueBYX11RepJU4d+yC6y6zd72R6AQIgJaNJno7D4Es+YF0OUdLo7kWE0ozaNg3v22Niv3ofEdA=";


    public TokenAuthenticationFilter(@Qualifier("tokenUserDetailsServiceImpl") UserDetailsService userDetailsService, UserRepository userRepository, NavigateService navigateService) {
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.navigateService = navigateService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            //从请求头获取年报token登陆方式的值
            String token = request.getHeader("nb-token");
            if (token != null) {
                // 解析年报token获取用户名密码
                Map<String, Object> stringObjectMap = parseToken(token);

                //如果未解析出内容
                if (stringObjectMap == null) {
                    throw new RuntimeException("[nb-token]方式登陆, 解析用户密码错误!");
                }

                Assert.notNull(stringObjectMap.get("username"), "用户名必须传递!");
                Assert.notNull(stringObjectMap.get("password"), "密码必须传递!");
                String username = stringObjectMap.get("username").toString();
                String password = stringObjectMap.get("password").toString();

                //如果用户名密码为空
                if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
                    throw new RuntimeException("[nb-token]方式登陆, 用户名或密码为空!");
                }

                //校验用户密码 不存在跳过
                if (!isUserExists(username, password)) {
                    throw new RuntimeException("[nb-token]方式登陆, 未查询到用户, 用户名或密码错误!");
                }



                Assert.notNull(stringObjectMap.get("tableTypeId"), "模式主键必须传递!");
                Assert.notNull(stringObjectMap.get("years"), "年份必须传递!");
                int tableTypeId = Integer.parseInt(stringObjectMap.get("tableTypeId").toString());
                int years = Integer.parseInt(stringObjectMap.get("years").toString());

                //如果模式主键或月份不在有效范围内
                if (tableTypeId > 0 || years > 0) {
                    throw new RuntimeException("[nb-token]方式登陆, 模式主键或月份不在有效范围内!");
                }


                Authentication authentication = createAuthentication(username);
                SecurityContextHolder.getContext().setAuthentication(authentication);

                //选择模式
                navigateService.selectMode(Integer.valueOf(stringObjectMap.get("tableTypeId").toString())
                        , Integer.valueOf(stringObjectMap.get("years").toString()));
            }
        } catch (Exception e) {
            logger.error("使用[nb-token]的方式识别失败，跳过！ 详细错误:[{}]", e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 解析token获取登陆用户名
     * @param nbToken 年报token登陆的值
     * @return        返回冲token中解析的用户名和密码
     */
    private Map<String, Object> parseToken(String nbToken) {

        try {
            //解密得到用户名和密码的json串
            String userInfo = RSA.decryptByPrivate(nbToken, privateKey);
            return JSON.parseObject(userInfo, Map.class);
        } catch (Exception e){
            return null;
        }
    }

    /**
     * 通过用户名密码查询用户是否存在
     * @param username 用户名
     * @param password 密码
     * @return         是否存在，true：存在， false：不存在
     */
    private boolean isUserExists(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(password);
        Example<User> userExample = Example.of(user);
        return userRepository.findOne(userExample).isPresent();
    }


//    /**
//     * 选择的模式是否正确
//     * @param tableTypeId 模式主键
//     * @param years       月份
//     * @return            是否选择正确，true：正确， false：不正确
//     */
//    private boolean isSelectModelCorrect(String tableTypeId, String years) {
//
//
//
//    }

    /**
     * 通过登陆的用户名，创建授权对象
     * @param username 登陆用户名
     * @return         登陆用户名对应的授权对象
     */
    private Authentication createAuthentication(String username) {
        // 创建Authentication对象
        UserDetails user = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }


}
