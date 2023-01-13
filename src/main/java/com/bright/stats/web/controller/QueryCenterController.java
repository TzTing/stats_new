package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.dto.ExportExcelQueryCenterDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.QueryCenterQuery;
import com.bright.stats.service.QueryCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
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
        String userDistNo = SecurityUtil.getLoginUser().getTjDistNo();
        queryCenterQuery.setTypeCode(typeCode);
        queryCenterQuery.setOptType(optType);
        queryCenterQuery.setUserDistNo(userDistNo);
        PageResult<Map<String, Object>> mapPageResult = queryCenterService.listTableDataForPage(queryCenterQuery);
        return Result.success(mapPageResult);
    }


    @ApiOperation(value = "获取分析表信息")
    @GetMapping("/analysisTable/list")
    public Result listAnalysisTables(Integer years, Integer months){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        List<FileList> analysisTables = queryCenterService.listAnalysisTables(typeCode, years, months);
        return Result.success(analysisTables);
    }


    @PreAuthorize("hasAnyAuthority('querycenter:menuExport')")
    @ApiOperation(value = "导出数据查询中心数据")
    @PostMapping("/exportExcel")
    public void exportExcel(@RequestBody ExportExcelQueryCenterDTO exportExcelQueryCenterDTO, HttpServletResponse response){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        Integer optType = SecurityUtil.getLoginUser().getTableType().getOptType();
        String userDistNo = SecurityUtil.getLoginUser().getTjDistNo();
        exportExcelQueryCenterDTO.setUserDistNo(userDistNo);
        exportExcelQueryCenterDTO.setTypeCode(typeCode);
        exportExcelQueryCenterDTO.setOptType(optType);
        queryCenterService.exportExcel(exportExcelQueryCenterDTO, response);
    }


}
