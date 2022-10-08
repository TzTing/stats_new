package com.bright.stats.service.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.AssertUtil;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.*;
import com.bright.stats.pojo.dto.*;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.BaseDataQuery;
import com.bright.stats.pojo.query.ExistDataQuery;
import com.bright.stats.pojo.query.UploadBaseQuery;
import com.bright.stats.pojo.vo.*;
import com.bright.stats.service.BaseDataService;
import com.bright.stats.util.DataConstants;
import com.bright.stats.util.ExcelUtilPOI;
import com.bright.stats.web.websocket.WebSocket;
import lombok.RequiredArgsConstructor;
import net.sf.excelutils.ExcelException;
import net.sf.json.JSONObject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 15:14
 * @Description
 */
@Service
@RequiredArgsConstructor
public class BaseDataServiceImpl implements BaseDataService {

    private final WebSocket webSocket;
    private final JdbcTemplate jdbcTemplatePrimary;
    private final BaseDataManager baseDataManager;
    private final FileListManager fileListManager;
    private final UploadBaseManager uploadBaseManager;
    private final DataProcessNewManager dataProcessNewManager;
    private final ExcelTemplateManager excelTemplateManager;

    @Override
    public List<RuleInnerVO> listRuleInners(String typeCode, String tableName, Integer years, Integer months, String userDistNo) {
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        List<RuleInner> ruleInners = fileList.getRuleInners();
        List<RuleInnerVO> ruleInnerVOS = new ArrayList<>();
        for (RuleInner ruleInner : ruleInners) {
            RuleInnerVO ruleInnerVO = new RuleInnerVO();
            ruleInnerVO.setFieldName(ruleInner.getFieldName());
            ruleInnerVO.setExpress(ruleInner.getExpress());
            ruleInnerVO.setDetail(ruleInner.getDetail());
            ruleInnerVO.setOpt(ruleInner.getOpt());
            ruleInnerVOS.add(ruleInnerVO);
        }
        return ruleInnerVOS;
    }

