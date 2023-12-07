package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.RocketConstant;
import com.bright.stats.mq.RocketProduceService;
import com.bright.stats.pojo.dto.*;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.MqMessage;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.ClassUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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
@Validated
@RequiredArgsConstructor
public class BaseDataController {

    private final RocketProduceService rocketProduceService;
    private final BaseDataService baseDataService;

    private static final List<String> ALLOWED_FILE_TYPES = Arrays.asList("xls", "xlsx");

    @ApiOperation(value = "获取基础数据表内公式")
    @GetMapping("/ruleInner/list")
    public Result listRuleInners(@NotBlank(message = "表名不能为空") String tableName
            , @NotNull(message = "年份不能为空") Integer years
            , @NotNull(message = "月份不能为空") Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        List<RuleInnerVO> ruleInnerVOS = baseDataService.listRuleInners(typeCode, tableName, years, months, userDistNo);
        return Result.success(ruleInnerVOS);
    }

    @ApiOperation(value = "获取基础数据表头")
    @GetMapping("/tableHeader/list")
    public Result listTableHeaders(@NotBlank(message = "表名不能为空") String tableName
            , @NotNull(message = "年份不能为空") Integer years
            , @NotNull(message = "月份不能为空") Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        List<TableHeader> tableHeaders = baseDataService.listTableHeaders(typeCode, tableName, years, months, userDistNo);
        return Result.success(tableHeaders);
    }

    @ApiOperation(value = "分页获取基础表数据")
    @PostMapping("/tableData/page")
    public Result listTableDataForPage(@RequestBody @Validated BaseDataQuery baseDataQuery) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        baseDataQuery.setTypeCode(typeCode);
        baseDataQuery.setUserDistNo(loginUser.getTjDistNo());
        PageResult<Map<String, Object>> mapPageResult = baseDataService.listTableDataForPage(baseDataQuery);
        return Result.success(mapPageResult);
    }

    @PreAuthorize("hasAnyAuthority('baseData:maintenanceData')")
    @ApiOperation(value = "保存基础表数据")
    @PostMapping("/tableData/save")
    public Result saveTableData(@Validated @RequestBody TableDataDTO tableDataDTO) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        tableDataDTO.setTypeCode(typeCode);
        tableDataDTO.setUsername(loginUser.getUsername());

        try{
            MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();
            mqMessagesDTO.setUsername(tableDataDTO.getUsername());
            mqMessagesDTO.setDistNo(tableDataDTO.getDistNo());
            mqMessagesDTO.setTypeCode(tableDataDTO.getTypeCode());
            mqMessagesDTO.setYears(tableDataDTO.getYears());
            mqMessagesDTO.setMonths(tableDataDTO.getMonths());


            //检查正在运行或待运行的任务
            baseDataService.checkRuningAll(mqMessagesDTO);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }

        try{
            baseDataService.saveTableData(tableDataDTO);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("保存失败！");
        }


        return Result.success();
    }

    @PreAuthorize("hasAnyAuthority('baseData:menusum')")
    @ApiOperation(value = "汇总基础数据")
    @PostMapping("/summary")
    public Result summary(@RequestBody SummaryDTO summaryDTO) {
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        summaryDTO.setTypeCode(tableType.getTableType());
        summaryDTO.setUserDistNo(SecurityUtil.getLoginUser().getTjDistNo());

        MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();
        mqMessagesDTO.setTopicType(RocketConstant.TOPIC_SUMMARY);
        mqMessagesDTO.setYears(summaryDTO.getYears());
        mqMessagesDTO.setMonths(summaryDTO.getMonths());
        mqMessagesDTO.setDistNo(summaryDTO.getDistNo());
        mqMessagesDTO.setTypeCode(tableType.getTableType());
        mqMessagesDTO.setUsername(SecurityUtil.getLoginUser().getUsername());

//        Boolean checkFlag = baseDataService.checkRunningByDist(mqMessagesDTO);
//        if(!checkFlag){
//            return Result.fail("当前有汇总操作在执行中，请等执行完成后再进行操作。详情请查看任务列表。");
//        }

        Boolean checkFlag = baseDataService.checkRunning(mqMessagesDTO);
        if(!checkFlag){
            return Result.fail("当前有汇总操作在执行中，请等执行完成后再进行操作。详情请查看任务列表。");
        }

        MqMessage<SummaryDTO> mqMessage = new MqMessage();
        mqMessage.setKeyword(SecurityUtil.getLoginUser().getUsername() + "_" + UUID.randomUUID().toString());
        mqMessage.setTopicType(RocketConstant.TOPIC_SUMMARY);
        mqMessage.setUsername(SecurityUtil.getLoginUser().getUsername());
        mqMessage.setYears(summaryDTO.getYears());
        mqMessage.setMonths(summaryDTO.getMonths());
        mqMessage.setDistNo(summaryDTO.getDistNo());
        mqMessage.setReadFlag(false);
        mqMessage.setTypeCode(tableType.getTableType());
        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_TODO);
        mqMessage.setData(summaryDTO);
        rocketProduceService.sendMessage(mqMessage);

