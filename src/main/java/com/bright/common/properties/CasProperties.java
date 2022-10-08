package com.bright.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author txf
 * @Date 2022/6/24 15:44
 * @Description
 */
@Getter
@Setter
@Component
public class CasProperties {

    @Value("${app.server.host.url}")
    private String appServerUrl;

    @Value("${app.login.url}")
    private String appLoginUrl;

    @Value("${app.logout.url}")
    private String appLogoutUrl;

    @Value("${app.home.url}")
    private String appHomeUrl;

    @Value("${cas.server.host}")
    private String casServerUrl;

    @Value("${cas.server.login_url}")
    private String casServerLoginUrl;

    @Value("${cas.server.logout_url}")
    private String casServerLogoutUrl;
}