    @Override
    public List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months, String userDistNo) {
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        return fileList.getTableHeaders();
    }

    @Override
    public PageResult<Map<String, Object>> listTableDataForPage(BaseDataQuery baseDataQuery) {
        PageResult<Map<String, Object>> mapPageResult = baseDataManager.listTableData(baseDataQuery, true, PageResult.class);
        return mapPageResult;
    }

    @Override
    public void saveTableData(TableDataDTO tableDataDTO) {
        baseDataManager.saveTableData(tableDataDTO);
    }

    @Override
    public SummaryVO summary(SummaryDTO summaryDTO) {
        SummaryVO summaryVO = baseDataManager.summary(summaryDTO);
        return summaryVO;
    }

    @Override
    public List<CheckVO> check(CheckDTO checkDTO) {
        Integer years = checkDTO.getYears();
        Integer months = checkDTO.getMonths();
        String distNo = checkDTO.getDistNo();
        String tableName = checkDTO.getTableName();
        String typeCode = checkDTO.getTypeCode();
        Object[] ids = checkDTO.getIds();
        Integer grade = checkDTO.getGrade();
        Boolean isAllDist = checkDTO.getIsAllDist();
        Boolean isSb = checkDTO.getIsSb();
        Boolean isGrade = checkDTO.getIsGrade();
        TableType tableType = SecurityUtil.getLoginUser().getTableType();
        String userDistNo = checkDTO.getUserDistNo();


        List<FileList> fileLists = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
        List<CheckVO> checkVOS = new ArrayList<>();

//        BigDecimal fileListSize = BigDecimal.valueOf(fileLists.size());
//        int size = fileLists.size();
        for (FileList fileList : fileLists) {
            List<CheckVO> result = baseDataManager.check(tableType, fileList, ids, years, months, distNo, grade, isAllDist, isSb, false);
            checkVOS.addAll(result);
//            size--;
//            BigDecimal bigDecimalSize = BigDecimal.valueOf(size);
//            BigDecimal i = fileListSize.subtract(bigDecimalSize);
//            BigDecimal schedule = i.divide(fileListSize, 2, BigDecimal.ROUND_CEILING).multiply(BigDecimal.valueOf(100));
//            webSocket.sendMessage(schedule + "");
        }
        return checkVOS;
    }

    @Override
    public PageResult<UploadBase> listUploadBaseForPage(UploadBaseQuery uploadBaseQuery) {
        return uploadBaseManager.listUploadBaseForPage(uploadBaseQuery);
    }

    @Override
    public List<String> reportOrWithdraw(String dpName, String keyword, User user) {
        String tableType = user.getTableType().getTableType();
        String userDistNo = user.getDistNo();
        String username = user.getUsername();
        List<DataProcessNew> dataProcessNews = dataProcessNewManager.listDataProcessNews(dpName, tableType, -1);
        for (DataProcessNew dataProcessNew : dataProcessNews) {
            if (dataProcessNew.getAlertType() == 1 || dataProcessNew.getAlertType() == 0) {
                String processSql = dataProcessNew.getProcessSql();
                String alertSql = dataProcessNew.getAlert();

                processSql = processSql.replace("${keyword}", keyword);
                processSql = processSql.replace("${_udistNo}", userDistNo);
                processSql = processSql.replace("${writer}", username);

                List<Map<String, Object>> maps = null;
                if (dataProcessNew.getIsAlert()) {
                    maps = jdbcTemplatePrimary.queryForList(processSql);

                    if (!CollectionUtils.isEmpty(maps)) {

                        alertSql = alertSql.replace("${keyword}", keyword);
                        alertSql = alertSql.replace("${_udistNo}", userDistNo);
                        alertSql = alertSql.replace("${writer}", username);

                        List<String> strings = jdbcTemplatePrimary.queryForList(alertSql, String.class);
                        if (!CollectionUtils.isEmpty(strings)) {
                            System.out.println(strings.get(0));
                            return strings;
                        }
                    }
                } else {
                    jdbcTemplatePrimary.execute(processSql);
                }
            } else {
                continue;
            }
        }
        return null;
    }

    @Override
    public List<ExcelTemplateVO> listExcelTemplates(Integer years, String typeCode, String username) {
        List<ExcelTemplate> excelTemplates = excelTemplateManager.listExcelTemplates(years, typeCode, username, FileListConstant.FILE_LIST_TABLE_TYPE_BASE);
        List<ExcelTemplateVO> excelTemplateVOS = new ArrayList<>();
        for (ExcelTemplate excelTemplate : excelTemplates) {
            ExcelTemplateVO excelTemplateVO = new ExcelTemplateVO();
            excelTemplateVO.setId(excelTemplate.getId());
            excelTemplateVO.setYears(excelTemplate.getYears());
            excelTemplateVO.setShortDis(excelTemplate.getShortDis());
            excelTemplateVO.setType(excelTemplate.getType());
            excelTemplateVO.setExcelType(excelTemplate.getExcelType());
            excelTemplateVO.setFileName(excelTemplate.getFileName());
            excelTemplateVO.setWriter(excelTemplate.getWriter());
            excelTemplateVO.setWriteDate(excelTemplate.getWriteDate());
            excelTemplateVOS.add(excelTemplateVO);
        }
        return excelTemplateVOS;
    }

    @Override
    public ExportExcelVO exportExcelByTemplate(ExportExcelDTO exportExcelDTO) {
        Integer excelTemplateId = exportExcelDTO.getExcelTemplateId();
        Integer years = exportExcelDTO.getYears();
        Integer months = exportExcelDTO.getMonths();
        String distName = exportExcelDTO.getDistName();
        String tableName = exportExcelDTO.getTableName();
        String typeCode = exportExcelDTO.getTypeCode();
        String lx = exportExcelDTO.getLx();
        String lxName = exportExcelDTO.getLxName();
        Integer grade = exportExcelDTO.getGrade();
        Boolean isBalanced = exportExcelDTO.getIsBalanced();
        String distNo = exportExcelDTO.getDistNo();
        List<String> sorts = exportExcelDTO.getSorts();
        List<Condition> conditions = exportExcelDTO.getConditions();
        String userDistNo = exportExcelDTO.getUserDistNo();


        ExcelTemplate excelTemplate = excelTemplateManager.getExcelTemplateById(excelTemplateId);
        AssertUtil.notNull(excelTemplate, "模板未指定或者模板路径不正确！");
        Map<String, Object> data = new HashMap<>(16);
        if (excelTemplate.getJxMode() == 2) {
            throw new RuntimeException("不支持用户自定义excel导出！");
        }
        BaseDataQuery baseDataQuery = new BaseDataQuery();
        baseDataQuery.setYears(years);
        baseDataQuery.setMonths(months);
        baseDataQuery.setTableName(tableName);
        baseDataQuery.setLx(lx);
        baseDataQuery.setLxName(lxName);
        baseDataQuery.setGrade(grade);
        baseDataQuery.setIsBalanced(isBalanced);
        baseDataQuery.setDistNo(distNo);
        baseDataQuery.setTypeCode(typeCode);
        baseDataQuery.setConditions(conditions);
        baseDataQuery.setSorts(sorts);
        if (!"多表一个Excel".equals(excelTemplate.getExcelType())) {
            List<Map<String, Object>> list = baseDataManager.listTableData(baseDataQuery, false, List.class);
            data.put(tableName.toLowerCase(), list);
        } else {
            List<FileList> fileLists = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
            for (FileList fileList : fileLists) {
                baseDataQuery.setTableName(fileList.getTableName());
                List<Map<String, Object>> list = baseDataManager.listTableData(baseDataQuery, false, List.class);
                if (excelTemplate.getTemplateShape() == 2) {
                    data.put(fileList.getTableName().toLowerCase(), list);
                } else {
                    throw new RuntimeException("不支持用户自定义excel导出！");
                }

            }
        }
        ExportExcelVO exportExcelVO = new ExportExcelVO();
        exportExcelVO.setYears(years);
        exportExcelVO.setMonths(months);
        exportExcelVO.setUserDistName(distName);
        exportExcelVO.setDistname(distName);
        exportExcelVO.setDateStr("2022-07-07");
        exportExcelVO.setCuryear("2022");
        exportExcelVO.setCurmonth("07");
        exportExcelVO.setCurday("07");
        exportExcelVO.setData(data);
        exportExcelVO.setExcelTemplatePath(excelTemplate.getName());
        exportExcelVO.setFileName(excelTemplate.getFileName());
        return exportExcelVO;
    }

    @Override
    public void exportExcelByTemplateAndTag(ExportExcelTagDTO exportExcelTagDTO, HttpServletRequest request, HttpServletResponse response) {
        Integer excelTemplateId = exportExcelTagDTO.getExcelTemplateId();
        Integer years = exportExcelTagDTO.getYears();
        Integer months = exportExcelTagDTO.getMonths();
        String distNo = exportExcelTagDTO.getDistNo();
        String distName = exportExcelTagDTO.getDistName();
        Integer tabId = exportExcelTagDTO.getTabId();
        String lx = exportExcelTagDTO.getLx();
        String lxName = exportExcelTagDTO.getLxName();
        Integer grade = exportExcelTagDTO.getGrade();
        Boolean isWanYuan = exportExcelTagDTO.getIsWanYuan();
        String paramJson = exportExcelTagDTO.getParamJson();
        List<String> sorts = exportExcelTagDTO.getSorts();
        List<Condition> conditions = exportExcelTagDTO.getConditions();
        JSONObject mapJson = JSONObject.fromObject(paramJson);

        String sessionId = request.getSession().getId();
        Map<Object, Object> params = new HashMap<>();
        params.put("years", years);
        if (isWanYuan) {
            params.put("unit", "万元");
        } else {
            params.put("unit", "元");
        }
        if (months != 0) {
            params.put("months", months);
        }
        params.put("userdistName", distName);
        params.put("distname", distName);
        params.put("distid", distNo);
        params.put("tab_id", tabId);
        params.put("sel_lxname", lxName);
        params.put("sessionid", sessionId);
        params.put("lx", lx);
        params.put("level", DataConstants.getMaxDistNoLength(distNo, grade));
        if (null != mapJson && mapJson.size() > 0) {
            params.putAll(mapJson);
        }

        ExcelTemplate excelTemplate = excelTemplateManager.getExcelTemplateById(excelTemplateId);

        response.reset();
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=" + excelTemplate.getFileName()
                + "");
        try {
            ExcelUtilPOI.parse(request.getSession().getServletContext(), response.getOutputStream(), excelTemplate.getName(), excelTemplate.getEcx(), params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ExcelException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> importExcelByTemplate(TableType tableType, Integer excelTemplateId, String rootPath, String filePath, String distNo, Integer years, Integer months) {
        Map<String, Object> map = baseDataManager.importExcelByTemplate(tableType, excelTemplateId, rootPath, filePath, distNo, years, months);
        return map;
    }

    @Override
    public Map<String, Object> listLxsAndLxNames(String typeCode, String tableName, String distNo, Integer years, Integer months) {
        Map<String, Object> map = baseDataManager.listLxsAndLxNames(typeCode, tableName, distNo, years, months);
        return map;
    }

    @Override
    public Map<String, Object> listExistData(ExistDataQuery existDataQuery) {
        Map<String, Object> map = baseDataManager.listExistData(existDataQuery);
        return map;
    }

    @Override
    public Map<String, Object> getPreviousYearData(String typeCode, String tableName, Integer years, Integer months, String paramJson) {
        Map<String, Object> map = baseDataManager.getPreviousYearData(typeCode, tableName, years, months, paramJson);
        return map;
    }

}
