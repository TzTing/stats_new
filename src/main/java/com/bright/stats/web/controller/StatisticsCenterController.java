package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.StatisticsCenterQuery;
import com.bright.stats.service.StatisticsCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/4 11:07
 * @Description
 */
@Api(tags = "数据统计中心接口")
@RestController
@RequestMapping("/statisticsCenter")
@RequiredArgsConstructor
public class StatisticsCenterController {

    private final StatisticsCenterService statisticsCenterService;

    @ApiOperation(value = "获取基础数据表头")
    @GetMapping("/tableHeader/list")
    public Result listTableHeaders(String tableName, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        List<TableHeader> tableHeaders = statisticsCenterService.listTableHeaders(typeCode, tableName, years, months);
        return Result.success(tableHeaders);
    }

    @ApiOperation(value = "分页获取基础表数据")
    @PostMapping("/tableData/page")
    public Result listTableDataForPage(@RequestBody StatisticsCenterQuery statisticsCenterQuery) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        statisticsCenterQuery.setTypeCode(typeCode);
        PageResult<Map<String, Object>> mapPageResult = statisticsCenterService.listTableDataForPage(statisticsCenterQuery);
        return Result.success(mapPageResult);
    }
}
