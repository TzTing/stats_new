package com.bright.common.security;

import com.bright.common.properties.CasProperties;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @Author txf
 * @Date 2022/6/24 15:54
 * @Description
 */
@Component
public class CasAuthenticationEntryPointImpl implements AuthenticationEntryPoint {

    private CasProperties casProperties;

    public CasAuthenticationEntryPointImpl(CasProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendRedirect(casProperties.getAppServerUrl()+"/send");
    }
}
