package com.bright.stats.service.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.security.SecurityUser;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.manager.MqMessageManager;
import com.bright.stats.manager.NavigateManager;
import com.bright.stats.manager.TableTypeManager;
import com.bright.stats.pojo.dto.CreateEmptyTableDTO;
import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.query.MqMessagesQuery;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.service.NavigateService;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;

import javax.persistence.criteria.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
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

        return null;
    }

    /**
     * 生成空的配置表数据
     * @param tableType
     * @param years
     * @return
     */
    public Boolean createConfigEmptyTable(String tableType, Integer years){
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        String tableCode = "";
        String columns = "";
        String modifyColumns = "";
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
            List<FileList> fileLists
                    = fileListManager.listFileListsOnly(tableCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, 0);

            //如果当前年份没有查询出数据
            if(CollectionUtils.isEmpty(fileLists)){
                //需要插入上一年的空数据

            }
        }


        return false;
    }

    /**
     * 生成空的数据表数据
     * @param createEmptyTableDTO
     * @return
     */
    public Boolean createDataEmptyTable(CreateEmptyTableDTO createEmptyTableDTO){
        return false;
    }

    public void findTableColumns(String tableName, String excludeColumns, String tableAlias, String columns, String modifyColumn){

    }


}
