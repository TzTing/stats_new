package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.*;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.BaseDataQuery;
import com.bright.stats.pojo.query.ExistDataQuery;
import com.bright.stats.pojo.query.UploadBaseQuery;
import com.bright.stats.pojo.vo.*;
import com.bright.stats.service.BaseDataService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import net.sf.excelutils.ExcelException;
import net.sf.excelutils.ExcelUtils;
import net.sf.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @Author txf
 * @Date 2022/8/1 15:13
 * @Description
 */
@Api(tags = "基础数据处理接口")
@RestController
@RequestMapping("/baseData")
@RequiredArgsConstructor
public class BaseDataController {

    private final BaseDataService baseDataService;

    @ApiOperation(value = "获取基础数据表内公式")
    @GetMapping("/ruleInner/list")
    public Result listRuleInners(String tableName, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        List<RuleInnerVO> ruleInnerVOS = baseDataService.listRuleInners(typeCode, tableName, years, months, userDistNo);
        return Result.success(ruleInnerVOS);
    }

    @ApiOperation(value = "获取基础数据表头")
    @GetMapping("/tableHeader/list")
    public Result listTableHeaders(String tableName, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        List<TableHeader> tableHeaders = baseDataService.listTableHeaders(typeCode, tableName, years, months, userDistNo);
        return Result.success(tableHeaders);
    }

