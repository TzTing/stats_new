package com.bright.common.handler;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: Tz
 * @Date: 2022/10/13 15:22
 */
@Component
public class CasAuthenticationSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler implements AuthenticationSuccessHandler {

    public static List<Map<String, Object>> onlineUsers = new ArrayList<>();

    public CasAuthenticationSuccessHandler() {
    }

    public CasAuthenticationSuccessHandler(String defaultTargetUrl) {
        this.setDefaultTargetUrl(defaultTargetUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        this.handle(request, response, authentication);
        this.clearAuthenticationAttributes(request);
    }

    protected final void clearAuthenticationAttributes(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        }
    }


}
