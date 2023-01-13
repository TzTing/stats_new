package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.ExportExcelQueryCenterDTO;
import com.bright.stats.pojo.dto.ExportExcelStatisticsCenterDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.StatisticsCenterQuery;
import com.bright.stats.service.StatisticsCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
        statisticsCenterQuery.setUserDistNo(loginUser.getTjDistNo());
        PageResult<Map<String, Object>> mapPageResult = statisticsCenterService.listTableDataForPage(statisticsCenterQuery);
        return Result.success(mapPageResult);
    }

    @ApiOperation(value = "获取统计表信息")
    @GetMapping("/statisticsTable/list")
    public Result listStatisticsTables(Integer years, Integer months){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        List<FileList> analysisTables = statisticsCenterService.listStatisticsTables(typeCode, years, months);
        return Result.success(analysisTables);
    }


    @PreAuthorize("hasAnyAuthority('statisticsCenter:menuExport')")
    @ApiOperation(value = "导出数据统计中心数据")
    @PostMapping("/exportExcel")
    public void exportExcel(@RequestBody ExportExcelStatisticsCenterDTO exportExcelStatisticsCenterDTO, HttpServletRequest request, HttpServletResponse response){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        Integer optType = SecurityUtil.getLoginUser().getTableType().getOptType();
        String userDistNo = SecurityUtil.getLoginUser().getTjDistNo();

//        CasAuthenticationToken cat = (CasAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
//        cat.getAssertion();

        exportExcelStatisticsCenterDTO.setUserDistNo(userDistNo);
        exportExcelStatisticsCenterDTO.setTypeCode(typeCode);
        exportExcelStatisticsCenterDTO.setOptType(optType);
        statisticsCenterService.exportExcel(exportExcelStatisticsCenterDTO, response);
    }
}
