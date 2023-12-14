package com.bright.stats.service.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.security.SecurityUser;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.*;
import com.bright.stats.pojo.dto.CreateEmptyTableDTO;
import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.pojo.query.MqMessagesQuery;
import com.bright.stats.pojo.vo.InteractiveVO;
import com.bright.stats.pojo.vo.InteractiveVOEx;
import com.bright.stats.repository.primary.FileItemRepository;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.repository.primary.NoteRepository;
import com.bright.stats.service.NavigateService;
import com.bright.stats.util.ReplaceSqlUtil;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

import javax.persistence.criteria.*;
import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/7/28 16:20
 * @Description
 */
@Service
@RequiredArgsConstructor
public class NavigateServiceImpl implements NavigateService {

    private final TableTypeManager tableTypeManager;
    private final NavigateManager navigateManager;
    private final MqMessageRepository mqMessageRepository;
    private final MqMessageManager mqMessageManager;
    private final FileListManager fileListManager;
    private final NoteRepository noteRepository;
    private final JdbcTemplate jdbcTemplatePrimary;
    private final FileItemManager fileItemManager;
    private final FileItemRepository fileItemRepository;


    @Override
    public List<TableType> listTableTypes() {
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        return tableTypes;
    }

    @Override
    public void selectMode(Integer tableTypeId) {
        TableType tableType = tableTypeManager.getTableTypeById(tableTypeId);
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        if(tableTypes.indexOf(tableType) == -1){
            throw new RuntimeException("当前选择的模式不存在！");
        }

        //用户选择模式之后将模式绑定到用户中
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //如果没有登陆用户
        if(Objects.isNull(principal)){
            throw new RuntimeException("用户未登陆！");
        }

        //如果用户是登陆的状态 绑定用户选择的模式
        SecurityUser securityUser = (SecurityUser) principal;
        securityUser.getUser().setTableType(tableType);

        //暂时的
//        SecurityUtil.getLoginUser().setTableType(tableType);

//        loginUser.setTableType(tableType);
//        List<Dist> dists = new ArrayList<>();
//        loginUser.setDists(dists);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
//        securityUser.setUser(loginUser);
//
//        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(securityUser, authentication.getCredentials());
//        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

    }

    @Override
    public void selectMode(Integer tableTypeId, Integer years) {
        TableType tableType = tableTypeManager.getTableTypeById(tableTypeId);
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        if(tableTypes.indexOf(tableType) == -1){
            throw new RuntimeException("当前选择的模式不存在！");
        }

        if (years < tableType.getBeginYear() || years > tableType.getCurNewYear()) {
            throw new RuntimeException("选择的年份不在" + tableType.getBeginYear() + "~" + tableType.getCurNewYear() + "区间范围内！");
        }

        //设置当前选中的年份
        tableType.setSelectYear(years);

        //用户选择模式之后将模式绑定到用户中
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        //如果没有登陆用户
        if(Objects.isNull(principal)){
            throw new RuntimeException("用户未登陆！");
        }

        //如果用户是登陆的状态 绑定用户选择的模式
        SecurityUser securityUser = (SecurityUser) principal;
        securityUser.getUser().setTableType(tableType);

    }

