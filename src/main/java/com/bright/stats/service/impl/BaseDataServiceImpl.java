package com.bright.stats.service.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.AssertUtil;
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
import com.bright.stats.util.*;
import com.bright.stats.web.websocket.WebSocket;
import lombok.RequiredArgsConstructor;
import net.sf.excelutils.ExcelException;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/1 15:14
 * @Description
 */
@Service
@RequiredArgsConstructor
public class BaseDataServiceImpl implements BaseDataService {


    private final RestTemplate restTemplate;
    private final WebSocket webSocket;
    private final JdbcTemplate jdbcTemplatePrimary;
    private final BaseDataManager baseDataManager;
    private final FileListManager fileListManager;
    private final UploadBaseManager uploadBaseManager;
    private final DataProcessNewManager dataProcessNewManager;
    private final ExcelTemplateManager excelTemplateManager;
    private final MqMessageManager mqMessageManager;
    private final DistManager distManager;

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
    public SummaryVO summaryCodingRun(SummaryDTO summaryDTO) {
        SummaryVO summaryVO = baseDataManager.summaryCodingRun(summaryDTO);
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
        TableType tableType = checkDTO.getTableType();
        String userDistNo = checkDTO.getUserDistNo();


        List<FileList> fileLists = new ArrayList<>();

        //如果指定了表  则稽核单表
        if(StringUtils.isNotBlank(tableName)){
            fileLists.add(fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo));
        } else {
            fileLists = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
        }

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

