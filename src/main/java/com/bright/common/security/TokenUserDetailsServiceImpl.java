package com.bright.common.security;

import com.bright.stats.manager.DistManager;
import com.bright.stats.manager.UserManager;
import com.bright.stats.pojo.po.primary.RoleFunction;
import com.bright.stats.pojo.po.second.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * <p> Project: stats - TokenUserDetailsService </p>
 *
 * token的认证方式
 * @author Tz
 * @version 1.0.0
 * @date 2024/07/02 11:33
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class TokenUserDetailsServiceImpl implements UserDetailsService {


    private final UserManager userManager;
    private final DistManager distManager;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        User user = userManager.getUserByUsername(username);

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


        //设置用户地区名全称
        user.setAllDistName(allDistName);
        securityUser.setUser(user);
        securityUser.setSecurityAuthorities(authorities);
        return securityUser;
    }
}
