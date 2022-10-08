package com.bright.stats.web.controller;

import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.service.NavigateService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/1 10:37
 * @Description
 */
@Api(tags = "导航接口")
@RestController
@RequestMapping("/navigate")
@RequiredArgsConstructor
public class NavigateController {

    private final NavigateService navigateService;

    @GetMapping("/tableType/list")
    public Result listTableTypes(){
        List<TableType> tableTypes = navigateService.listTableTypes();
        return Result.success(tableTypes);
    }

    @GetMapping("/selectMode/{tableTypeId}")
    public Result selectMode(@PathVariable Integer tableTypeId){
        navigateService.selectMode(tableTypeId);
        return Result.success();
    }

    @GetMapping("/userInfo")
    public Result getUserInfo(){
        User loginUser = SecurityUtil.getLoginUser();
        loginUser.setPassword(null);
        return Result.success(loginUser);
    }
}
