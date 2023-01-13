package com.bright.common.security;

import com.bright.common.cache.TimeExpiredPoolCache;
import com.bright.common.properties.CasProperties;
import com.bright.stats.manager.DistManager;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.vo.OnlineUserVo;
import com.bright.stats.util.SessionUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author: Tz
 * @Date: 2022/11/05 10:28
 */
public class OnlineUserFilter extends OncePerRequestFilter {

    private CasProperties casProperties;

    public OnlineUserFilter(CasProperties casProperties) {
        this.casProperties = casProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {

        User user = null;
        filterChain.doFilter(httpServletRequest, httpServletResponse);

        if (SecurityContextHolder.getContext().getAuthentication() != null
                && SecurityContextHolder.getContext().getAuthentication().getPrincipal() != null) {

            if (!"anonymousUser".equalsIgnoreCase(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString())) {
                SecurityUser securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                user = securityUser.getUser();

                OnlineUserVo onlineUserVo = new OnlineUserVo();

                onlineUserVo.setUsername(user.getUsername());

                //地区名称全称
                onlineUserVo.setDistName(user.getAllDistName());
                onlineUserVo.setBrowser(SessionUnit.getOsAndBrowserInfo2(httpServletRequest));
                onlineUserVo.setLoginIp(SessionUnit.realIpAddr(httpServletRequest));
                onlineUserVo.setRoleName(user.getTjRoleName());
                onlineUserVo.setLoginTime(((CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getAssertion().getValidFromDate());

                TimeExpiredPoolCache.getInstance().put(user.getUsername(), onlineUserVo, 1800000L);
            }

        }

    }


}