//        SummaryVO summaryVO = baseDataService.summary(summaryDTO);
        SummaryVO summaryVO = new SummaryVO();

        return Result.success(summaryVO);
    }


    @PreAuthorize("hasAnyAuthority('baseData:btncheck')")
    @ApiOperation(value = "稽核基础数据")
    @PostMapping("/check")
    public Result check(@RequestBody CheckDTO checkDTO) {
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        checkDTO.setTypeCode(tableType.getTableType());
        checkDTO.setUserDistNo(SecurityUtil.getLoginUser().getTjDistNo());

        checkDTO.setTableType(tableType);

        MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();
        mqMessagesDTO.setTopicType(RocketConstant.TOPIC_CHECK);
        mqMessagesDTO.setYears(checkDTO.getYears());
        mqMessagesDTO.setMonths(checkDTO.getMonths());
        mqMessagesDTO.setDistNo(checkDTO.getDistNo());
        mqMessagesDTO.setTypeCode(tableType.getTableType());
        mqMessagesDTO.setUsername(SecurityUtil.getLoginUser().getUsername());

//        Boolean checkFlag = baseDataService.checkRunningByDist(mqMessagesDTO);
        Boolean checkFlag = baseDataService.checkRunning(mqMessagesDTO);

        if(!checkFlag){
            List<CheckVO> checkVOS = new ArrayList<>();
            return Result.fail("当前有稽核操作在执行中，请等执行完成后再进行操作。详情请查看任务列表。");
        }

        MqMessage<CheckDTO> mqMessage = new MqMessage();
        mqMessage.setKeyword(SecurityUtil.getLoginUser().getUsername() + "_" + UUID.randomUUID().toString());

        mqMessage.setTopicType(RocketConstant.TOPIC_CHECK);
        mqMessage.setUsername(SecurityUtil.getLoginUser().getUsername());
        mqMessage.setYears(checkDTO.getYears());
        mqMessage.setMonths(checkDTO.getMonths());
        mqMessage.setDistNo(checkDTO.getDistNo());
        mqMessage.setReadFlag(false);
        mqMessage.setTypeCode(tableType.getTableType());
        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_TODO);
        mqMessage.setData(checkDTO);
        rocketProduceService.sendMessage(mqMessage);

//        rocketMQTemplate.convertAndSend(RocketConstant.TOPIC_CHECK, mqMessage);

//        List<CheckVO> checkVOS = baseDataService.check(checkDTO);
        List<CheckVO> checkVOS = new ArrayList<>();
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

    @PreAuthorize("hasAnyAuthority('baseData:btnsealData')")
    @ApiOperation(value = "上报或退回单位数据")
    @PostMapping("/reportOrWithdraw")
    public Result reportOrWithdraw(@RequestBody ReportDTO reportDTO) {
        User user = SecurityUtil.getLoginUser();

        reportDTO.setUser(user);

        MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();

        if("待办事项_上报".equals(reportDTO.getDpName())){
            mqMessagesDTO.setTopicType(RocketConstant.TOPIC_REPORT);
        }else{
            mqMessagesDTO.setTopicType(RocketConstant.TOPIC_WITHDRAW);
        }
        mqMessagesDTO.setYears(reportDTO.getYears());
        mqMessagesDTO.setMonths(reportDTO.getMonths());
        mqMessagesDTO.setDistNo(reportDTO.getKeyword().split("_")[1]);
        mqMessagesDTO.setTypeCode(user.getTableType().getTableType());
        mqMessagesDTO.setUsername(SecurityUtil.getLoginUser().getUsername());

//        Boolean checkFlag = baseDataService.checkRunningByDist(mqMessagesDTO);
//        if(!checkFlag){
//            return Result.fail("当前有上报或退回操作在执行中，请等执行完成后再进行操作。详情请查看任务列表。");
//        }

        Boolean checkFlag = baseDataService.checkRunning(mqMessagesDTO);
        if(!checkFlag){
            return Result.fail("当前有上报或退回操作在执行中，请等执行完成后再进行操作。详情请查看任务列表。");
        }

        MqMessage<ReportDTO> mqMessage = new MqMessage();
        mqMessage.setKeyword(SecurityUtil.getLoginUser().getUsername() + "_" + UUID.randomUUID().toString());

        if("待办事项_上报".equals(reportDTO.getDpName())){
            mqMessage.setTopicType(RocketConstant.TOPIC_REPORT);
        }else{
            mqMessage.setTopicType(RocketConstant.TOPIC_WITHDRAW);
        }

        mqMessage.setUsername(SecurityUtil.getLoginUser().getUsername());
        mqMessage.setYears(user.getTableType().getCurNewYear());
        mqMessage.setMonths(user.getTableType().getCurMonth());
        mqMessage.setTypeCode(user.getTableType().getTableType());
        mqMessage.setDistNo(reportDTO.getKeyword().split("_")[1]);
        mqMessage.setReadFlag(false);
        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_TODO);
        mqMessage.setData(reportDTO);
        rocketProduceService.sendMessage(mqMessage);