    @Override
    public List<MqMessage> listMqMessages(MqMessagesQuery mqMessagesQuery) {


        MqMessage mqMessage = new MqMessage();

        mqMessage.setUsername(mqMessagesQuery.getUsername());
        mqMessage.setTypeCode(mqMessagesQuery.getTypeCode());
        mqMessage.setMonths(mqMessagesQuery.getMonths());
        mqMessage.setDistNo(mqMessagesQuery.getDistNo());

        if (!StringUtils.isEmpty(mqMessagesQuery.getTopicType())) {
            mqMessage.setTopicType(mqMessagesQuery.getTopicType());
        }

        if (mqMessagesQuery.getConsumerFlag() != null) {
            mqMessage.setConsumerFlag(mqMessagesQuery.getConsumerFlag());
        }

        if (mqMessagesQuery.getReadFlag() != null) {
            mqMessage.setReadFlag(mqMessagesQuery.getReadFlag());
        }

//        Example<MqMessage> mqMessagesQueryExample = Example.of(mqMessage);
//        List<MqMessage> mqMessages = mqMessageRepository.findAll(mqMessagesQueryExample);

        List<MqMessage> mqMessages = mqMessageRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> ps = new ArrayList<>();


            ps.add(criteriaBuilder.equal(root.get("username"), mqMessagesQuery.getUsername()));
            ps.add(criteriaBuilder.equal(root.get("typeCode"), mqMessagesQuery.getTypeCode()));
            ps.add(criteriaBuilder.equal(root.get("months"), mqMessagesQuery.getMonths()));

//            if(StringUtils.isNotBlank(mqMessagesQuery.getTopicType())){
//                ps.add(criteriaBuilder.equal(root.get("topicType"), mqMessagesQuery.getTopicType()));
//            }

            //应该要能查询到上级的执行情况  但是不能操作上级
            if(StringUtils.isNotBlank(mqMessagesQuery.getDistNo())){
//                ps.add(criteriaBuilder.like(root.get("distNo"), mqMessagesQuery.getDistNo() + "%"));
            }

            if(mqMessagesQuery.getReadFlag() != null){
                ps.add(criteriaBuilder.equal(root.get("readFlag"), mqMessagesQuery.getReadFlag()));
            }

            if (mqMessagesQuery.getConsumerFlag() != null) {
                ps.add(criteriaBuilder.equal(root.get("consumerFlag"), mqMessagesQuery.getConsumerFlag()));
            }
            criteriaQuery.where(ps.toArray(new Predicate[ps.size()]));
            criteriaQuery.orderBy(criteriaBuilder.desc(root.get("updatedTime")));
            return criteriaQuery.getRestriction();
        });
        mqMessages = mqMessages.stream().filter(e -> {
            if(e.getDistNo().startsWith(mqMessage.getDistNo()) || mqMessage.getDistNo().startsWith(e.getDistNo())){
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        return mqMessages;
    }

    /**
     * 批量阅读
     *
     * @param mqMessagesQuery
     * @return
     */
    @Override
    public Boolean readMqMessages(MqMessagesDTO mqMessagesQuery) {
        Integer size = mqMessageRepository.readMqMessages(mqMessagesQuery.getIds(), mqMessagesQuery.getUsername());

        if(size > 0){
            return true;
        }
        return false;
    }

    /**
     * 撤销消息
     *
     * @param mqMessagesDTO
     * @return
     */
    @Override
    public List<MqMessage> revokeMessage(MqMessagesDTO mqMessagesDTO) {


        //如果id为空 根据其他条件来撤回
        if(CollectionUtils.isEmpty(mqMessagesDTO.getIds())){

            //撤回前判断消息是否可以被撤回
            List<MqMessage> mqMessages = mqMessageManager.findTakeMessage(mqMessagesDTO);

            if(CollectionUtils.isEmpty(mqMessages)){
                throw new RuntimeException("没有可撤回的任务或当前任务正在执行中！");
            }


            Integer size = mqMessageRepository.revokeMessageByParam(mqMessagesDTO.getYears()
                    , mqMessagesDTO.getMonths()
                    , mqMessagesDTO.getDistNo()
                    , mqMessagesDTO.getTypeCode()
                    , mqMessagesDTO.getUsername());

            if(size > 0){
                return new ArrayList<>();
            }

            return mqMessages;

        } else {
            //撤回前判断消息是否可以被撤回
            List<MqMessage> mqMessages = mqMessageManager.listAvailableMqMessagesByIds(mqMessagesDTO);

            if(CollectionUtils.isEmpty(mqMessages)){
                throw new RuntimeException("没有可撤回的任务或当前任务正在执行中");
            }

            //如果数据不一致
            if(mqMessagesDTO.getIds().size() != mqMessages.size()){
                return mqMessages.stream().filter(e -> mqMessagesDTO.getIds().contains(e.getId())).collect(Collectors.toList());
            }

            Integer size = mqMessageRepository.revokeMessageByIds(mqMessagesDTO.getIds());
            if(size > 0){
                return new ArrayList<>();
            }

            return mqMessages;
        }

    }


    @Override
    public List<Integer> validMessageTask(MqMessagesDTO mqMessagesDTO) {
        List<Integer> result = new ArrayList<>();


        //查询当前是否存在正在执行的任务、正在被消费
        List<MqMessage> runMqMessages = mqMessageManager.findRunningMessage(mqMessagesDTO);
        if(!CollectionUtils.isEmpty(runMqMessages)){
            result.add(1);
        }

        //查询当前是否存在待执行的任务、还未开始消费
        List<MqMessage> takeMqMessages = mqMessageManager.findTakeMessage(mqMessagesDTO);
        if(!CollectionUtils.isEmpty(takeMqMessages)){
            result.add(2);
        }

        return result;
    }


    /**
     * 根据条件生成空配置数据
     *
     * @param createEmptyTableDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Boolean createEmptyTable(CreateEmptyTableDTO createEmptyTableDTO) {

        String tableName = createEmptyTableDTO.getTableName();
        Integer years = createEmptyTableDTO.getYears();
        Integer beginMonths = createEmptyTableDTO.getBeginMonths();
        Integer endMonths = createEmptyTableDTO.getEndMonths();
        String typeCode = createEmptyTableDTO.getTypeCode();
        String distNo = createEmptyTableDTO.getDistNo();
        Integer optType = createEmptyTableDTO.getOptType();

        //

        //生成空的配置表
        createConfigEmptyTable(typeCode, years);

        //生成空的数据表
        createDataEmptyTable(createEmptyTableDTO);

        return true;
    }


    @Override
    public List<InteractiveVOEx> listNotes(String keyword, String tableType) {

        List<InteractiveVOEx> interactiveVOExes = new ArrayList<>();

        List<Note> notes = noteRepository.findNotes();

        if(CollectionUtils.isEmpty(notes)){
            return interactiveVOExes;
        }

        if(StringUtils.isNotBlank(keyword)){
            notes = notes.stream().filter(e -> {
                if(keyword.equalsIgnoreCase(e.getKeyword())){
                    return true;
                }
                return false;
            }).collect(Collectors.toList());
        }


        if(CollectionUtils.isEmpty(notes)){
            return interactiveVOExes;
        }

        notes = notes.stream().filter(e -> {
            if("系统".equalsIgnoreCase(e.getTypeCode())
                    || e.getTypeCode().equalsIgnoreCase(tableType)){
                return true;
            }
            return false;
        }).collect(Collectors.toList());


        for (Note note : notes) {

            InteractiveVOEx interactiveVOEx = new InteractiveVOEx();
            interactiveVOEx.setWindowType(0);
            interactiveVOEx.setMessageType(note.getKeyword());
            interactiveVOEx.setWindowsUrl(note.getAlterUrl());

            String contentSql = ReplaceSqlUtil.getSql(note.getAlterSql(), null);

            //获取内容
            List<Map<String, Object>> content = jdbcTemplatePrimary.queryForList(contentSql);
            if(!CollectionUtils.isEmpty(content) && !CollectionUtils.isEmpty(content.get(0))){

                if(!Objects.isNull(content.get(0))){
                    String info = content.get(0).values().stream().map(e -> e.toString()).collect(Collectors.joining(","));
                    interactiveVOEx.setWindowInfo(info);
                }
            }


            if(StringUtils.isNotBlank(note.getAlterType()) && "1".equalsIgnoreCase(note.getAlterType())){
                interactiveVOEx.setWindowType(1);
                String paramsSql = ReplaceSqlUtil.getSql(note.getAlterParam(), null);
                List<Map<String, Object>> params = jdbcTemplatePrimary.queryForList(paramsSql);
                if(!CollectionUtils.isEmpty(params.get(0))){
                    String paramsJson = params.get(0).get("params").toString();
                    interactiveVOEx.setWindowsParam(paramsJson);
                }
            }
            interactiveVOExes.add(interactiveVOEx);
        }

        return interactiveVOExes;
    }

    /**
     * 生成空的配置表数据
     * @param tableType
     * @param years
     * @return
     */
    @Transactional(rollbackFor = Throwable.class)
    public Boolean createConfigEmptyTable(String tableType, Integer years){

        //查询所以可用的模式 年报、月报等
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        String tableCode = "";
        StringBuilder columns = new StringBuilder();
        StringBuilder modifyColumns = new StringBuilder();
        String sql = "";
        int num = 0;

        tableTypes = tableTypes.stream().filter(e -> {
            if(e.getTableType().equalsIgnoreCase(tableType)){
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());


        //
        for (int i = 0; i < tableTypes.size(); i++) {
            TableType tempTableType = tableTypes.get(i);
            tableCode = tempTableType.getTableType();

            /*----------------------------追加fileList数据---------------------------------*/
            List<FileList> fileLists
                    = fileListManager.listFileLists(tableCode, years, 0);

            //如果当前年份没有查询出数据
            if(CollectionUtils.isEmpty(fileLists)){

                columns.setLength(0);
                modifyColumns.setLength(0);

                //需要插入上一年的空数据
                findTableColumns("fileList", "years", "m", columns, modifyColumns);

                //将上一年的数据添加到当前年份
                String lastYearFileListDataSql = "insert into fileList(years," + columns + ") " +
                        " select " + years + " years, " + modifyColumns + " from filelist m " +
                        " where m.typeCode='" + tableCode + "' and m.years=" + (years - 1) + " and isnull(m.tablename,'')<>''";

                jdbcTemplatePrimary.update(lastYearFileListDataSql);

            }



            /*----------------------------追加fileItem数据---------------------------------*/
            String baseFileItemSql = "select count(*) row_count from fileitem m inner join filelist f " +
                    "on m.years=f.years and m.tablename=f.tableName and typeCode=? and tableType='基本表' and ISNULL(f.tablename,'')<>''" +
                    "where m.years=?";
            List<Map<String, Object>> baseFileItemListMaps = jdbcTemplatePrimary.queryForList(baseFileItemSql, tableCode, years);

            if(CollectionUtils.isEmpty(baseFileItemListMaps)
                    || (!CollectionUtils.isEmpty(baseFileItemListMaps)
                    && (Long)baseFileItemListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("fileItem", "years", "m", columns, modifyColumns);

                String lastYearsBaseFileItemDataSql = "insert into fileItem(years, " + columns + ") " +
                        "select " + years + "  years, " + modifyColumns + " from fileitem m inner join filelist f  " +
                        "on m.years=f.years and m.tablename=f.tableName and  f.typeCode='" + tableCode + "' " +
                        "and tableType='基本表' and isnull(f.tablename,'')<>'' " +
                        "where  m.years= " + (years - 1) + " and isnull(m.tablename,'') <> ''";

                jdbcTemplatePrimary.update(lastYearsBaseFileItemDataSql);
            }




            /*----------------------------追加fileItem分析数据---------------------------------*/
            String ansFileItemSql = "select count(m.id) row_count from fileitem m inner join filelist f on m.years=f.years " +
                    "and m.tablename='ans_'||convert(varchar(10),f.ansNo) " +
                    "and typeCode=? and tableType='分析表' and ISNULL(f.tablename,'')<>'' where m.years=?";
            List<Map<String, Object>> ansFileItemListMaps = jdbcTemplatePrimary.queryForList(ansFileItemSql, tableCode, years);


            if(CollectionUtils.isEmpty(ansFileItemListMaps)
                    || (!CollectionUtils.isEmpty(ansFileItemListMaps)
                    && (Long)ansFileItemListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("fileItem", "years", "m", columns, modifyColumns);

                String lastYearsAnsFileItemDataSql = "insert into fileItem(years, " + columns + ") " +
                        "select " + years + " years, " + modifyColumns + " from fileitem m inner join filelist f " +
                        "on m.years = f.years and m.tablename = 'ans_'||convert(varchar(10),f.ansNo) and f.typeCode='" + tableCode + "' and tableType='分析表' " +
                        "and ISNULL(f.tablename,'')<>''  and ISNULL(f.ansNo,0)<>0" +
                        " where  m.years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsAnsFileItemDataSql);
            }



            /*----------------------------追加fileItemLink 数据---------------------------------*/
            String fileItemLink = "select count(k.id) row_count from fileItemLink k inner join filelist f on k.years=f.years " +
                    "and k.tableName=f.tableName and f.typeCode=? " +
                    "and f.tablename<>'' where k.years=?";

            List<Map<String, Object>> fileItemLinkListMaps = jdbcTemplatePrimary.queryForList(fileItemLink, tableCode, years);


            if(CollectionUtils.isEmpty(fileItemLinkListMaps)
                    || (!CollectionUtils.isEmpty(fileItemLinkListMaps)
                    && (Long)fileItemLinkListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("fileItemLink", "years", "k", columns, modifyColumns);

                String lastYearsFileItemLinkDataSql = "insert into fileItemLink(years," + columns + ")" +
                        "select " + years + " years, " + modifyColumns + " from fileItemLink k inner join filelist f on k.years=f.years " +
                        "and k.tableName=f.tableName and f.typeCode='" + tableCode + "'and f.tabletype='基本表' " +
                        "and f.tablename<>'' where k.years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsFileItemLinkDataSql);
            }


            /*----------------------------追加fileItemLinkex 数据---------------------------------*/
            String fileItemLinkEx = "select count(k.id) row_count from fileItemLinkex  k inner join filelist f on k.years=f.years " +
                    " and k.tableName=f.tableName and f.typeCode=? " +
                    " and f.tablename<>'' where k.years=?";

            List<Map<String, Object>> fileItemLinkExListMaps = jdbcTemplatePrimary.queryForList(fileItemLinkEx, tableCode, years);


            if(CollectionUtils.isEmpty(fileItemLinkExListMaps)
                    || (!CollectionUtils.isEmpty(fileItemLinkExListMaps)
                    && (Long)fileItemLinkExListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("fileItemLinkex", "years", "k", columns, modifyColumns);

                String lastYearsFileItemLinkExDataSql = "insert into fileItemLinkex(years, " + columns + ") " +
                        "select " + years + " years, " + modifyColumns + " from fileItemLinkex k inner join filelist f " +
                        "on k.years=f.years and k.tableName=f.tableName and f.typeCode='" + tableCode + "'  and f.tabletype='基本表' " +
                        "and isnull(f.tablename,'')<>'' where k.years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsFileItemLinkExDataSql);
            }



            /*----------------------------追加fileItemLinkexex 数据---------------------------------*/
            String fileItemLinkExEx = "select count(k.id) row_count from fileItemLinkExEx  k inner join filelist f on k.years=f.years " +
                    "and k.tableName=f.tableName and f.typeCode=? " +
                    "and ISNULL(f.tablename,'')<>'' where k.years=?";

            List<Map<String, Object>> fileItemLinkExExListMaps = jdbcTemplatePrimary.queryForList(fileItemLinkExEx, tableCode, years);


            if(CollectionUtils.isEmpty(fileItemLinkExExListMaps)
                    || (!CollectionUtils.isEmpty(fileItemLinkExExListMaps)
                    && (Long)fileItemLinkExExListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("fileItemLinkExEx", "years", "k", columns, modifyColumns);

                String lastYearsFileItemLinkExExDataSql = "insert into fileItemLinkExEx(years, " + columns + ") " +
                        "select " + years + " years, " + modifyColumns + " from fileItemLinkExEx k inner join filelist f " +
                        "on k.years=f.years and k.tableName=f.tableName and f.typeCode='" + tableCode + "'  " +
                        "and f.tabletype='基本表' and  ISNULL(f.tablename,'')<>''" +
                        "where k.years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsFileItemLinkExExDataSql);
            }


            /*----------------------------追加field_fomula 数据---------------------------------*/
            String fieldFormula = "select count(k.id) row_count from fileItemLinkExEx  k inner join filelist f on k.years=f.years " +
                    "and k.tableName=f.tableName and f.typeCode=? " +
                    "and ISNULL(f.tablename,'')<>'' where k.years=?";

            List<Map<String, Object>> fieldFormulaListMaps = jdbcTemplatePrimary.queryForList(fieldFormula, tableCode, years);

            if(CollectionUtils.isEmpty(fieldFormulaListMaps)
                    || (!CollectionUtils.isEmpty(fieldFormulaListMaps)
                    && (Long)fieldFormulaListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("field_fomula", "years", "k", columns, modifyColumns);

                String lastYearsFieldFormulaDataSql = "insert into field_fomula(years, " + columns + ") " +
                        "select " + years + " years, " + modifyColumns + " from field_fomula k inner join filelist f on k.years=f.years " +
                        "and k.tableName=f.tableName and f.typeCode='" + tableCode + "'  and f.tabletype='基本表' " +
                        "and  ISNULL(f.tablename,'')<>'' " +
                        "where  k.years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsFieldFormulaDataSql);
            }



            /*----------------------------追加lxorder 数据---------------------------------*/
            String lxOrder = "select count(id) row_count from lxorder where typeCode=? and years=?";

            List<Map<String, Object>> lxOrderListMaps = jdbcTemplatePrimary.queryForList(lxOrder, tableCode, years);

            if(CollectionUtils.isEmpty(lxOrderListMaps)
                    || (!CollectionUtils.isEmpty(lxOrderListMaps)
                    && (Long)lxOrderListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("lxorder", "years", "", columns, modifyColumns);

                String lastYearsLxOrderDataSql = "insert into lxorder(years, " + columns + ")" +
                        "select " + years + " as  years, " + modifyColumns + " from lxorder " +
                        "where typeCode='" + tableCode + "' and years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsLxOrderDataSql);
            }


            /*----------------------------追加middle_lxorder 数据---------------------------------*/
            String middleLxOrder = "select count(id) row_count from middle_lxorder where typeCode=? and years=?";

            List<Map<String, Object>> middleLxOrderListMaps = jdbcTemplatePrimary.queryForList(middleLxOrder, tableCode, years);

            if(CollectionUtils.isEmpty(middleLxOrderListMaps)
                    || (!CollectionUtils.isEmpty(middleLxOrderListMaps)
                    && (Long)middleLxOrderListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("middle_lxorder", "years", "", columns, modifyColumns);

                String lastYearsMiddleLxOrderDataSql = "insert into middle_lxorder(years, " + columns + ") " +
                        "select " + years + " as years, " + modifyColumns + " from middle_lxorder " +
                        "where typeCode='" + tableCode + "' and years=" + (years - 1);

                jdbcTemplatePrimary.update(lastYearsMiddleLxOrderDataSql);
            }



            /*----------------------------追加分析表公式 Ans_table---------------------------------*/
            String ansTable = "select count(sno) row_count from Ans_table a inner join filelist f on a.years=f.years " +
                    "and a.repno=f.ansNo and f.typeCode=? " +
                    "and tableType='分析表' and ISNULL(f.ansNo,0)<>0 where a.years=? and isnull(months,0)=0";

            List<Map<String, Object>> ansTableListMaps = jdbcTemplatePrimary.queryForList(ansTable, tableCode, years);

            if(CollectionUtils.isEmpty(ansTableListMaps)
                    || (!CollectionUtils.isEmpty(ansTableListMaps)
                    && (Long)ansTableListMaps.get(0).get("row_count") == 0)){

                columns.setLength(0);
                modifyColumns.setLength(0);
                findTableColumns("Ans_table", "years", "a", columns, modifyColumns);

                String lastYearsAnsTableDataSql = "insert into Ans_table( years, " + columns + ") " +
                        "select " + years + " as years, " + modifyColumns + " from Ans_table a inner join filelist f on a.years=f.years " +
                        "and a.repno=f.ansNo and f.typeCode='" + tableCode + "' " +
                        "and tableType='分析表'  and ISNULL(f.ansNo,0)<>0 where a.years= " + (years - 1) +
                        " and isnull(months,0)=0";

                jdbcTemplatePrimary.update(lastYearsAnsTableDataSql);
            }

        }


        /*----------------------------追加地区数据---------------------------------*/
        String dist = "select count(id) row_count from dist where years=?";

        List<Map<String, Object>> distListMaps = jdbcTemplatePrimary.queryForList(dist, years);

        if(CollectionUtils.isEmpty(distListMaps)
                || (!CollectionUtils.isEmpty(distListMaps)
                && (Long)distListMaps.get(0).get("row_count") == 0)){

            columns.setLength(0);
            modifyColumns.setLength(0);
            findTableColumns("dist", "years", "d", columns, modifyColumns);

            String lastYearsDistDataSql = "insert into  dist(years, " + columns + ") " +
                    "select " + years + " as years, " + modifyColumns + " from dist d left join (select distid from dist  " +
                    "where years=" + years + " and ISNULL(distId,'')<>'' " +
                    "and ISNULL(distName,'')<>'')dd on d.distId=dd.distId where d.years=" + (years - 1) + " " +
                    "and ISNULL(d.distId,'')<>'' and ISNULL(d.distName,'')<>'' and dd.distId is null";

            jdbcTemplatePrimary.update(lastYearsDistDataSql);
        }


        /*----------------------------追加扩展地区数据---------------------------------*/
        String distEx = "select count(id) row_count from distEx where years=?";

        List<Map<String, Object>> distExListMaps = jdbcTemplatePrimary.queryForList(distEx, years);

        if(CollectionUtils.isEmpty(distExListMaps)
                || (!CollectionUtils.isEmpty(distExListMaps)
                && (Long)distExListMaps.get(0).get("row_count") == 0)){

            columns.setLength(0);
            modifyColumns.setLength(0);
            findTableColumns("distEx", "years", "d", columns, modifyColumns);

            String lastYearsDistExDataSql = "insert into  distEx(years, " + columns + ") " +
                    "select " + years + " as years, " + modifyColumns + " from distEx d  where d.years=" + (years - 1);

            jdbcTemplatePrimary.update(lastYearsDistExDataSql);
        }


        return true;
    }

    /**
     * 生成空的数据表数据
     * @param createEmptyTableDTO
     * @return
     */
    public Boolean createDataEmptyTable(CreateEmptyTableDTO createEmptyTableDTO){
        return false;
    }

    /**
     *
     * @param tableName
     * @param excludeColumns
     * @param tableAlias
     * @param columns
     * @param modifyColumn
     */
    public void findTableColumns(String tableName, String excludeColumns, String tableAlias, StringBuilder columns, StringBuilder modifyColumn){


        //获取当前表的所有字段
        String tableFieldsSql = "  select column_name from all_tab_columns where table_name = ? " +
                "AND OWNER = (SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID())) " +
                "AND column_name not in (select column_name from all_cons_columns where table_name = ? and owner = (SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID()))) " +
                "AND column_name not in  " +
                "  (select regexp_substr(?,'[^,]+',1,level,'i') as tempcol from dual connect by level <= LENGTH(TRANSLATE(?,','||?,','))+1)";


        //查询出指定表排除指定字段后的所有字段
        List<Map<String, Object>> tableFieldsMaps
                = jdbcTemplatePrimary.queryForList(tableFieldsSql, new Object[]{tableName, tableName, excludeColumns, excludeColumns, excludeColumns});
        List<String> tableFieldsList = tableFieldsMaps.stream().map(e -> e.get("column_name").toString()).collect(Collectors.toList());

        //如果表没有查询出字段, 则表不存在或者其他情况
        if(CollectionUtils.isEmpty(tableFieldsList)){
            throw new RuntimeException("不存在表：" + tableName);
        }

        columns.append(tableFieldsList.stream().collect(Collectors.joining(", ")));

        modifyColumn.append(tableFieldsList.stream().map(e -> {
            if (StringUtils.isNotBlank(tableAlias)){
                return tableAlias + "." + e;
            }
            return e;
        }).collect(Collectors.joining(", ")));

    }


}
