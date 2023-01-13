package com.bright.stats.web.controller;

import com.bright.common.result.Result;
import com.bright.stats.pojo.vo.DistVO;
import com.bright.stats.service.DistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/2 11:31
 * @Description
 */
@Api(tags = "地区接口")
@RestController
@Validated
@RequestMapping("/dist")
@RequiredArgsConstructor
public class DistController {

    private final DistService distService;

    @ApiOperation(value = "获取地区树数据")
    @GetMapping("/distTree/list")
    public Result listDistTrees(@NotNull(message = "年份不能为空") Integer years
            , @NotBlank(message = "地区编号不能为空") String distNo){
        String userDistNo = "";
        List<DistVO> distVOS = distService.listDistTrees(years, userDistNo, distNo);
        return Result.success(distVOS);
    }
}
