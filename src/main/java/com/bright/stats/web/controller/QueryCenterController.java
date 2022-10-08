package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.QueryCenterQuery;
import com.bright.stats.service.QueryCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/3 16:35
 * @Description
 */
@Api(tags = "数据查询中心接口")
@RestController
@RequestMapping("/queryCenter")
@RequiredArgsConstructor
public class QueryCenterController {

    private final QueryCenterService queryCenterService;

    @ApiOperation(value = "获取基础数据表头")
    @GetMapping("/tableHeader/list")
    public Result listTableHeaders(String tableName, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        List<TableHeader> tableHeaders = queryCenterService.listTableHeaders(typeCode, tableName, years, months, userDistNo);
        return Result.success(tableHeaders);
    }

    @ApiOperation(value = "分页获取基础表数据")
    @PostMapping("/tableData/page")
    public Result listTableDataForPage(@RequestBody QueryCenterQuery queryCenterQuery) {
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        String typeCode = tableType.getTableType();
        Integer optType = tableType.getOptType();
        queryCenterQuery.setTypeCode(typeCode);
        queryCenterQuery.setOptType(optType);
        PageResult<Map<String, Object>> mapPageResult = queryCenterService.listTableDataForPage(queryCenterQuery);
        return Result.success(mapPageResult);
    }

}
