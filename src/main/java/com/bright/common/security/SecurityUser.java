package com.bright.common.security;

import com.bright.stats.pojo.po.second.User;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;

/**
 * @Author txf
 * @Date 2022/6/24 17:32
 * @Description
 */
@Data
public class SecurityUser implements UserDetails {

    private Integer id;
    private String username;
    private String password;

    private User user;
    private Set<SecurityAuthority> securityAuthorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.securityAuthorities;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