    @ApiOperation(value = "分页获取基础表数据")
    @PostMapping("/tableData/page")
    public Result listTableDataForPage(@RequestBody BaseDataQuery baseDataQuery) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        baseDataQuery.setTypeCode(typeCode);
        baseDataQuery.setUserDistNo(loginUser.getTjDistNo());
        PageResult<Map<String, Object>> mapPageResult = baseDataService.listTableDataForPage(baseDataQuery);
        return Result.success(mapPageResult);
    }

    @ApiOperation(value = "保存基础表数据")
    @PostMapping("/tableData/save")
    public Result saveTableData(@RequestBody TableDataDTO tableDataDTO) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        tableDataDTO.setTypeCode(typeCode);
        baseDataService.saveTableData(tableDataDTO);
        return Result.success();
    }

    @ApiOperation(value = "汇总基础数据")
    @PostMapping("/summary")
    public Result summary(@RequestBody SummaryDTO summaryDTO) {
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        summaryDTO.setTypeCode(tableType.getTableType());
        SummaryVO summaryVO = baseDataService.summary(summaryDTO);
        return Result.success(summaryVO);
    }

    @CrossOrigin
    @ApiOperation(value = "稽核基础数据")
    @PostMapping("/check")
    public Result check(@RequestBody CheckDTO checkDTO) {
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        checkDTO.setTypeCode(tableType.getTableType());
        List<CheckVO> checkVOS = baseDataService.check(checkDTO);
        return Result.success(checkVOS);
    }

    @ApiOperation(value = "分页获取上报单位数据")
    @PostMapping("/uploadBase/page")
    public Result listUploadBaseForPage(@RequestBody UploadBaseQuery uploadBaseQuery) {
        User user = SecurityUtil.getLoginUser();
        String typeCode = user.getTableType().getTableType();
        String userDistNo = user.getTjDistNo();
        uploadBaseQuery.setTypeCode(typeCode);
        uploadBaseQuery.setUserDistNo(userDistNo);
        PageResult<UploadBase> pageResult = baseDataService.listUploadBaseForPage(uploadBaseQuery);
        return Result.success(pageResult);
    }

    @ApiOperation(value = "上报或退回单位数据")
    @PostMapping("/reportOrWithdraw")
    public Result reportOrWithdraw(String dpName, String keyword) {
        User user = SecurityUtil.getLoginUser();
        List<String> result = baseDataService.reportOrWithdraw(dpName, keyword, user);
        return Result.success(result);
    }

    @ApiOperation(value = "获取excel模板列表")
    @GetMapping("/excelTemplate/list")
    public Result listExcelTemplates(Integer years, String tableName, @RequestParam(name = "isSuperTemplate", defaultValue = "true") Boolean isSuperTemplate) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String username = loginUser.getUsername();
        if (!isSuperTemplate) {
            typeCode = tableName;
        }
        List<ExcelTemplateVO> excelTemplateVOS = baseDataService.listExcelTemplates(years, typeCode, username);
        return Result.success(excelTemplateVOS);
    }

    @ApiOperation(value = "按excel模板导出数据")
    @PostMapping("/exportExcelByTemplate")
    public void exportExcelByTemplate(@RequestBody ExportExcelDTO exportExcelDTO, HttpServletResponse response) {
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        exportExcelDTO.setTypeCode(typeCode);
        ExportExcelVO exportExcelVO = baseDataService.exportExcelByTemplate(exportExcelDTO);
        JSONObject jsonObject = JSONObject.fromObject(exportExcelVO);
        for (Object o : jsonObject.keySet()) {
            ExcelUtils.addValue(o.toString(), jsonObject.get(o));
        }
        String config = "static/" + exportExcelVO.getExcelTemplatePath();
        String path = ClassUtils.getDefaultClassLoader().getResource(config).getPath();

//        response.setContentType("application/vnd.ms-excel");
//        response.setCharacterEncoding("utf-8");
//        response.setHeader("Content-Disposition","attachment; filename=\"" + exportExcelVO.getFileName());

        response.setCharacterEncoding("utf-8");
        try {
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(exportExcelVO.getFileName(), "utf-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        response.setContentType("application/vnd.ms-excel;charset=gb2312");

        try {

            ExcelUtils.export(path, response.getOutputStream());
        } catch (ExcelException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ApiOperation(value = "按excel模板和标签导出数据", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping("/exportExcelByTemplateAndTag")
    public void exportExcelByTemplateAndTag(@RequestBody ExportExcelTagDTO exportExcelTagDTO, HttpServletRequest request, HttpServletResponse response) {
        baseDataService.exportExcelByTemplateAndTag(exportExcelTagDTO, request, response);
    }

    @ApiOperation(value = "按excel模板导入数据")
    @PostMapping(value = "/importExcelByTemplate")
    public Result importExcelByTemplate(ImportExcelDTO importExcelDTO, MultipartFile importExcelFile) {
        Integer excelTemplateId = importExcelDTO.getExcelTemplateId();
        Integer years = importExcelDTO.getYears();
        Integer months = importExcelDTO.getMonths();
        String distNo = importExcelDTO.getDistNo();
        String distName = importExcelDTO.getDistName();
        String tableName = importExcelDTO.getTableName();
        String filePath = importExcelDTO.getFilePath();
        String rootPath = ClassUtils.getDefaultClassLoader().getResource("static/").getPath();
        TableType tableType = SecurityUtil.getLoginUser().getTableType();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd");
        String format = simpleDateFormat.format(new Date());
        System.out.println(format);

        String path = ClassUtils.getDefaultClassLoader().getResource("static/upload/excel/").getPath();
        File file = new File(path + format);
        if (!file.exists()) {
            file.mkdirs();
        }

        String oldFilename = importExcelFile.getOriginalFilename();
        String username = "超级用户";
        String uuid = UUID.randomUUID().toString();
        String newFilename = username + "_" + uuid + "_" + oldFilename;

        try {
            importExcelFile.transferTo(new File(file, newFilename));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        newFilename = file + "/" + newFilename;

        Integer successRows = 0;
        Integer errorRows = 0;
        List<ImportExcelVO> list = null;
        Map<String, Object> reInfo = baseDataService.importExcelByTemplate(tableType, excelTemplateId, rootPath, newFilename, distNo, years, months);
        if (null != reInfo) {
            list = (List<ImportExcelVO>) reInfo.get("importExcel");
            successRows = (Integer) reInfo.get("successRows");
            errorRows = (Integer) reInfo.get("errorRows");
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("list", list);
        map.put("successRows", successRows);
        map.put("errorRows", errorRows);
        return Result.success(map);
    }

    @ApiOperation(value = "获取类型和类型名称列表")
    @GetMapping(value = "/lxAndLxName/list")
    public Result listLxsAndLxNames(String tableName, String distNo, Integer years, Integer months){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        Map<String, Object> map = baseDataService.listLxsAndLxNames(typeCode, tableName, distNo, years, months);
        return Result.success(map);
    }

    @ApiOperation(value = "存在数据")
    @PostMapping("/existData/list")
    public Result listExistData(ExistDataQuery existDataQuery){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        existDataQuery.setTypeCode(typeCode);
        Map<String, Object> map = baseDataService.listExistData(existDataQuery);
        return Result.success(map);
    }

    @ApiOperation(value = "获取上期数据")
    @PostMapping("/previousYearData")
    public Result getPreviousYearData(String tableName, Integer years, Integer months, String paramJson){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        Map<String, Object> map = baseDataService.getPreviousYearData(typeCode, tableName, years, months, paramJson);
        return Result.success(map);
    }
}