    @Transactional(rollbackFor = Throwable.class)
    @Override
    public List<InteractiveVO> reportOrWithdraw(String dpName, String keyword, User user) {
        String tableType = user.getTableType().getTableType();
        String userDistNo = user.getTjDistNo();
        String username = user.getUsername();

        List<InteractiveVO> interactiveVOS = new ArrayList<>();

        //上报前需要先校验当前级别和下级的汇总数是否一直
        if("待办事项_上报".equalsIgnoreCase(dpName)){
            //查询上报的年份
            UploadBase uploadBase = uploadBaseManager.findById(new Integer(keyword.split("_")[0]));

            //查询当前地区的最大长度
            int maxDistLen = distManager.getCurrMaxDistNoLength(uploadBase.getDistNo(), uploadBase.getYears());

            //如果当前地区和操作的地区一样 不在执行汇总
            if(maxDistLen != uploadBase.getDistNo().length()){
                SummaryDTO summaryDTO = new SummaryDTO();

                summaryDTO.setYears(uploadBase.getYears());
                summaryDTO.setMonths(uploadBase.getMonths());
                summaryDTO.setDistNo(uploadBase.getDistNo());
                summaryDTO.setUserDistNo(user.getTjDistNo());
                summaryDTO.setTypeCode(tableType);

                SummaryVO summaryVO = summaryCodingRun(summaryDTO);
                if(!summaryVO.getRvalue()){
                    throw new RuntimeException("上报数据失败");
                }
            }
        }

        List<DataProcessNew> dataProcessNews = dataProcessNewManager.listDataProcessNews(dpName, tableType, -1);

        if(CollectionUtils.isEmpty(dataProcessNews)){
            throw new RuntimeException("没有配置dataProcess");
        }

        for (DataProcessNew dataProcessNew : dataProcessNews) {
            if (dataProcessNew.getAlertType() == 1 || dataProcessNew.getAlertType() == 0) {
                String processSql = dataProcessNew.getProcessSql();
                String alertSql = dataProcessNew.getAlert();

                String fileListSql = " (select * from filelist where 1 = 1 " +
                        " and (belongDistNo like '{userDistNo}%' or '${userDistNo}' like belongDistNo+'%') ) filelist";

                fileListSql = fileListSql.replace("{userDistNo}", userDistNo);

                processSql = processSql.replace("${keyword}", keyword);
                processSql = processSql.replace("${_udistNo}", userDistNo);
                processSql = processSql.replace("${writer}", username);
                processSql = processSql.replace("${filelist}", fileListSql);

                List<Map<String, Object>> maps = null;
                if (dataProcessNew.getIsAlert()) {
//                    maps = jdbcTemplatePrimary.queryForList(processSql);
                    maps = JdbcUtil.queryForMapListGetFirstResultSet(jdbcTemplatePrimary, processSql);

                    if (!CollectionUtils.isEmpty(maps)) {

                        alertSql = alertSql.replace("${keyword}", keyword);
                        alertSql = alertSql.replace("${_udistNo}", userDistNo);
                        alertSql = alertSql.replace("${writer}", username);
                        alertSql = alertSql.replace("${filelist}", fileListSql);

//                        List<String> strings = jdbcTemplatePrimary.queryForList(alertSql, String.class);
                        List<String> strings = JdbcUtil.queryForListGetFirstResultSet(jdbcTemplatePrimary, alertSql, String.class);
                        if (!CollectionUtils.isEmpty(strings)) {

                            for(String str : strings){
                                InteractiveVO temp = new InteractiveVO();
                                temp.setSbFlag(false);
                                temp.setWindowType(1);
                                temp.setWindowInfo(str);
                                interactiveVOS.add(temp);
                            }

                            return interactiveVOS;
                        }
                    }
                } else {
//                    jdbcTemplatePrimary.execute(processSql);
                    String[] idAndDistNo = keyword.split("_");
                    reportOrWithdrawRunForCoding(Integer.valueOf(idAndDistNo[0]), idAndDistNo[1]
                            , user.getTjDistNo()
                            , user.getUsername()
                            , "待办事项_上报".equalsIgnoreCase(dpName));
                }
            } else if(dataProcessNew.getAlertType() == 94) {
                String processSql = dataProcessNew.getProcessSql();
                String alertSql = dataProcessNew.getAlert();

                processSql = processSql.replace("${keyword}", keyword);
                processSql = processSql.replace("${_udistNo}", userDistNo);
                processSql = processSql.replace("${writer}", username);

                List<Map<String, Object>> maps = null;
                if (dataProcessNew.getIsAlert()) {

//                    maps = jdbcTemplatePrimary.queryForList(processSql);
                    maps = JdbcUtil.queryForMapListGetFirstResultSet(jdbcTemplatePrimary, processSql);

                    if (!CollectionUtils.isEmpty(maps)) {

                        for(int i = 0; i < maps.size(); i++){
                            alertSql = alertSql.replace("${c_distno}", (String)maps.get(i).get("c_distno"));

                            //发起网络请求
                            //存在未处理返回true
                            //{"rvalue":false,"isException":false}
                            //进行登录
                            //登陆头信息
                            HttpHeaders existNotProcessDataHeaders = new HttpHeaders();
                            List<String> cookies = new ArrayList<>();

                            // cookie设置
                            existNotProcessDataHeaders.put(HttpHeaders.COOKIE, cookies);


                            //设置请求参数
                            //因为请求为post请求且content-type 的值为application/x-www-form-urlencoded
                            //所有必须用下面的集合来传递参数
                            MultiValueMap<String, Object> existNotProcessDataParam = new LinkedMultiValueMap<>(15);

                            //登陆报文
                            HttpEntity<MultiValueMap<String, Object>> existNotProcessDataHttpEntity =
                                    new HttpEntity<>(existNotProcessDataParam, existNotProcessDataHeaders);

                            //发起登陆请求
                            ResponseEntity<String> existNotProcessDataResponse = null;

                            try{
                                existNotProcessDataResponse = restTemplate.exchange(alertSql
                                        , HttpMethod.GET
                                        , existNotProcessDataHttpEntity
                                        , String.class);

//                                {"rvalue":false,"isException":false}
//                                if(existNotProcessDataResponse.getBody().contains(""))


                                Map<String, Object> map = (Map<String, Object>) JSONObject.toBean(JSONObject.fromObject(existNotProcessDataResponse.getBody()), Map.class);

                                //如果请求失败或存在预警的数据 则返回
                                if(CollectionUtils.isEmpty(map)
                                        || ((Boolean) map.get("rvalue"))
                                        || ((Boolean) map.get("isException"))){
                                    //返回要跳转的连接
                                    InteractiveVO temp = new InteractiveVO();
                                    temp.setWindowType(2);
                                    temp.setWindowInfo(dataProcessNew.getAlertSub());
                                    temp.setSbFlag(false);
                                    interactiveVOS.add(temp);

                                    return interactiveVOS;
                                }

//                                InteractiveVO temp = new InteractiveVO();
//                                temp.setWindowType(2);
//                                temp.setWindowInfo(dataProcessNew.getAlertSub());
//                                interactiveVOS.add(temp);

                            }catch (Exception e){
                                e.printStackTrace();
                                throw new RuntimeException("校验失败！");
                            }

                        }
                    }
                } else {
//                  jdbcTemplatePrimary.execute(processSql);
                    String[] idAndDistNo = keyword.split("_");
                    reportOrWithdrawRunForCoding(Integer.valueOf(idAndDistNo[0]), idAndDistNo[1]
                            , user.getTjDistNo()
                            , user.getUsername()
                            , "待办事项_上报".equalsIgnoreCase(dpName));
                }
            } else {
                continue;
            }
        }
        return null;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void reportOrWithdrawRunForCoding(Integer uploadBaseId, String distNo, String userDistNo, String username, Boolean isReport){
        UploadBase uploadBase = uploadBaseManager.findById(uploadBaseId);
        if(Objects.isNull(uploadBase)){
            throw new RuntimeException("没有上报或退回单位的记录！");
        }

        if(!uploadBase.getRunFlag()){
            throw new RuntimeException("当前上报或退回单位不可用！");
        }

        Integer years = uploadBase.getYears();
        Integer months = uploadBase.getMonths();
        String tableType = uploadBase.getTableType();

        Boolean isReportBefore = false;

        Dist dist = distManager.getDistByYearAndDistNo(years, distNo);

        if(distNo.equalsIgnoreCase("0")){
            distNo = "%";
        }

        //查询出所有上报数据单位（包括已上报和未上报）
        List<UploadBase> allUploadBasesList = uploadBaseManager.findReportByDistAndTableType(years, months, distNo, tableType);

        //筛选出已经上报了的单位
        List<UploadBase> reportUploadBasesList = allUploadBasesList.stream().filter(e -> {
            if(e.getRunFlag() && e.getOkFlag() && e.getSbFlag()){
                return true;
            }
            return false;
        }).collect(Collectors.toList());


        StringBuilder updateUploadBaseSql = new StringBuilder();


        if(isReport){
            //如果是上报

            //上报本级
            updateUploadBaseSql.setLength(0);
            updateUploadBaseSql.append("update uploadBase set sbflag=1,okflag=1,outbz=1,bdate=NOW(),writer= '" + username +
                    "' WHERE uploadBase.years=" + years + " AND uploadBase.months=" + months +
                    " AND uploadBase.distno='" + distNo + "' and uploadBase.runflag=1 " +
                    " and uploadBase.okflag=0 and uploadBase.tableType='" + tableType + "'");
            jdbcTemplatePrimary.execute(updateUploadBaseSql.toString());


        } else {
                //如果是退回
            updateUploadBaseSql.setLength(0);
            updateUploadBaseSql.append("update uploadBase set sbflag=0,okflag=0,outbz=0,bdate=null,edate=NOW(),writer= '" + username + "' " +
                    "WHERE years=" + years + " AND months=" + months +
                    " AND distno='" + distNo + "' and runflag=1 and okflag=1  and distno<>'" + userDistNo + "' and tableType='" + tableType + "'");
                jdbcTemplatePrimary.execute(updateUploadBaseSql.toString());

        }


        //处理完成后
//        List<FileList> fileLists = fileListManager.listFileLists(tableType, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
//
//
//        //关联更新所有单位下的表数据状态
//        for (FileList fileList : fileLists) {
//
//            StringBuilder updateSql = new StringBuilder();
//
//            if(isReport){
//                updateSql.setLength(0);
//                //如果是上报
//                updateSql.append("UPDATE " + fileList.getTableName() + " set okflag=1, statusno=3, saveflag='是',upReportTime=NOW() where years = ")
//                        .append(years).append(" and distid like '").append(distNo).append("%'").append(" and ifnull(okflag, 0) = 0");
//
//            } else {
//                //如果是退回
//                updateSql.setLength(0);
//
//                if(isReportBefore){
//                    //如果是上报
//                    updateSql.append("UPDATE " + fileList.getTableName() + " set okflag=0, statusno=2, saveflag='否',upReportTime=null where years = ")
//                            .append(years).append(" and distid like '").append(distNo).append("%'").append(" and okflag=1 and distid<>'").append(userDistNo).append("'");
//                } else {
//
//                    //如果是上报
//                    updateSql.append("UPDATE " + fileList.getTableName() + " set okflag=0, statusno=2, saveflag='否',upReportTime=null where years = ")
//                            .append(years).append(" and distid = '").append(distNo).append("%'").append(" and okflag=1 and distid<>'").append(userDistNo).append("'");
//
//                }
//            }
//            jdbcTemplatePrimary.execute(updateSql.toString());
//        }


        String fileNamesSql = "SELECT uploadBase.years as years, uploadBase.months as months, fileList.tableName as tablename, " +
                "fileList.tableDis as tableDis, " +
                "tableType.optType as optType, isDeleteTab as isDeleteTab" +
                "         from uploadBase inner join fileList" +
                "          on uploadBase.tableType=fileList.typeCode and filelist.tableType='基本表'   and uploadBase.years=fileList.years  inner join tableType " +
                "           on uploadBase.tableType=tableType.tableType    " +
                "        where uploadBase.id=" + uploadBaseId;

        List<Map<String, Object>> fileLists = jdbcTemplatePrimary.queryForList(fileNamesSql);

        //关联更新所有单位下的表数据状态
        for (Map<String, Object> fileList : fileLists) {

            StringBuilder updateSql = new StringBuilder();

            if (isReport) {
                updateSql.setLength(0);
                updateSql.append("UPDATE " + fileList.get("tablename") + " set okflag=1, statusno=3, saveflag='是',upReportTime=NOW() where years = ")
                        .append(years).append(" and distid = '").append(distNo).append("'").append(" and ifnull(okflag, 0) = 0");
            } else {
                updateSql.setLength(0);
                updateSql.append("UPDATE " + fileList.get("tablename") + " set okflag=0, statusno=2, saveflag='否',upReportTime=null where years = ")
                        .append(years).append(" and distid = '").append(distNo).append("'").append(" and okflag=1 and distid<>'").append(userDistNo).append("'");
            }

            jdbcTemplatePrimary.execute(updateSql.toString());
        }
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
    public List<ExcelTemplateVO> listExcelTemplates(Integer years, String typeCode, String username, String userDistNo) {
        List<ExcelTemplate> excelTemplates = excelTemplateManager.listExcelTemplates(years, typeCode, username, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, userDistNo);
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
        Boolean isGrade = exportExcelDTO.getIsGrade();


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
        baseDataQuery.setUserDistNo(userDistNo);
        baseDataQuery.setIsGrade(isGrade);

        if (!"多表一个Excel".equals(excelTemplate.getExcelType())) {
            List<Map<String, Object>> list = baseDataManager.listTableData(baseDataQuery, false, List.class);
            FileList fileList = fileListManager.getFileList(typeCode
                    , FileListConstant.FILE_LIST_TABLE_TYPE_BASE
                    , baseDataQuery.getTableName()
                    , years
                    , months
                    , userDistNo);

            List<FileItem> fileItems = fileList.getFileItems();

            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> tempRes = list.get(i);

                for (int i1 = 0; i1 < fileItems.size(); i1++) {
                    FileItem fileItem = fileItems.get(i1);

                    //如果不是数值类型且存在
                    if(!"N".equalsIgnoreCase(fileItem.getFType()) && tempRes.containsKey(fileItem.getFieldName())){
                        //把value的值由null换成空字符串
                        tempRes.put(fileItem.getFieldName(), Objects.isNull(tempRes.get(fileItem.getFieldName()))
                                ? "" : tempRes.get(fileItem.getFieldName()));
                    }

                    //不是数值类型 或者不存在key 跳过
                    if(!"N".equalsIgnoreCase(fileItem.getFType())
                            || !tempRes.containsKey(fileItem.getFieldName())
                            || !"1".equalsIgnoreCase(fileItem.getDisFlag())){
                        continue;
                    }

                    //获得数值的格式化
                    String disFormat = StringUtils.isEmpty(fileItem.getDisFormat()) ? "" : fileItem.getDisFormat().split(",")[0];

                    //根据格式化做对应的处理
                    //获取小数位数
                    Integer unitSize = disFormat.split("\\.").length < 2 ? 0 : disFormat.split("\\.")[1].length();

                    if(Objects.isNull(tempRes.get(fileItem.getFieldName()))){
                        tempRes.put(fileItem.getFieldName(), new BigDecimal(0).setScale(unitSize));
                    } else {
                        BigDecimal value = new BigDecimal(tempRes.get(fileItem.getFieldName()).toString());
                        tempRes.put(fileItem.getFieldName(), value.setScale(unitSize, BigDecimal.ROUND_DOWN));
                    }

                   /* BigDecimal value = (BigDecimal)tempRes.get(fileItem.getFieldName());
                    //值为空重新赋值
                    if(Objects.isNull(value)){
                        tempRes.put(fileItem.getFieldName(), new BigDecimal(0).setScale(unitSize));
                    } else {
                        //不为空处理数值位数
                        tempRes.put(fileItem.getFieldName(), value.setScale(unitSize, BigDecimal.ROUND_DOWN));
                    }*/
                }
            }

           

            data.put(tableName.toLowerCase(), list);
        } else {
            List<FileList> fileLists = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
            for (FileList fileList : fileLists) {
                baseDataQuery.setTableName(fileList.getTableName());

                //在导出所有表的情况下 如果选择了类型条件，有些表没有'lx'则导出全部
                if(CollectionUtils.isEmpty(fileList.getFileItemLinkExs())){
                    baseDataQuery.setLx("全部");
                } else {
                    baseDataQuery.setLx(lx);
                }
                List<Map<String, Object>> list = baseDataManager.listTableData(baseDataQuery, false, List.class);

                List<FileItem> fileItems = fileList.getFileItems();


                if (excelTemplate.getTemplateShape() == 2) {

                    for (int i = 0; i < list.size(); i++) {
                        Map<String, Object> tempRes = list.get(i);

                        for (int i1 = 0; i1 < fileItems.size(); i1++) {
                            FileItem fileItem = fileItems.get(i1);


                            //如果不是数值类型且存在
                            if(!"N".equalsIgnoreCase(fileItem.getFType()) && tempRes.containsKey(fileItem.getFieldName())){
                                //把value的值由null换成空字符串
                                tempRes.put(fileItem.getFieldName(), Objects.isNull(tempRes.get(fileItem.getFieldName()))
                                        ? "" : tempRes.get(fileItem.getFieldName()));
                            }

                            //不是数值类型 或者不存在key 跳过
                            if(!"N".equalsIgnoreCase(fileItem.getFType()) || !tempRes.containsKey(fileItem.getFieldName())){
                                continue;
                            }

                            //获得数值的格式化
                            String disFormat = StringUtils.isEmpty(fileItem.getDisFormat()) ? "" : fileItem.getDisFormat().split(",")[0];

                            //根据格式化做对应的处理
                            //获取小数位数
                            Integer unitSize = disFormat.split("\\.").length < 2 ? 0 : disFormat.split("\\.")[1].length();

                            if(Objects.isNull(tempRes.get(fileItem.getFieldName()))){
                                tempRes.put(fileItem.getFieldName(), new BigDecimal(0).setScale(unitSize));
                            } else {
                                BigDecimal value = new BigDecimal(tempRes.get(fileItem.getFieldName()).toString());
                                tempRes.put(fileItem.getFieldName(), value.setScale(unitSize, BigDecimal.ROUND_DOWN));
                            }

                            /*BigDecimal value = (BigDecimal)tempRes.get(fileItem.getFieldName());
                            //值为空重新赋值
                            if(Objects.isNull(value)){
                                tempRes.put(fileItem.getFieldName(), new BigDecimal(0).setScale(unitSize));
                            } else {
                                //不为空处理数值位数
                                tempRes.put(fileItem.getFieldName(), value.setScale(unitSize, BigDecimal.ROUND_DOWN));
                            }*/
                        }
                    }

                    data.put(fileList.getTableName().toLowerCase(), list);

                } else {
                    throw new RuntimeException("不支持用户自定义excel导出！");
                }

            }

            /*List<FileList> fileLists2 = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, "0");
            for (FileList fileList : fileLists2) {
                if(Objects.isNull(data.get(fileList.getTableName().toLowerCase()))){
                    data.put(fileList.getTableName().toLowerCase(), new ArrayList<>());
                }
            }*/
        }


        ExportExcelVO exportExcelVO = new ExportExcelVO();
        exportExcelVO.setYears(years);
        exportExcelVO.setMonths(months);
        exportExcelVO.setUserDistName(distName);
        exportExcelVO.setDistname(distName);
        exportExcelVO.setDateStr(DateUtil.getDate(new Date()));
        exportExcelVO.setCuryear(DateUtil.getDate(new Date(), 1));
        exportExcelVO.setCurmonth(DateUtil.getDate(new Date(), 2));
        exportExcelVO.setCurday(DateUtil.getDate(new Date(), 3));
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
        response.setHeader("Content-Disposition", "attachment; filename=" + excelTemplate.getFileName() + "");
        
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

    /**
     * 获取基本表列表
     *
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    @Override
    public List<FileList> listBaseTables(String typeCode, Integer years, Integer months) {
        List<FileList> fileLists = baseDataManager.listBaseTables(typeCode, years, months);
        return fileLists;
    }

    @Override
    public List<Integer> getDistAllGrade() {
        List<Integer> distAllGrade = baseDataManager.getDistAllGrade();
        return distAllGrade;
    }

    @Override
    public Boolean initUploadData(InitUploadDataDTO initUploadDataDTO) {
        Boolean refalse=false;
        try {
//			String sql = "insert into uploadBase(years, months,distno, distName, tableType, type, name, bdate, edate, okflag,sbflag, runflag)" +
//					"select #years# as years,#months# as months, distid, distName,(select tableType from tableType where id=${__tableTypeId})as tableType,'N-#years#-#months#' as type, (d.distName+(select tableType from tableType where id=${__tableTypeId})+'数据统计表') as name,null as bdate, null as edate,0 as  okflag,0 as  sbflag,1 as runflag " +
//					" from dist d where years=#years#   and d.distId like case when '#_distNo#'='0'  then '%' else '#_distNo#%'end" +
//					"  and  not exists (select u.id from uploadBase u where d.years=u.years and u.months=#months# and d.distId=u.distno and u.tableType=(select tableType from tableType where id=${__tableTypeId}))";

            Map<String, Object> paramMap = new HashMap();
            paramMap.put("years", initUploadDataDTO.getYears());
            paramMap.put("months", initUploadDataDTO.getMonths());
            paramMap.put("distNo", initUploadDataDTO.getDistNo());

            String sql="${uploadbase_sql}";

            sql= Common.replaceFun1(sql, this.funcontrast(2));
            sql= ReplaceSqlUtil.getSql(sql, "");
            sql=Common.replaceParamsbyKeyNotNull(sql, paramMap);
            int renum=jdbcTemplatePrimary.update(sql);
            if(renum >- 1){
                refalse = true;
            }
        } catch (DataAccessException e) {
            e.printStackTrace();
        }
        return refalse;
    }

    /**
     * 校验正在运行的稽核操作
     *
     * @param mqMessagesDTO
     * @return
     */
    @Override
    public Boolean checkRunning(MqMessagesDTO mqMessagesDTO) {
        Boolean checkFlag = mqMessageManager.checkRunning(mqMessagesDTO);
        return checkFlag;
    }

    @Override
    public Boolean checkRunningByDist(MqMessagesDTO mqMessagesDTO) {

        String distNo = mqMessagesDTO.getDistNo();

        List<MqMessage> mqMessageList = mqMessageManager.checkRunningAll(mqMessagesDTO);

        //如果当前操作的地区上级或下级存在操作 则停止操作
        mqMessageList = mqMessageList.stream().filter(e -> {
            if(e.getDistNo().startsWith(distNo)
                    || distNo.startsWith(e.getDistNo())){
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        return !(mqMessageList.size() > 0);
    }

    @Override
    public void checkRuningAll(MqMessagesDTO mqMessagesDTO) {

        //查询当前是否存在正在执行的任务、正在被消费
        List<MqMessage> runMqMessages = mqMessageManager.findRunningMessage(mqMessagesDTO);
        if(!CollectionUtils.isEmpty(runMqMessages)){
            throw new RuntimeException("当前存在正在执行的任务！");
        }

        //查询当前是否存在待执行的任务、还未开始消费
        List<MqMessage> takeMqMessages = mqMessageManager.findTakeMessage(mqMessagesDTO);
        if(!CollectionUtils.isEmpty(takeMqMessages)){
            throw new RuntimeException("当前存在执行的任务！");
        }
    }

    /**
     * 根据条件获取上报情况
     *
     * @param distNo
     * @param years
     * @param months
     * @param tableType
     * @return true:已上报  false:未上报
     */
    @Override
    public Boolean getReportSituation(String distNo, Integer years, Integer months, String tableType) {
        List<UploadBase> reportByDistAndTableType = uploadBaseManager.findReportByDistAndTableType(years, months, distNo, tableType);

        //为空 直接判定为未上报
        if(CollectionUtils.isEmpty(reportByDistAndTableType)){
            return false;
        }

        //为空 直接判定为未上报
        reportByDistAndTableType = reportByDistAndTableType.stream().filter(e -> e.getDistNo().equalsIgnoreCase(distNo)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(reportByDistAndTableType)){
            return false;
        }

        //查到数据并且已经上报了
        if(reportByDistAndTableType.get(0).getOkFlag()){
            return true;
        } else {
            return false;
        }

    }


    public List<Map<String, Object>> funcontrast(int ttype) {
        List<Map<String, Object>> rvalue = null;
        String sql = "select mysql_fun, sql_fun, valuedec, sqlstr, kingbase_sqlstr from fun_contrast where visible=1 and ttype=? order by disid";
        rvalue = jdbcTemplatePrimary.queryForList(sql, new Object[]{ttype});
        return rvalue;
    }


    /**
     * 校验上报前上下级汇总数据的一致性
     * @return
     */
    public boolean verificationGatherUniformity(String keyword, User user){
        String[] idAndDistNo = keyword.split("_");
        Integer id = new Integer(idAndDistNo[0]);
        String distNo = idAndDistNo[1];

        String tableType = user.getTableType().getTableType();
        String userDistNo = user.getTjDistNo();
        String username = user.getUsername();

        //查询上报的年份
        UploadBase uploadBase = uploadBaseManager.findById(id);

        //查询要上报的所有表
        List<FileList> fileLists
                = fileListManager.listFileLists(tableType, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, uploadBase.getYears(), uploadBase.getMonths(), userDistNo);


        for (FileList fileList : fileLists) {
            List<FileItem> fileItems = fileList.getFileItems();
            List<String> fieldList = new ArrayList<>();
            for (FileItem fileItem : fileItems) {
                if("1".equalsIgnoreCase(fileItem.getDisFlag()) && "N".equalsIgnoreCase(fileItem.getFType())){
                    fieldList.add(fileItem.getFieldName());
                }
            }

            String fieldNames = fieldList.stream().map(e -> "sum(" + e + ") as " + e).collect(Collectors.joining(", "));

            //查询下级数据的汇总
            String downLevelSql = "select " + fieldNames + " from " + fileList.getTableName()
                    + " where 1 = 1 and distid like '" + distNo + "%' and distid <> " + "'" + distNo + "' "
                    + " and years = " + uploadBase.getYears();

            String currentLevelSql = "select " + fieldNames + " from " + fileList.getTableName()
                    + " where 1 = 1 and distid = " + "'" + distNo + "' "
                    + " and years = " + uploadBase.getYears();


            if(!CollectionUtils.isEmpty(fileList.getFileItemLinkExs())){
                //如果有类型则只判断汇总数
                downLevelSql = downLevelSql + " and lx = '汇总数'";
                currentLevelSql = currentLevelSql + " and lx = '汇总数'";
            }

            List<Map<String, Object>> downLevelDataList = jdbcTemplatePrimary.queryForList(downLevelSql);
            List<Map<String, Object>> currentLevelDataList = jdbcTemplatePrimary.queryForList(currentLevelSql);

            if(CollectionUtils.isEmpty(downLevelDataList) || CollectionUtils.isEmpty(currentLevelDataList)){
                throw new RuntimeException("没有对应的上报基础数据!");
            }

            Map<String, Object> downLevelData = downLevelDataList.get(0);
            Map<String, Object> currentLevelData = currentLevelDataList.get(0);

            for (String s : downLevelData.keySet()) {
                if(!downLevelData.get(s).equals(currentLevelData.get(s))){
                    throw new RuntimeException(fileList.getTableDis() + "：未进行汇总稽核!");
                }
            }
        }

        return true;
    }


}
