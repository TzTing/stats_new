package com.bright.common.handler;

import com.bright.common.cache.TimeExpiredPoolCache;
import com.bright.common.security.SecurityUser;
import com.bright.stats.pojo.po.second.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author: Tz
 * @Date: 2022/11/08 10:03
 */
public class LogoutRemoveUserHandler implements LogoutHandler {

    /**
     * Causes a logout to be completed. The method must complete successfully.
     *
     * @param request        the HTTP request
     * @param response       the HTTP response
     * @param authentication the current principal details
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {

        try{
            if (authentication != null && authentication.getPrincipal() != null) {
                if (!"anonymousUser".equalsIgnoreCase(authentication.getPrincipal().toString())) {
                    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
                    User user = securityUser.getUser();

                    //退出删除该在线用户
                    TimeExpiredPoolCache.getInstance().remove(user.getUsername());
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
