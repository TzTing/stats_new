package com.bright.common.config;

import com.alibaba.fastjson.JSON;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.po.primary.OperationLog;
import com.bright.stats.repository.primary.OperationLogRepository;
import com.bright.stats.util.DateUtil;
import com.bright.stats.util.SessionUnit;
import lombok.SneakyThrows;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.*;
import org.hibernate.internal.SessionFactoryImpl;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.type.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManagerFactory;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class MyEventListener extends SQLPropertyValues implements PreInsertEventListener, PreDeleteEventListener, PreUpdateEventListener {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    private OperationLogRepository operationLogRepository;


    @PostConstruct
    private void init() {
        SessionFactoryImpl sessionFactory = entityManagerFactory.unwrap(SessionFactoryImpl.class);
        EventListenerRegistry registry = sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
        registry.getEventListenerGroup(EventType.PRE_INSERT).appendListener(this);
        registry.getEventListenerGroup(EventType.PRE_DELETE).appendListener(this);
        registry.getEventListenerGroup(EventType.PRE_UPDATE).appendListener(this);
    }

    @SneakyThrows
    @Override
    public boolean onPreInsert(PreInsertEvent preInsertEvent) {

        /*==========数据库日志=========*/
        OperationLog operationLog = new OperationLog();
        Long beginTime = System.currentTimeMillis();
        ByteArrayOutputStream messageDetail = null;

        //如果是添加的日志本身的实体  则不处理
        if (preInsertEvent.getEntity() instanceof OperationLog) {
            return false;
        }
        try {

            operationLog.setOpeBeginDate(DateUtil.getCurrDate());
            operationLog.setOpeStats(true);

            AbstractEntityPersister ep = (AbstractEntityPersister) preInsertEvent.getPersister();
            //主键
//            ep.getSQLInsertStrings()
            Type idType = ep.getIdentifierType();
            String[] keyColumns = ep.getRootTableKeyColumnNames();
            Object[] keyValues = null;

            try {
                keyValues = getPkValues(idType, preInsertEvent.getId(), ep.getFactory());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            //其他列
            String[] propertys = ep.getPropertyNames();
            List<String> columnNamesList = new ArrayList<String>();
            for (String property : propertys) {
                String[] columns = ep.getPropertyColumnNames(property);
                for (String column : columns) {
                    columnNamesList.add(column);
                }
            }
            Object[] values = getValues(preInsertEvent.getState(), ep.getPropertyTypes(), ep.getFactory());
//            Object[] values = null;
            String table = ep.getRootTableName();
            String[] columnArr = new String[columnNamesList.size()];
            SQLConvertor convertor = SQLConvertor.newInstance(keyColumns, keyValues, columnNamesList.toArray(columnArr), values, table);
            String sql = convertor.toInsertSQL();

            String params = JSON.toJSONString(preInsertEvent.getEntity());

            operationLog.setOpeSql(sql);
            operationLog.setOpeParam(params);


            if(RequestContextHolder.getRequestAttributes() == null){
                String uri = "异步处理";
                operationLog.setReqUri(uri);
                operationLog.setOpeBeginDate(DateUtil.getCurrDate());

            }else{
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String uri = request.getRequestURI();
//                String ip = request.getRemoteAddr();
                String ip = SessionUnit.realIpAddr(request);
                String method = request.getMethod();

                // 读取session中的用户
                String username = SecurityUtil.getLoginUser().getUsername();

                operationLog.setOpeBeginDate(DateUtil.getCurrDate());
                operationLog.setReqUri(uri);
                operationLog.setReqType(method);
                operationLog.setOpeType("DAO");
                operationLog.setOpeIp(ip);
                operationLog.setOpeUser(username);
            }

            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLogRepository.save(operationLog);

        } catch (Throwable e) {
            operationLog.setOpeStats(false);
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            messageDetail = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(messageDetail));
            String exception = messageDetail.toString();
            operationLog.setOpeErrorMessage(exception);
            operationLogRepository.save(operationLog);
            messageDetail.close();
        } finally {
            if(!Objects.isNull(messageDetail)){
                messageDetail.close();
            }
        }


        return false;
    }

    /**
     * Return true if the operation should be vetoed
     *
     * @param preDeleteEvent
     */
    @SneakyThrows
    @Override
    public boolean onPreDelete(PreDeleteEvent preDeleteEvent) {

        /*==========数据库日志=========*/
        OperationLog operationLog = new OperationLog();
        Long beginTime = System.currentTimeMillis();
        ByteArrayOutputStream messageDetail = null;

        //如果是添加的日志本身的实体  则不处理
        if (preDeleteEvent.getEntity() instanceof OperationLog) {
            return false;
        }
        try {

            operationLog.setOpeBeginDate(DateUtil.getCurrDate());
            operationLog.setOpeStats(true);

            AbstractEntityPersister ep = (AbstractEntityPersister) preDeleteEvent.getPersister();
            //主键
//            ep.getSQLInsertStrings()
            Type idType = ep.getIdentifierType();
            String[] keyColumns = ep.getRootTableKeyColumnNames();
            Object[] keyValues = null;

            try {
                keyValues = getPkValues(idType, preDeleteEvent.getId(), ep.getFactory());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            //其他列
            String[] propertys = ep.getPropertyNames();
            List<String> columnNamesList = new ArrayList<String>();
            for (String property : propertys) {
                String[] columns = ep.getPropertyColumnNames(property);
                for (String column : columns) {
                    columnNamesList.add(column);
                }
            }
            Object[] values = getValues(preDeleteEvent.getDeletedState(), ep.getPropertyTypes(), ep.getFactory());
//            Object[] values = null;
            String table = ep.getRootTableName();
            String[] columnArr = new String[columnNamesList.size()];
            SQLConvertor convertor = SQLConvertor.newInstance(keyColumns, keyValues, columnNamesList.toArray(columnArr), values, table);
            String sql = convertor.toDeleteSQL();

            String params = JSON.toJSONString(preDeleteEvent.getEntity());

            operationLog.setOpeSql(sql);
            operationLog.setOpeParam(params);


            if(RequestContextHolder.getRequestAttributes() == null){
                String uri = "异步处理";
                operationLog.setReqUri(uri);
                operationLog.setOpeBeginDate(DateUtil.getCurrDate());

            }else{
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String uri = request.getRequestURI();
//                String ip = request.getRemoteAddr();
                String ip = SessionUnit.realIpAddr(request);
                String method = request.getMethod();

                // 读取session中的用户
                String username = SecurityUtil.getLoginUser().getUsername();

                operationLog.setOpeBeginDate(DateUtil.getCurrDate());
                operationLog.setReqUri(uri);
                operationLog.setReqType(method);
                operationLog.setOpeType("DAO");
                operationLog.setOpeIp(ip);
                operationLog.setOpeUser(username);
            }
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLogRepository.save(operationLog);

        } catch (Throwable e) {
            operationLog.setOpeStats(false);
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            messageDetail = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(messageDetail));
            String exception = messageDetail.toString();
            operationLog.setOpeErrorMessage(exception);
            operationLogRepository.save(operationLog);
            messageDetail.close();
        } finally {
            if(!Objects.isNull(messageDetail)){
                messageDetail.close();
            }
        }


        return false;
    }

    /**
     * Return true if the operation should be vetoed
     *
     * @param preUpdateEvent
     */
    @SneakyThrows
    @Override
    public boolean onPreUpdate(PreUpdateEvent preUpdateEvent) {
        /*==========数据库日志=========*/
        OperationLog operationLog = new OperationLog();
        Long beginTime = System.currentTimeMillis();
        ByteArrayOutputStream messageDetail = null;

        //如果是添加的日志本身的实体  则不处理
        if (preUpdateEvent.getEntity() instanceof OperationLog) {
            return false;
        }
        try {

            operationLog.setOpeBeginDate(DateUtil.getCurrDate());
            operationLog.setOpeStats(true);

            AbstractEntityPersister ep = (AbstractEntityPersister) preUpdateEvent.getPersister();
            //主键
//            ep.getSQLInsertStrings()
            Type idType = ep.getIdentifierType();
            String[] keyColumns = ep.getRootTableKeyColumnNames();
            Object[] keyValues = null;

            try {
                keyValues = getPkValues(idType, preUpdateEvent.getId(), ep.getFactory());
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
            //其他列
            String[] propertys = ep.getPropertyNames();
            List<String> columnNamesList = new ArrayList<String>();
            for (String property : propertys) {
                String[] columns = ep.getPropertyColumnNames(property);
                for (String column : columns) {
                    columnNamesList.add(column);
                }
            }
            Object[] values = getValues(preUpdateEvent.getState(), ep.getPropertyTypes(), ep.getFactory());
//            Object[] values = null;
            String table = ep.getRootTableName();
            String[] columnArr = new String[columnNamesList.size()];
            SQLConvertor convertor = SQLConvertor.newInstance(keyColumns, keyValues, columnNamesList.toArray(columnArr), values, table);
            String sql = convertor.toUpdateSQL();

            String params = JSON.toJSONString(preUpdateEvent.getEntity());

            operationLog.setOpeSql(sql);
            operationLog.setOpeParam(params);


            if(RequestContextHolder.getRequestAttributes() == null){
                String uri = "异步处理";
                operationLog.setReqUri(uri);
                operationLog.setOpeBeginDate(DateUtil.getCurrDate());

            }else{
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String uri = request.getRequestURI();
//                String ip = request.getRemoteAddr();
                String ip = SessionUnit.realIpAddr(request);
                String method = request.getMethod();

                // 读取session中的用户
                String username = SecurityUtil.getLoginUser().getUsername();

                operationLog.setOpeBeginDate(DateUtil.getCurrDate());
                operationLog.setReqUri(uri);
                operationLog.setReqType(method);
                operationLog.setOpeType("DAO");
                operationLog.setOpeIp(ip);
                operationLog.setOpeUser(username);
            }

            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLogRepository.save(operationLog);

        } catch (Exception e) {
            operationLog.setOpeStats(false);
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            messageDetail = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(messageDetail));
            String exception = messageDetail.toString();
            operationLog.setOpeErrorMessage(exception);
            operationLogRepository.save(operationLog);
            messageDetail.close();
        } finally {
            if(!Objects.isNull(messageDetail)){
                messageDetail.close();
            }
        }


        return false;
    }
}