package com.bright.common.security;

import com.bright.stats.manager.DistManager;
import com.bright.stats.manager.UserManager;
import com.bright.stats.pojo.po.primary.RoleFunction;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.cas.authentication.CasAssertionAuthenticationToken;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author txf
 * @Date 2022/6/24 15:52
 * @Description
 */
@Component
@RequiredArgsConstructor
public class CasUserDetailsServiceImpl implements AuthenticationUserDetailsService<CasAssertionAuthenticationToken> {

    private final UserManager userManager;
    private final DistManager distManager;

    @Override
    public UserDetails loadUserDetails(CasAssertionAuthenticationToken token) throws UsernameNotFoundException {
        String loginName = token.getName();
        User user = userManager.getUserByUsername(loginName);

        user.setPassword(null);
        Set<SecurityAuthority> authorities = new HashSet<>();
        for (RoleFunction roleFunction : user.getRoleFunctions()) {
            if("navigate".equalsIgnoreCase(roleFunction.getNname())){
                continue;
            }
            SecurityAuthority securityAuthority = new SecurityAuthority();
            String authority =  roleFunction.getNname() + ":" + roleFunction.getNavNo();
            securityAuthority.setAuthority(authority);
            authorities.add(securityAuthority);
        }

        SecurityUser securityUser = new SecurityUser();
        securityUser.setId(user.getId());
        securityUser.setUsername(user.getUsername());
        securityUser.setPassword(user.getPassword());

        //刚登陆没有选择模式，年份就用当前事件的年份
        String allDistName = distManager.getDistFullName(user.getTjDistNo()
                , 2022);
//        String allDistName = distManager.getDistFullName(user.getTjDistNo()
//                , Integer.parseInt(DateUtil.getDate(new Date(), 1)));


        //设置用户地区名全称
        user.setAllDistName(allDistName);
        securityUser.setUser(user);
        securityUser.setSecurityAuthorities(authorities);
        return securityUser;
    }
}
