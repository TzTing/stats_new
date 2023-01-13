package com.bright.stats.web.controller;

import com.bright.common.properties.CasProperties;
import com.bright.common.result.Result;
import com.bright.common.result.ResultEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author txf
 * @Date 2022/6/24 15:46
 * @Description
 */
@RestController
public class CasController {

    private CasProperties casProperties;

    @Autowired
    public CasController(CasProperties casProperties) {
        this.casProperties = casProperties;
    }

    @GetMapping("/send")
    public Result send() {
        String url = casProperties.getCasServerLoginUrl() + "?service="
                + casProperties.getAppServerUrl() + casProperties.getAppLoginUrl();
        return Result.fail(ResultEnum.USER_ERROR_0000, url);
    }

    @GetMapping("/login")
    public Result login() {
        return Result.success();
    }

    @GetMapping("/logout")
    public Result logout() {
        String url = casProperties.getCasServerLogoutUrl() + "?service="
                + casProperties.getAppHomeUrl();
        return Result.success(ResultEnum.SUCCESS, url);
    }
}