//        List<String> result = baseDataService.reportOrWithdraw(dpName, keyword, user);
        List<String> result = new ArrayList<>();
//        List<InteractiveVO> result = baseDataService.reportOrWithdraw(dpName, keyword, user);
        return Result.success(result);
    }

    @ApiOperation(value = "获取excel模板列表")
    @GetMapping("/excelTemplate/list")
    public Result listExcelTemplates(Integer years, String tableName, @RequestParam(name = "isSuperTemplate", defaultValue = "true") Boolean isSuperTemplate) {
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String username = loginUser.getUsername();
        String userDistNo = loginUser.getTjDistNo();
        if (!isSuperTemplate) {
            typeCode = tableName;
        }
        List<ExcelTemplateVO> excelTemplateVOS = baseDataService.listExcelTemplates(years, typeCode, username, userDistNo);
        return Result.success(excelTemplateVOS);
    }

    @PreAuthorize("hasAnyAuthority('baseData:menuExport')")
    @ApiOperation(value = "按excel模板导出数据")
    @PostMapping("/exportExcelByTemplate")
    public void exportExcelByTemplate(@RequestBody ExportExcelDTO exportExcelDTO, HttpServletResponse response) {
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        exportExcelDTO.setTypeCode(typeCode);
        exportExcelDTO.setUserDistNo(SecurityUtil.getLoginUser().getTjDistNo());
        ExportExcelVO exportExcelVO = baseDataService.exportExcelByTemplate(exportExcelDTO);

//        if(!CollectionUtils.isEmpty(exportExcelVO.getData())){
//            Map<String, Object> data = exportExcelVO.getData();
//            for (String temp : data.keySet()) {
//                List<Map<String,Object>> mapList = (List<Map<String, Object>>) data.get(temp);
//                for(Map<String, Object> tempMap : mapList){
//                    for (String keyTemp : tempMap.keySet()){
//                        if(Objects.isNull(tempMap.get(keyTemp))){
//                            tempMap.put(keyTemp, "");
//                        }
//                    }
//                }
//            }
//        }


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
            response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(exportExcelVO.getDistname() + "_" + exportExcelVO.getFileName(), "utf-8"));
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


    @PreAuthorize("hasAnyAuthority('baseData:menuImport')")
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
        User user = SecurityUtil.getLoginUser();

        // 获取上传文件的原始文件名
        String originalFilename = importExcelFile.getOriginalFilename();

        // 获取文件扩展名
        String fileExtension = null;
        if (originalFilename.lastIndexOf(".") != -1 && originalFilename.lastIndexOf(".") != 0) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        } else {
            fileExtension = "";
        }

        // 校验文件扩展名是否在允许上传的类型列表中
        if (!ALLOWED_FILE_TYPES.contains(fileExtension)) {
            return Result.fail("不允许上传该类型的文件");
        }

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

        //校验是否有真正执行或者待执行的任务
        MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();
        mqMessagesDTO.setUsername(user.getUsername());
        mqMessagesDTO.setDistNo(importExcelDTO.getDistNo());
        mqMessagesDTO.setTypeCode(tableType.getTableType());
        mqMessagesDTO.setYears(importExcelDTO.getYears());
        mqMessagesDTO.setMonths(importExcelDTO.getMonths());

        try {
            baseDataService.checkRuningAll(mqMessagesDTO);
        } catch (Exception e){
            return Result.fail(e.getMessage());
        }



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


    @ApiOperation(value = "获取基础表信息")
    @GetMapping("/baseTable/list")
    public Result listBaseTables(Integer years, Integer months){
        String typeCode = SecurityUtil.getLoginUser().getTableType().getTableType();
        List<FileList> baseTables = baseDataService.listBaseTables(typeCode, years, months);
        return Result.success(baseTables);
    }


    @ApiOperation(value = "获取地区所有级别")
    @GetMapping("/allDistGrade")
    public Result getDistAllGrade(){
        List<Integer> distAllGrade = baseDataService.getDistAllGrade();
        return Result.success(distAllGrade);
    }


    @PreAuthorize("hasAnyAuthority('baseData:initUploadData')")
    @ApiOperation(value = "同步上报数据")
    @PostMapping("/initUploadData")
    public Result initUploadData(@RequestBody InitUploadDataDTO initUploadDataDTO){
        Boolean res = baseDataService.initUploadData(initUploadDataDTO);
        return Result.success(res);
    }


    @ApiOperation(value = "获取数据上报情况")
    @GetMapping("/getReportSituation")
    public Result getReportSituation(String distNo, Integer years, Integer months, String tableType, HttpServletResponse response) {

        //设置允许跨域访问该接口
        //允许所有来源访问
        response.addHeader("Access-Control-Allow-Origin", "*");
        //允许访问的方式
        response.addHeader("Access-Control-Allow-Method", "POST,GET");

        Boolean reportSituation = baseDataService.getReportSituation(distNo, years, months, tableType);

        return Result.success(reportSituation);
    }
}
