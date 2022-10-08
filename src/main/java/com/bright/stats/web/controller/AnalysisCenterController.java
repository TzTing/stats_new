package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.vo.SqlInfoVO;
import com.bright.stats.service.AnalysisCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/4 11:38
 * @Description
 */
@Api(tags = "数据分析中心接口")
@RestController
@RequestMapping("/analysisCenter")
@RequiredArgsConstructor
public class AnalysisCenterController {

    private final AnalysisCenterService analysisCenterService;

    @ApiOperation(value = "获取分析方案列表")
    @GetMapping("/analysisScheme/list")
    public Result listAnalysisSchemes(Integer years){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        List<SqlInfoVO> sqlInfoVOS = analysisCenterService.listAnalysisSchemes(years, typeCode);
        return Result.success(sqlInfoVOS);
    }

    @ApiOperation(value = "获取分析中心表头")
    @GetMapping("/tableHeader/list")
    public Result listTableHeaders(Integer years, String sqlNo) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        List<TableHeader> tableHeaders = analysisCenterService.listTableHeaders(years, typeCode, sqlNo);
        return Result.success(tableHeaders);
    }

    @ApiOperation(value = "分页获取分析中心数据")
    @PostMapping("/tableData/page")
    public Result listTableDataForPage(@RequestBody AnalysisCenterQuery analysisCenterQuery) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        analysisCenterQuery.setTypeCode(typeCode);
        PageResult<Map<String, Object>> mapPageResult = analysisCenterService.listTableDataForPage(analysisCenterQuery);
        return Result.success(mapPageResult);
    }

    @ApiOperation(value = "导出分析中心数据")
    @PostMapping("/exportExcel")
    public void exportExcel(ExportExcelNoTemplateDTO exportExcelNoTemplateDTO, HttpServletResponse response){
        analysisCenterService.exportExcel(exportExcelNoTemplateDTO, response);
    }

}
