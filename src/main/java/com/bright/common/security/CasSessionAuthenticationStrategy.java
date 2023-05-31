package com.bright.common.security;

import com.bright.common.cache.TimeExpiredPoolCache;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.po.second.User;
import lombok.SneakyThrows;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * @author: Tz
 * @Date: 2022/10/13 16:05
 */
public class CasSessionAuthenticationStrategy implements SessionAuthenticationStrategy {

    @Override
    public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {

        /*//获取存放在线用户的容器
        Map<String, User> onlineUsers = (Map<String, User>) request.getServletContext().getAttribute("onlineUsers");

        //如果为空 初始化容器
        if(CollectionUtils.isEmpty(onlineUsers)){
            onlineUsers = new HashMap<>(16);
        }
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User loginUser = securityUser.getUser();
        onlineUsers.put(loginUser.getUsername(), loginUser);
        request.getServletContext().setAttribute("onlineUsers", onlineUsers);*/

        /*Date untilDate = ((CasAuthenticationToken) authentication).getAssertion().getValidUntilDate();
        Date fromDate = ((CasAuthenticationToken) authentication).getAssertion().getValidFromDate();
        Long expiredTime = 1800000L;

        if(untilDate != null && fromDate != null){
            expiredTime = untilDate.getTime() - fromDate.getTime();
        }

        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User loginUser = securityUser.getUser();
        TimeExpiredPoolCache.getInstance().put(loginUser.getUsername(), loginUser, expiredTime);*/
    }
}
