package com.bright.common.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

/**
 * @Author txf
 * @Date 2022/6/24 17:46
 * @Description
 */
@Data
public class SecurityAuthority implements GrantedAuthority {

    private String authority;

    @Override
    public String getAuthority() {
        return this.authority;
    }
}
