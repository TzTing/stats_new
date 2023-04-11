package com.bright.stats.manager.impl;

import com.alibaba.fastjson2.JSON;
import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.*;
import com.bright.stats.pojo.dto.SumBaseDataDTO;
import com.bright.stats.pojo.dto.SummaryDTO;
import com.bright.stats.pojo.dto.TableDataDTO;
import com.bright.stats.pojo.model.ExcelTemplateInfo;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.BaseDataQuery;
import com.bright.stats.pojo.query.ExistDataQuery;
import com.bright.stats.pojo.vo.CheckVO;
import com.bright.stats.pojo.vo.ImportExcelVO;
import com.bright.stats.pojo.vo.SummaryVO;
import com.bright.stats.util.*;
import com.sun.org.apache.xpath.internal.operations.Bool;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.excelutils.ExcelException;
import net.sf.excelutils.WorkbookUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.*;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.*;
import java.io.File;
import java.math.BigDecimal;
import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Author txf
 * @Date 2022/8/1 9:33
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseDataManagerImpl implements BaseDataManager {

    @PersistenceContext
    private EntityManager entityManagerPrimary;

    private final JdbcTemplate jdbcTemplatePrimary;
    private final FileListManager fileListManager;
    private final DistManager distManager;
    private final DistExManager distExManager;
    private final BsConfigManager bsConfigManager;
    private final DataProcessNewManager dataProcessNewManager;
    private final ExcelTemplateManager excelTemplateManager;
    private final TabInLimitLxManager tabInLimitLxManager;
    private final LxOrderManager lxOrderManager;

    @Override
    public <T> T listTableData(BaseDataQuery baseDataQuery, boolean isPage, Class<T> typeClass) {
        Integer years = baseDataQuery.getYears();
        Integer months = baseDataQuery.getMonths();
        String tableName = baseDataQuery.getTableName();
        String lx = baseDataQuery.getLx();
        String lxName = baseDataQuery.getLxName();
        Integer grade = baseDataQuery.getGrade();
        Boolean isGrade = baseDataQuery.getIsGrade();
        Boolean isBalanced = baseDataQuery.getIsBalanced();
        String distNo = baseDataQuery.getDistNo();
        String typeCode = baseDataQuery.getTypeCode();
        List<Condition> conditions = baseDataQuery.getConditions();
        Integer pageNumber = baseDataQuery.getPageNumber();
        Integer pageSize = baseDataQuery.getPageSize();
        List<String> sorts = baseDataQuery.getSorts();
        String userDistNo = baseDataQuery.getUserDistNo();

        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        List<FileItem> fileItems = fileList.getFileItems();
        List<String> fileItemCollect = fileItems.stream().map(fileItem -> fileItem.getFieldName() + " as " + fileItem.getFieldName()).collect(Collectors.toList());
        fileItemCollect.add("id as id");
        if(!fileItemCollect.contains("balflag as balflag")){
            fileItemCollect.add("balflag as balflag");
        }
        if(!fileItemCollect.contains("saveflag as saveflag")){
            fileItemCollect.add("saveflag as saveflag");
        }
        if(!fileItemCollect.contains("sumflag as sumflag")){
            fileItemCollect.add("sumflag as sumflag");
        }
        String filed = String.join(", ", fileItemCollect);
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select ").append(filed).append(" from ").append(fileList.getTableName());
        StringBuffer sqlWhereStringBuffer = new StringBuffer(" where 1=1 ");
        Map<String, Object> parameterMap = new LinkedHashMap<>(16);

        sqlWhereStringBuffer.append(" and distId like :distNo ");
        if (distNo.equals("0")) {
            parameterMap.put("distNo", "%");
        } else {
            parameterMap.put("distNo", distNo + "%");
        }

        sqlWhereStringBuffer.append(" and years=:years ");
        parameterMap.put("years", years);

        if (!StringUtils.isEmpty(lx) && !lx.equals("全部")) {
            //如果是多选的情况下 用in
            if(lx.contains(",")){
                sqlWhereStringBuffer.append(" and lx in (:lx) ");
                List<String> lxs = Arrays.asList(lx.split(","));
                parameterMap.put("lx", lxs);
            } else {
                sqlWhereStringBuffer.append(" and lx=:lx ");
                parameterMap.put("lx", lx);
            }
        }

        if (!StringUtils.isEmpty(lxName) && !lxName.equals("全部")) {
            sqlWhereStringBuffer.append(" and lxName=:lxName ");
            parameterMap.put("lxName", lxName);
        }

        if (null != grade) {
            sqlWhereStringBuffer.append(" and len(distId)<=:grade ");
            parameterMap.put("grade", DataConstants.getMaxDistNoLength(distNo, grade));
        }

        if (Objects.nonNull(isGrade) && !isGrade) {
            sqlWhereStringBuffer.append(" and len(distId)=:isGrade ");
            parameterMap.put("isGrade", DataConstants.getMaxDistNoLength(distNo, grade));
        }

        if (Objects.nonNull(isBalanced) && !isBalanced) {
            sqlWhereStringBuffer.append(" and balFlag='否' ");
        }

        List<Condition> parameterConditions = new ArrayList<>();

        if (!CollectionUtils.isEmpty(conditions)) {
            for (Condition condition : conditions) {
                for (FileItem fileItem : fileItems) {
                    if (condition.getFieldName().equalsIgnoreCase(fileItem.getFieldName())) {
                        condition.setFieldType(fileItem.getFType().toUpperCase());
                        parameterConditions.add(condition);
                    }
                }
            }
        }

        Map<String, Object> sqlCondition = this.getSqlCondition(parameterConditions);

        sqlWhereStringBuffer.append(sqlCondition.get("sqlParameterCondition"));
        parameterMap.putAll((Map<String, Object>) sqlCondition.get("parameterMap"));
        sqlStringBuffer.append(sqlWhereStringBuffer);

        StringBuffer sqlOrderByStringBuffer = new StringBuffer(" order by id ");

        if (!CollectionUtils.isEmpty(sorts)) {
//            sqlOrderByStringBuffer.append(", ");
            sqlOrderByStringBuffer.setLength(0);
            sqlOrderByStringBuffer.append(" order by ");
            for (int i = 0; i < sorts.size(); i++) {
                if (sorts.get(i).indexOf(",") == -1) {
                    if (i == sorts.size() - 1) {
                        sqlOrderByStringBuffer.append(sorts.get(i));
                    } else {
                        sqlOrderByStringBuffer.append(sorts.get(i) + ", ");
                    }

                } else {
                    String sortString = sorts.get(i);
                    sortString = sortString.replace(",", " ");
                    if (i == sorts.size() - 1) {
                        sqlOrderByStringBuffer.append(sortString);
                    } else {
                        sqlOrderByStringBuffer.append(sortString + ", ");
                    }
                }
            }
        } else {
            sqlOrderByStringBuffer.setLength(0);

            sqlOrderByStringBuffer.append(fileList.getOrderStr());
            String sqlOrder = fileList.getOrderStr();

            if(org.apache.commons.lang.StringUtils.isEmpty(org.apache.commons.lang.StringUtils.trimToEmpty(sqlOrder))) {

                sqlOrder = "order by years,distId" + (fileList.getFileItemLink() != null ? ",lxid" : "");
                sqlOrder += ", distid";

                sqlOrder += ", ztid, id, sumflag";

            }

            sqlOrderByStringBuffer.append(sqlOrder);
        }

        if (Objects.nonNull(pageNumber) && Objects.nonNull(pageSize) && isPage) {


            if("rep905".equalsIgnoreCase(fileList.getTableName()) || "rep906".equalsIgnoreCase(fileList.getTableName())){


//                List<String> fileItemCollect2 =
//                        fileItems.stream().map(fileItem -> "t1." + fileItem.getFieldName() + " as " + fileItem.getFieldName()).collect(Collectors.toList());

                StringBuffer sqlStringBufferOther = new StringBuffer();
                sqlStringBufferOther.append("select ")
//                        .append(String.join(", ", fileItemCollect2))
                        .append(" t1.* ")
                        .append(" from ( select * ")
                        .append(" from ").append(fileList.getTableName()).append(sqlWhereStringBuffer).append(" ) as t1 ")
                        .append(" left join ( select * from distEx where 1 = 1 and distid like '")
                        .append("0".equalsIgnoreCase(distNo) ? "%" : distNo + "%")
                        .append("'")
                        .append(" and tablename = '")
                        .append(fileList.getTableName())
                        .append("'")
                        .append(" and years = ")
                        .append(years)
                        .append(") as t2 on t1.ztid = t2.ztid order by ")
                        .append("isnull(t2.parent_id, t2.ztid), t2.ztTypeId");


                if(CollectionUtils.isEmpty(sorts)) {
                    String sqlOrder = fileList.getOrderStr();

                    if(StringUtils.isEmpty(StringUtils.trimToEmpty(sqlOrder))) {
                        sqlOrder = " ,t1.years, t1.distId" + (fileList.getFileItemLink() != null ? ",t1.lxid" : "");
                        sqlOrder += ", t1.distid";

                        sqlOrder += ", t1.ztid, t1.id, t1.sumflag";
                        sqlStringBufferOther.append(sqlOrder);
                    }
                } else {
                    sqlStringBufferOther.append(",");
                    for (int i = 0; i < sorts.size(); i++) {
                        if (sorts.get(i).indexOf(",") == -1) {
                            if (i == sorts.size() - 1) {
                                sqlStringBufferOther.append(sorts.get(i));
                            } else {
                                sqlStringBufferOther.append(sorts.get(i) + ", ");
                            }

                        } else {
                            String sortString = sorts.get(i);
                            sortString = sortString.replace(",", " ");
                            if (i == sorts.size() - 1) {
                                sqlStringBufferOther.append(sortString);
                            } else {
                                sqlStringBufferOther.append(sortString + ", ");
                            }
                        }
                    }

                }


                String sql = sqlStringBufferOther.toString();

                for (String parameterKey : parameterMap.keySet()) {
                    if(sqlValidate(parameterMap.get(parameterKey).toString())){
                        throw new RuntimeException("非法请求参数!");
                    }
                    if(parameterMap.get(parameterKey) instanceof Number){
                        sql = sql.replace(":" + parameterKey, parameterMap.get(parameterKey).toString());
                    } else {
                        String lxTemp = "";
                        //如果是替换'lx'字段的值 且lx是多选的情况下 拼接的语句是in 要做处理
                        if(parameterKey.equalsIgnoreCase("lx") && lx.contains(",")){
                            lxTemp = Arrays.asList(lx.split(",")).stream().map(e -> "'" + e + "'").collect(Collectors.joining(","));
                        }
                        if(StringUtils.isBlank(lxTemp)){
                            sql = sql.replace(":" + parameterKey, "'" + parameterMap.get(parameterKey).toString() + "'");
                        } else {
                            sql = sql.replace(":" + parameterKey, lxTemp);
                        }
                    }
                }

//                sql += " limit " + (pageNumber * pageSize) + ", " + pageSize;

//                Object[] params = parameterMap.keySet().stream().map(e -> parameterMap.get(e)).toArray(Object[]::new);

                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(sql);
//                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(sql, params);

                Long count = Long.valueOf(list.size());

                list = list.stream().skip(pageNumber * pageSize).limit(pageSize).collect(Collectors.toList());

                return (T) PageResult.of(count, list);
            }


            StringBuffer sqlCountStringBuffer = new StringBuffer("select count(*) from ");
            sqlCountStringBuffer.append(fileList.getTableName());
            sqlCountStringBuffer.append(sqlWhereStringBuffer);
            Query nativeQueryCount = entityManagerPrimary.createNativeQuery(sqlCountStringBuffer.toString());
            for (String parameterKey : parameterMap.keySet()) {
                nativeQueryCount.setParameter(parameterKey, parameterMap.get(parameterKey));
            }
            Long counts = Long.valueOf(nativeQueryCount.getSingleResult().toString());

            sqlStringBuffer.append(sqlOrderByStringBuffer);
            Query nativeQuery = entityManagerPrimary.createNativeQuery(sqlStringBuffer.toString());

            for (String parameterKey : parameterMap.keySet()) {
                nativeQuery.setParameter(parameterKey, parameterMap.get(parameterKey));
            }
            nativeQuery.setFirstResult(pageNumber * pageSize);
            nativeQuery.setMaxResults(pageSize);

            nativeQuery.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map<String, Object>> resultList = nativeQuery.getResultList();
            return (T) PageResult.of(counts, resultList);
        }


        if("rep905".equalsIgnoreCase(fileList.getTableName()) || "rep906".equalsIgnoreCase(fileList.getTableName())){
            StringBuffer sqlStringBufferOther = new StringBuffer();
            sqlStringBufferOther.append("select t1.* from ( select * ")
                    .append(" from ").append(fileList.getTableName()).append(sqlWhereStringBuffer).append(" ) as t1 ")
                    .append(" left join ( select * from distEx where 1 = 1 and distid like '")
                    .append("0".equalsIgnoreCase(distNo) ? "%" : distNo + "%")
                    .append("'")
                    .append(" and tablename = '")
                    .append(fileList.getTableName())
                    .append("'")
                    .append(" and years = ")
                    .append(years)
                    .append(") as t2 on t1.ztid = t2.ztid order by ")
                    .append("isnull(t2.parent_id, t2.ztid), t2.ztTypeId");


            if(CollectionUtils.isEmpty(sorts)) {
                String sqlOrder = fileList.getOrderStr();

                if(StringUtils.isEmpty(StringUtils.trimToEmpty(sqlOrder))) {
                    sqlOrder = " ,t1.years, t1.distId" + (fileList.getFileItemLink() != null ? ",t1.lxid" : "");
                    sqlOrder += ", t1.ztid, t1.id, t1.sumflag";
                    sqlStringBufferOther.append(sqlOrder);
                }
            } else {
                sqlStringBufferOther.append(",");
                for (int i = 0; i < sorts.size(); i++) {
                    if (sorts.get(i).indexOf(",") == -1) {
                        if (i == sorts.size() - 1) {
                            sqlStringBufferOther.append(sorts.get(i));
                        } else {
                            sqlStringBufferOther.append(sorts.get(i) + ", ");
                        }

                    } else {
                        String sortString = sorts.get(i);
                        sortString = sortString.replace(",", " ");
                        if (i == sorts.size() - 1) {
                            sqlStringBufferOther.append(sortString);
                        } else {
                            sqlStringBufferOther.append(sortString + ", ");
                        }
                    }
                }

            }

            String sql = sqlStringBufferOther.toString();

            for (String parameterKey : parameterMap.keySet()) {

                if(sqlValidate(parameterMap.get(parameterKey).toString())){
                    throw new RuntimeException("非法请求参数!");
                }

                if(parameterMap.get(parameterKey) instanceof Number){
                    sql = sql.replace(":" + parameterKey, parameterMap.get(parameterKey).toString());
                } else {
                    String lxTemp = "";
                    //如果是替换'lx'字段的值 且lx是多选的情况下 拼接的语句是in 要做处理
                    if(parameterKey.equalsIgnoreCase("lx") && lx.contains(",")){
                        lxTemp = Arrays.asList(lx.split(",")).stream().map(e -> "'" + e + "'").collect(Collectors.joining(","));
                    }
                    if(StringUtils.isBlank(lxTemp)){
                        sql = sql.replace(":" + parameterKey, "'" + parameterMap.get(parameterKey).toString() + "'");
                    } else {
                        sql = sql.replace(":" + parameterKey, lxTemp);
                    }
                }
            }

            List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(sql);

            return (T) list;

            /*Query nativeQuery = entityManagerPrimary.createNativeQuery(sqlStringBufferOther.toString());
            for (String parameterKey : parameterMap.keySet()) {
                nativeQuery.setParameter(parameterKey, parameterMap.get(parameterKey));
            }
            nativeQuery.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            List<Map<String, Object>> resultList = nativeQuery.getResultList();
            return (T) resultList;*/
        }




        sqlStringBuffer.append(sqlOrderByStringBuffer);
        Query nativeQuery = entityManagerPrimary.createNativeQuery(sqlStringBuffer.toString());
        for (String parameterKey : parameterMap.keySet()) {
            nativeQuery.setParameter(parameterKey, parameterMap.get(parameterKey));
        }
        nativeQuery.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List<Map<String, Object>> resultList = nativeQuery.getResultList();
        return (T) resultList;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveTableData(TableDataDTO tableDataDTO) {
        Integer years = tableDataDTO.getYears();
        Integer months = tableDataDTO.getMonths();
        String tableName = tableDataDTO.getTableName();
        String typeCode = tableDataDTO.getTypeCode();

        JSONArray insertData = JSONArray.fromObject(tableDataDTO.getInsertData());
        JSONArray updateData = JSONArray.fromObject(tableDataDTO.getUpdateData());
        JSONArray deleteData = JSONArray.fromObject(tableDataDTO.getDeleteData());

        User loginUser = SecurityUtil.getLoginUser();
        String userDistNo = loginUser.getTjDistNo();
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        final List<FileItem> fileItems = fileList.getFileItems();

        String sql = null, insertSqlHead = "INSERT INTO " + fileList.getTableName(),
                insertSqlEnd = "VALUES",
                updateSqlHead = "UPDATE " + fileList.getTableName(),
                updateSqlWhere = "WHERE id=?";
        Object[] insertParameters = null, updateParameters = null;

        String otherSql = "UPDATE " + fileList.getTableName() + " set balflag='否' where id=?";
        String statusSql = " update  " + fileList.getTableName() + " set  statusno=case when balflag='是' then 2 when saveflag='是' then 3 else 1 end where id=?";

        //sql
        for (int i = 0; i < fileItems.size(); i++) {
            if (i == 0) {
                insertSqlHead += "(";
                insertSqlEnd += "(";

                updateSqlHead += " SET ";
            } else {
                insertSqlHead += ", ";
                insertSqlEnd += ", ";

                updateSqlHead += ", ";
            }

            insertSqlHead += fileItems.get(i).getFieldName().toUpperCase();
            insertSqlEnd += "?";

            updateSqlHead += fileItems.get(i).getFieldName().toUpperCase() + "=?";

            if (i == fileItems.size() - 1) {
                insertSqlHead += ", ZTID";
                insertSqlEnd += ", ?";

                insertSqlHead += ")";
                insertSqlEnd += ")";
            }
        }

        //JSONArray data = new JSONArray();
        //for(int i=0;i<insertData.size();i++) data.add(insertData.get(i));
        //for(int i=0;i<updateData.size();i++) data.add(updateData.get(i));

        //insert
        Integer id = null;
        for (int i = 0; i < insertData.size(); i++) {
            JSONObject jo = (JSONObject) insertData.get(i);

            id = (Integer) jo.get("ID");

            insertParameters = new Object[fileItems.size()];
            updateParameters = new Object[fileItems.size() + 1];

            for (int j = 0; j < fileItems.size(); j++) {
                insertParameters[j] = jo.get(fileItems.get(j).getFieldName().toUpperCase());
                updateParameters[j] = jo.get(fileItems.get(j).getFieldName().toUpperCase());
            }
            updateParameters[fileItems.size()] = id;


            //sql = "SELECT id FROM " + fileList.getTableName() + " WHERE id=?";
            //boolean isIns = jdbcTemplate.queryForListList(sql, new Object[]{id}).size() == 0;
            boolean isIns = true; //insertData.size() <= i+1;
            if (isIns) {
                sql = insertSqlHead + " " + insertSqlEnd;
                List<Object> resultList = new ArrayList<Object>(Arrays.asList(insertParameters));
                resultList.add(jo.get("ZTID"));

                final String insSql = sql;
                final Object[] parameters = resultList.toArray();
                KeyHolder keyHolder = new GeneratedKeyHolder();
                jdbcTemplatePrimary.update(new PreparedStatementCreator() {
                    public PreparedStatement createPreparedStatement(Connection arg0)
                            throws SQLException {
                        PreparedStatement ps = arg0.prepareStatement(insSql, Statement.RETURN_GENERATED_KEYS);
                        for (int i = 0; i < parameters.length; i++) {
                            ps.setObject(i + 1, (parameters[i] == null || StringUtils.equals(parameters[i].toString(), "null")) ? "" : parameters[i]);
                        }
                        return ps;
                    }

                }, keyHolder);
                Long generatedId = keyHolder.getKey().longValue();
                id = generatedId.intValue();


                sql = "UPDATE " + fileList.getTableName() + " SET years=?, distId=?, distName=?, gradeId=?, " +
                        "balFlag=?, saveFlag=?, sumFlag=? WHERE id=?";
                insertParameters = new Object[]{jo.get("YEARS"), jo.get("DISTID"), jo.get("DISTNAME"), //jo.get("DISID"),
                        jo.get("GRADEID"), jo.get("BALFLAG"), jo.get("SAVEFLAG"), jo.get("SUMFLAG"), id};
                jdbcTemplatePrimary.update(sql, insertParameters, new int[]{Types.INTEGER, Types.VARCHAR, Types.VARCHAR, Types.VARCHAR,
                        Types.VARCHAR, Types.VARCHAR, Types.VARCHAR, Types.INTEGER});


                if (jo.get("LXID") != null && (!(jo.get("LXID") instanceof JSONObject) || !((JSONObject) jo.get("LXID")).isNullObject())) {
                    sql = "UPDATE " + fileList.getTableName() + " SET lxId=? WHERE id=?";
                    jdbcTemplatePrimary.update(sql, new Object[]{jo.get("LXID"), id});
                }

                if (jo.get("LXNAME") != null && (!(jo.get("LXNAME") instanceof JSONObject) || !((JSONObject) jo.get("LXNAME")).isNullObject())) {
                    sql = "UPDATE " + fileList.getTableName() + " SET LXNAME=? WHERE id=?";
                    jdbcTemplatePrimary.update(sql, new Object[]{jo.get("LXNAME"), id});
                }

                if (jo.get("MONTHS") != null && (!(jo.get("MONTHS") instanceof JSONObject) || !((JSONObject) jo.get("MONTHS")).isNullObject())) {
                    sql = "UPDATE " + fileList.getTableName() + " SET months=? WHERE id=?";
                    jdbcTemplatePrimary.update(sql, new Object[]{jo.get("MONTHS"), id});
                }
            } else {
                sql = updateSqlHead + " " + updateSqlWhere;
                jdbcTemplatePrimary.update(sql, updateParameters);
            }
        }

        //update

        final JSONArray tempUpdateData = updateData;
        sql = updateSqlHead + " " + updateSqlWhere;
        jdbcTemplatePrimary.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return tempUpdateData.size();
            }

            public void setValues(PreparedStatement arg0, int arg1) throws SQLException {
                JSONObject jo = (JSONObject) tempUpdateData.get(arg1);


                Object[] parameters = new Object[fileItems.size() + 1];
                for (int j = 0; j < fileItems.size(); j++) {
                    if (StringUtils.equalsIgnoreCase(fileItems.get(j).getFieldName(), "balflag")) {
                        parameters[j] = "否";
                    } else {
                        parameters[j] = jo.get(fileItems.get(j).getFieldName().toUpperCase());
                    }

                }
                parameters[fileItems.size()] = jo.getInt("ID");


                for (int i = 0; i < parameters.length; i++) {
                    arg0.setObject(i + 1, ((parameters[i] == null || StringUtils.equals(parameters[i].toString(), "null"))) ? "" : parameters[i]);
                }
            }
        });

        //otherSql
//        List<Map<Object, Object>> listMap = ListJsonUtil.JsonArrayToListMap(tempUpdateData);

        List<Map<Object, Object>> listMap = new ArrayList<>();
        for (Object obj : tempUpdateData) {
            if (obj instanceof JSONObject) {
                JSONObject json = (JSONObject) obj;
                Map<Object, Object> param = new HashMap<>(16);
                if (json == null) {
                    listMap.add(null);
                } else {
                    Iterator<Object> iterator = json.keys();
                    while (iterator.hasNext()) {
                        Object key = iterator.next();
                        Object value = json.get(key);
                        key = key.toString().toLowerCase();
                        param.put(key, value);
                    }
                    listMap.add(param);
                }
            }
        }


        if (null != listMap) {
            for (Map<Object, Object> map : listMap) {
                jdbcTemplatePrimary.update(otherSql, new Object[]{map.get("id")});
            }
        }

        //20190628添加半年报、年末预报filelist保存语句
        if (StringUtils.equalsIgnoreCase(fileList.getTableName(), "rep910") && StringUtils.equalsIgnoreCase(fileList.getTypeCode(), "半年报、年末预报")) {
            if (null != listMap) {
                for (Map<Object, Object> map : listMap) {
                    if (StringUtils.isNotEmpty(fileList.getPreSavaStr())) {
                        jdbcTemplatePrimary.update(fileList.getPreSavaStr(), new Object[]{map.get("id")});
                    }

                }
            }
        }

        //statusSql
        if (null != listMap) {
            for (Map<Object, Object> map : listMap) {
                jdbcTemplatePrimary.update(statusSql, new Object[]{map.get("id")});
            }
        }


        //delete
        final JSONArray tempDeleteData = deleteData;
        sql = "DELETE FROM " + fileList.getTableName() + " WHERE id=?";
        jdbcTemplatePrimary.batchUpdate(sql, new BatchPreparedStatementSetter() {
            public int getBatchSize() {
                return tempDeleteData.size();
            }

            public void setValues(PreparedStatement arg0, int arg1)
                    throws SQLException {
                arg0.setInt(1, ((JSONObject) tempDeleteData.get(arg1)).getInt("ID"));
            }
        });
    }

    @Override
    public SummaryVO summary(SummaryDTO summaryDTO) {
        Integer years = summaryDTO.getYears();
        Integer months = summaryDTO.getMonths();
        String distNo = summaryDTO.getDistNo();
        String tableName = summaryDTO.getTableName();
        String typeCode = summaryDTO.getTypeCode();
        String userDistNo = summaryDTO.getUserDistNo();

        if (StringUtils.isEmpty(tableName)) {
            tableName = "";
        }

        if ("0".equals(distNo)) {
            distNo = "";
        }


        /*Map<String, Object> parameter = new LinkedHashMap<>(16);
        parameter.put("v_years", years);
        parameter.put("v_months", months);
        parameter.put("v_tableType", typeCode);
        parameter.put("v_tableName", tableName);
        parameter.put("v_distId", distNo);

        //4汇总当前地区，2当前地区和下级地区，1所有地区汇总
        parameter.put("v_distType", 2);

        parameter.put("v_distLength", 0);
        parameter.put("v_existsDataType", 1);

        parameter.put("v_userDistNo", userDistNo);

        StoredProcedureQuery storedProcedureQuery = entityManagerPrimary.createStoredProcedureQuery("_sumData");
        storedProcedureQuery.registerStoredProcedureParameter("v_years", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_months", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_tableType", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_tableName", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_distId", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_distType", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_distLength", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("v_existsDataType", Integer.class, ParameterMode.IN);

        storedProcedureQuery.registerStoredProcedureParameter("v_userDistNo", String.class, ParameterMode.IN);


        for (String parameterKey : parameter.keySet()) {
            storedProcedureQuery.setParameter(parameterKey, parameter.get(parameterKey));
        }
        boolean flag = storedProcedureQuery.execute();*/


        StringBuilder sql = new StringBuilder("call _sumData(");
        sql.append(years).append(", ");
        sql.append(months).append(", ");
        sql.append("'").append(typeCode).append("', ");
        sql.append("'").append(tableName).append("', ");
        sql.append("'").append(distNo).append("', ");
        sql.append(2).append(", ");
        sql.append(0).append(", ");
        sql.append(1).append(", ");
        sql.append("'").append(userDistNo).append("');");


        SummaryVO summaryVO = new SummaryVO();
        summaryVO.setRvalue(false);

        try {
            jdbcTemplatePrimary.execute(sql.toString());
            summaryVO.setRvalue(true);
        }catch (Exception e){
            throw e;
        }

//        summary2(summaryDTO);
        return summaryVO;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public List<CheckVO> check(TableType tableType, FileList fileList, Object[] ids, Integer years, Integer months, String distNo, Integer grade, Boolean isAllDist, Boolean isSb, Boolean isGrade) {
        List<CheckVO> rvalue = new ArrayList<>();
        StringBuffer upsql = new StringBuffer();
        StringBuffer upsqlbyid = new StringBuffer();
        StringBuffer defCond = new StringBuffer();
        StringBuffer third_sql = new StringBuffer();
        StringBuffer distStr = new StringBuffer();
        distStr.append(" and distid='").append(distNo).append("'");
        if (isAllDist) {
            distStr.setLength(0);
            distStr.append("and distid like '").append(distNo).append("%'");
        }
        try {
            List<RuleOuter> outers = fileList.getRuleOuters();

            rvalue.addAll(this.checksStype1(fileList, tableType, ids, outers, years, months, distNo, grade, isAllDist, isSb));
            rvalue.addAll(this.checksStype2(fileList, tableType, ids, outers, years, months, distNo, grade, isAllDist, isSb));

            //校验stype为类型为2的情况
//            rvalue.addAll(this.checksStype2(fileList, tableType, ids, outers, years, months, distNo, grade, isAllDist, isSb));

            //稽核表间公式
            defCond.setLength(0);
            upsql.setLength(0);
            upsqlbyid.setLength(0);
            upsqlbyid.append("update ")
                    .append(fileList.getTableName())
                    .append(" set balflag='否',statusno=1 where years=")
                    .append(years);
            if (tableType.getOptType() == 2 || tableType.getOptType() == 3) {
                upsqlbyid.append(" and months=").append(months);
            }
            upsqlbyid.append(" ").append(distStr).append(" and (saveflag='否' or ${isnull}(saveflag,'')='') and id=?");


            upsql.append("update ").append(fileList.getTableName())
                    .append(" set balflag='是',statusno=2 where years=").append(years);

            //"update rep901 set balflag=? where years=? and distid like ?  and (saveflag='否' or isnull(saveflag,'')='')";
            String sql = "select ou1.rid,ou1.zid,ou1.years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "ou1.months," : "") + "${isnull}(ou1.tabledec,'') as tablename,ou1.shortdist,ou1.distid,ou1.distname,${ISNULL}(ou1.lx,'') as lx,${ISNULL}(ou1.lxname,'') as lxname,ou1.detail,ou1.opt,${isnull}(ou1.bb ,0)as b1," +
                    "${isnull}(ou2.bb  ,0)as b2,${ISNULL}(ou1.bb-ou2.bb,0) b3,case when ${isnull}(ou1.bb,0)=0 and  ${isnull}(ou2.bb,0)=0  then 1 " +
                    "when  ${isnull}(ou1.bb,0)";

            //String upsql="update "+fileList.getTableName()+" set  balflag='是' where years="+years;
            if (tableType.getOptType() == 2 || tableType.getOptType() == 3) {
                upsql.append(" and months=").append(months);
            }
            upsql.append(" ").append(distStr).append(" and (saveflag='否' or ${isnull}(saveflag,'')='') ");

            String sqlZB_1 = "select zb .zid,zb.rid,zb.years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "zb.months," : "") + "zb.distid,zb.distname,zb.lx,zb.lxname,zb.tabledec,'" + fileList.getShortDis() +
                    "' as shortdist,zb.decexpress,zb.sourceexpress,zb.detail,zb.excond,zb.opt,zb.evalflag,zb.sourcecondexpress,zb.tableflag,zb.orderid from ( select  o.id as zid,r.id as rid,r.years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "r.months," : "") + "r.distid,r.distname,r.lx,r.lxname," +
                    "tableDec, decExpress, sourceExpress,Detail, exCond, opt, orderId, evalFlag, sourceCondExpress, tableflag " +
                    " from " + fileList.getTableName() + " r " +
                    " join _sRuleOuter o" +
                    " on r.years=o.years  and o.tabledec='" + fileList.getTableName() + "'" +
                    " where r.years=" + years + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and months=" + months : "") + " and distid like '" + distNo + "%'  and (saveflag='否' or ${isnull}(saveflag,'')='')" +
                    ")zb";

            String sqlZB_2 = " left join ( select o.id as oid,r.id as rid,r.years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "r.months," : "") + "r.distid,r.distname,r.lx,r.lxname" +
                    " from " + fileList.getTableName() + " r " +
                    " join _sRuleOuter o" +
                    " on r.years=o.years  and ${isnull}(r.lx,'')=${isnull}(o.lx,'') and ${isnull}(o.lx,'')<>'' and o.tabledec='" + fileList.getTableName() + "'" +
                    " where r.years=" + years + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and months=" + months : "") + " and distid like '" + distNo + "%'  and (saveflag='否' or ${isnull}(saveflag,'')='')" +
                    ")cb" +
                    " on  zb.zid=cb.oid and  zb.years=cb.years and zb.distid=cb.distid and zb.distname=cb.distname " +
                    " and zb.rid=cb.rid" +
                    " where cb.oid is not null";
            StringBuffer sqlZB = new StringBuffer();
            boolean isCheckOk = true;
            List<Object> listid = new ArrayList<Object>();
            String third_sqlZB = "";
            defCond.setLength(0);
            defCond.append(fileList.getFileItemLink() != null ? "lxName" : "");

            long startTime = System.currentTimeMillis();
            for (RuleOuter outer : outers) {
                if (outer.getSType() != 0) continue;
                third_sql.setLength(0);
                sqlZB.setLength(0);

                third_sql.append(sql);
                third_sql.append(outer.getOpt())
                        .append(" ${isnull}(ou2.bb,0) then 1 else 0  end isok from (");
                sqlZB.append(sqlZB_1);
                if (outer.getLx() != null && StringUtils.isNotEmpty(outer.getLx())) {
                    sqlZB.append(" ").append(sqlZB_2);
                }
                third_sqlZB = sqlZB.toString();

                String sqls = analysisExpress(third_sqlZB, years, months, distNo, fileList, false, outer.getDecExpress(), outer.getExCond(), defCond.toString(), tableType);
                String sqlsub = analysisExpress(third_sqlZB, years, months, distNo, fileList, false, outer.getSourceExpress(), outer.getSourceCondExpress(), defCond.toString(), tableType);

                third_sqlZB += " order by zb.rid,zb.zid";

                third_sql.append(sqls)
                        .append(")ou1 left join (")
                        .append(sqlsub)
                        .append(")ou2 on ou1.rid=ou2.rid and ou1.zid=ou2.zid and ou1.years=ou2.years ")
                        .append(((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and ou1.months=ou2.months " : ""))
                        .append("where ou1.years=").append(years)
                        .append(((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and ou1.months=" + months : ""))
                        .append(((isAllDist) ? " and ou1.distid like '" + distNo + "%'" : " and ou1.distid='" + distNo + "'"))
                        .append(" and ou1.zid=")
                        .append(outer.getId())
                        .append(" and (case when ${isnull}(ou1.bb,0)=0 and  ${isnull}(ou2.bb,0)=0  then 1 when  ${isnull}(ou1.bb,0)")
                        .append(outer.getOpt())
                        .append(" ${isnull}(ou2.bb,0) then 1 else 0 end) =0")
                        .append(((isGrade) ? " and ${len}(ou1.distid)<=" + DataConstants.getMaxDistNoLength(distNo, grade) : ""))
                        .append(" order by ou1.years,ou1.distid,ou1.rid,ou1.zid ");

                String sql1 = Common.replaceFun(third_sql.toString());
                List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql1);
                rvalue.addAll(ContrastData(datas));

            }
            List<Object> rids = new ArrayList<Object>();
            String str = "";
            int i = 0;
            for (CheckVO obj : rvalue) {
                rids.add(obj.getId());
                if (i != 0) str += ",";
                str += "?";
                i++;
            }

//            int[] num = tongjiJdbcTemplate.batchUpdata(Common.replaceFun(upsqlbyid.toString()), rids);

            final List<Object> paramLists = new ArrayList<>();
            paramLists.addAll(rids);
            //将指标不符合的数据更新成未稽核状态
            int[] num = jdbcTemplatePrimary.batchUpdate(Common.replaceFun(upsqlbyid.toString()), new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    Integer id = (Integer) paramLists.get(i);
                    ps.setInt(1, id);
                }

                @Override
                public int getBatchSize() {
                    return paramLists.size();
                }
            });


            if (null != rids && rids.size() != 0) {
                upsql.append(" and id not in(").append(str).append(")");
            }

//            boolean flag = tongjiJdbcTemplate.updateDate(Common.replaceFun(upsql.toString()), rids);
            //将指标符合的数据更新成稽核状态
            int update = jdbcTemplatePrimary.update(Common.replaceFun(upsql.toString()), rids.toArray(listid.toArray(new Object[rids.size()])));

        } catch (NumberFormatException e) {
            log.error("baseDataManagerImpl check error:" + e);
            e.printStackTrace();
        } catch (DataAccessException e) {
            log.error("baseDataManagerImpl check error:" + e);
        }
        Collections.sort(rvalue);
        return rvalue;
    }

    @Override
    public Map<String, Object> importExcelByTemplate(TableType tableTypeObj, Integer excelTemplateid, String rootPath, String newFileName, String topDistid, Integer topyears, Integer topmonths) {

        List<ImportExcelVO> excelInfo = new ArrayList<>();
        Map<String, Object> rvalue = new HashMap<>();
        Map<Object, Object> param = new HashMap<>();
        String num_matche = "^-?[0-9]\\d*\\.?\\d*$";
        int successRows = 0;
        int errorRows = 0;
        Boolean isContinue = false;
        HSSFWorkbook wk = null;

        if (topyears == 0) {
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("当前选择日期错误，请刷新页面后再操作！");
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;
        }

        if (null == topDistid || StringUtils.isEmpty(topDistid)) {
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("当前选择地区错误，请刷新页面后再操作！");
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;
        }


        String tableType = tableTypeObj.getTableType();
        String sqls = "select id from uploadBase  where  years=? and ${isnull}(months,0)=? and distno like ?  and tableType=? and okflag=1";
        List<Map<String, Object>> lists = jdbcTemplatePrimary.queryForList(Common.replaceFun(sqls), new Object[]{topyears, topmonths, topDistid, tableType});
        if (lists.size() > 0) {
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("当前地区已上报，操作取消！");
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;
        }

        ExcelTemplate excelTemplate = excelTemplateManager.getExcelTemplateById(excelTemplateid);
        String belongDistNo = excelTemplate.getBelongDistNo();
        User user = SecurityUtil.getLoginUser();

        //不为空则需要判断当前导入的模板 是否为该用户所属的模板
        if(StringUtils.isNotBlank(belongDistNo)){
            if(!user.getTjDistNo().startsWith(belongDistNo)
                    || !belongDistNo.startsWith(user.getTjDistNo())){
                throw new RuntimeException("用户:" + user.getUsername() + " 不能操作该模板!");
            }
        }

        String fileNamePath = rootPath + excelTemplate.getName();
        File file = new File(fileNamePath);
        if (!file.exists()) {
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("当前模板文件不存在，操作取消！");
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;
        }

        try {
            //解析后的模板
            List<ExcelTemplateInfo> templateInfos = queryExcelTemplateInfo(excelTemplate, fileNamePath);
            if (templateInfos.size() == 0) {
                ImportExcelVO ie = new ImportExcelVO();
                ie.setIsSuccess(false);
                ie.setRow(1);
                ie.setInfo("模板信息未配置，操作取消！");
                excelInfo.add(ie);
                rvalue.put("importExcel", excelInfo);
                rvalue.put("successRows", successRows);
                rvalue.put("errorRows", errorRows);
                return rvalue;
            }

            File file2 = new File(newFileName);
            if (!file2.exists()) {
                ImportExcelVO ie = new ImportExcelVO();
                ie.setIsSuccess(false);
                ie.setRow(1);
                ie.setInfo("导入文件不存在，操作取消！文件：" + newFileName);
                excelInfo.add(ie);
                rvalue.put("importExcel", excelInfo);
                rvalue.put("successRows", successRows);
                rvalue.put("errorRows", errorRows);
                return rvalue;
            }

            wk = WorkbookUtils.openWorkbook(newFileName);
            int sheetCount = wk.getNumberOfSheets();
            System.out.println("项目路径：" + rootPath);
            System.out.println("导入文件sheet数量：" + sheetCount);

            System.out.println("fileNamePath：" + fileNamePath);
            System.out.println("templateInfos.size：" + templateInfos.size());
            System.out.println("导入模板文件sheet数量：" + templateInfos.get(templateInfos.size() - 1).getSheetIndex() + 1);
            log.info("项目路径：" + rootPath);
            log.info("导入文件sheet数量：" + sheetCount);
            log.info("导入模板文件sheet数量：" + templateInfos.get(templateInfos.size() - 1).getSheetIndex() + 1);
            if (sheetCount != templateInfos.get(templateInfos.size() - 1).getSheetIndex() + 1) {
                ImportExcelVO ie = new ImportExcelVO();
                ie.setIsSuccess(false);
                ie.setRow(1);
                ie.setInfo("导入文件单元表数据不对应，操作取消！");
                excelInfo.add(ie);
                rvalue.put("importExcel", excelInfo);
                rvalue.put("successRows", successRows);
                rvalue.put("errorRows", errorRows);
                return rvalue;
            }

            for (ExcelTemplateInfo templateInfo : templateInfos) {
                HSSFSheet sheet = wk.getSheetAt(templateInfo.getSheetIndex());

                //待定，如何判断是否有值
                int[] distPosition = templateInfo.getDistPosition();
                int[] ztNamePosition = templateInfo.getZtNamePosition();
                int positionIndex = templateInfo.getDistPositionIndex();
                String strName = "";
                String valueCell = "";
                HSSFCell positionCell = null;
                if (distPosition != null) {
                    positionCell = sheet.getRow(distPosition[0]).getCell(distPosition[1]);
                }

                if (ztNamePosition != null) {
                    positionCell = sheet.getRow(distPosition[0]).getCell(distPosition[1]);
                }

                if (distPosition != null || ztNamePosition != null) {
                    if (positionCell == null) {
                        ImportExcelVO ie = new ImportExcelVO();
                        ie.setIsSuccess(false);
                        ie.setRow(1);
                        ie.setInfo("导入文件Excel第" + (templateInfo.getSheetIndex() + 1) + "表为非法表格，操作取消！");
                        excelInfo.add(ie);
                        rvalue.put("importExcel", excelInfo);
                        rvalue.put("successRows", successRows);
                        rvalue.put("errorRows", errorRows);
                        return rvalue;
                    } else if (distPosition != null) {

                        strName = StringUtils.trimToEmpty(positionCell.getStringCellValue());

                        if (positionIndex != 0 && (strName.length() > positionIndex)) {
                            strName = strName.substring(positionIndex);
                            strName = StringUtils.trimToEmpty(strName);
                        }
                    } else if (ztNamePosition != null) {

                        strName = StringUtils.trimToEmpty(positionCell.getStringCellValue());

                        if (positionIndex != 0 && (strName.length() > positionIndex)) {
                            strName = strName.substring(positionIndex);
                            strName = StringUtils.trimToEmpty(strName);
                        }
                    }
                }

                //取日期
                int[] yearsPosition = templateInfo.getYearsPosition();
                int yearsIndex = templateInfo.getYearsIndex();
                Integer years = 0;
                int months = 0;
                if (yearsPosition != null) {
                    positionCell = sheet.getRow(yearsPosition[0]).getCell(yearsPosition[1]);
                    valueCell = positionCell.getStringCellValue().substring(yearsIndex);
                    //int a=valueCell.indexOf("年");
//					valueCell=valueCell.substring(0,valueCell.indexOf("年"));
//					valueCell.indexOf("年")

                    if (valueCell.indexOf("年") != -1) {
                        years = Integer.parseInt(valueCell.substring(0, valueCell.indexOf("年")));
                    } else {
                        years = Integer.parseInt(valueCell);
                    }
                    if (years.intValue() != topyears.intValue()) {
                        ImportExcelVO ie = new ImportExcelVO();
                        ie.setIsSuccess(false);
                        ie.setRow(1);
                        ie.setInfo("导入EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表导入文件非当前选择日期，操作取消！");
                        excelInfo.add(ie);
                        rvalue.put("importExcel", excelInfo);
                        rvalue.put("successRows", successRows);
                        rvalue.put("errorRows", errorRows);
                        return rvalue;
                    }
                }

                //待定 取地区信息，账套信息

                Map<String, List<String>> distIdMap = new HashMap<String, List<String>>();
                List<String> distIdList = new ArrayList<String>();
                String tableName = templateInfo.getTableName();
                distIdMap.clear();
                distIdList.clear();

                User loginUser = SecurityUtil.getLoginUser();
                String userDistNo = loginUser.getTjDistNo();
                FileList fileList = fileListManager.getFileList(tableType, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, topyears, topmonths, userDistNo);

                if (null == fileList) {
                    ImportExcelVO ie = new ImportExcelVO();
                    ie.setIsSuccess(false);
                    ie.setRow(1);
                    ie.setInfo("导入文件Excel第" + (templateInfo.getSheetIndex() + 1) + "张表非法操作，操作取消！");
                    excelInfo.add(ie);
                    rvalue.put("importExcel", excelInfo);
                    rvalue.put("successRows", successRows);
                    rvalue.put("errorRows", errorRows);
                    return rvalue;
                }

                //是否导入当前表
                if(Objects.isNull(fileList.getIsImportData()) || !fileList.getIsImportData()) {
                    continue;
                }

                //参数
                param.put("years", topyears);
                param.put("tableType", tableType);
                param.put("months", topmonths);
                param.put("tableName", tableName);

                //表字段
                StringBuffer inserSql = new StringBuffer();
                StringBuffer delSql = new StringBuffer();
                StringBuffer headColumns = new StringBuffer();
                StringBuffer upSql = new StringBuffer();
                StringBuffer headSql = new StringBuffer();
                StringBuffer whereSql = new StringBuffer();
                StringBuffer values = new StringBuffer();
                List<FileItem> fileItems = fileList.getFileItems();
                Map<String, Object> keyColumns = templateInfo.getColumns();
                //List<List<Object>> listLists=new ArrayList<List<Object>>();
                List<String> uplist = new ArrayList<String>();
                int i = 0;
                int rowIndex = templateInfo.getBeginRow();
                Integer columnIndex = -1;
                //int ii=0;
                HSSFCell cell = null;
                List<List<Object>> templist = null;
                List<String> inserList = new ArrayList<>();


                //新增
                Map<String, String> insertItemValue = new LinkedHashMap<>();

                //更新
                Map<String, String> updateItemValue = new LinkedHashMap<>();


                List<RuleInner> ruleInners = fileList.getRuleInners();

                int keySize = keyColumns.size();
                int cellSize = sheet.getRow(rowIndex).getLastCellNum();
                //模板列与文件列
                if (keySize != cellSize) {
                    String str = sheet.getRow(rowIndex).getCell(0).getStringCellValue();
                    if (StringUtils.contains(str, "贝佳软件")) {
                        continue;
                    }
                    ImportExcelVO ie = new ImportExcelVO();
                    ie.setIsSuccess(false);
                    ie.setRow(1);
                    ie.setInfo("导入文件Execl第" + (templateInfo.getSheetIndex() + 1) + "张表非法，于当前模板列数不一致，操作取消！");
                    excelInfo.add(ie);
                    rvalue.put("importExcel", excelInfo);
                    rvalue.put("successRows", successRows);
                    rvalue.put("errorRows", errorRows);
                    return rvalue;
                }
                int keyCount = 0;
                Boolean isover = false;
                StringBuffer sql_distname_cur = new StringBuffer();
                String sql_distwhere = " and ( ${charindex('${curDistid}',distid)}>0 or ${charindex('${curDistid}',import_distNo)}>0)";
                sql_distwhere = Common.replaceFun(sql_distwhere);
                String sql_distname = "select distid,distname from dist where years=? and distid like ? and (distName=? or import_name=?) and ${ISNULL}(distId,'')<>''";
                String sql_distid = "select distid from dist where years=? and distid like ? and (distid = ? or import_distNo=?)   and ${ISNULL}(distId,'')<>''";
                String sql_lx = "select lxid from lxorder where years=? and typeCode=? and lx=?";
                //String sql_linkex="select prjItem from fileItemLinkex where years=? and tableName=? and prjItem=?";

                String gz_sql = "select distid from dist where years=? and distid like ? and distid=? ";
                while (!isover) {
                    keyCount = 0;
                    inserSql.setLength(0);
                    delSql.setLength(0);
                    upSql.setLength(0);
                    headSql.setLength(0);
                    whereSql.setLength(0);
                    headColumns.setLength(0);
                    values.setLength(0);

                    insertItemValue.clear();
                    updateItemValue.clear();

                    if (sheet.getRow(rowIndex) == null) {
                        isover = true;
                        break;
                    }

                    StringBuffer temp_distid = new StringBuffer();
                    String lxid = "";
                    isContinue = false;
                    String cell_distid = "-1";
                    OK:
                    for (FileItem fileItem : fileItems) {

                        columnIndex = (Integer) keyColumns.get(fileItem.getFieldName().toUpperCase());
                        if (columnIndex != null && columnIndex != -1) {
                            keyCount++;
                            cell = sheet.getRow(rowIndex).getCell(columnIndex);
                            String cellValue = "";
                            if (null != cell) {
                                switch (cell.getCellType()) {
                                    case HSSFCell.CELL_TYPE_NUMERIC:
//											DecimalFormat dformat=new DecimalFormat("0");
//											if(StringUtils.isNotEmpty(fileItem.getDisFormat())){
//												int cc=StringUtils.lastIndexOf(fileItem.getDisFormat(), ",");
//												int bb=fileItem.getDisFormat().length();
//													if(StringUtils.lastIndexOf(fileItem.getDisFormat(), ",")==fileItem.getDisFormat().length()-1){
//														dformat=new DecimalFormat(StringUtils.substring(fileItem.getDisFormat(), 0,fileItem.getDisFormat().lastIndexOf(",")));
//													}
//											}
                                        //dformat.format(cell.getNumericCellValue());
                                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                            Date d = cell.getDateCellValue();
                                            cellValue = DateUtil.getAllDate(d);
                                        } else {
                                            //cellValue=cell.getCellFormula(); 
                                            cellValue = Double.toString(cell.getNumericCellValue());
                                        }

                                        break;
                                    case HSSFCell.CELL_TYPE_FORMULA:
                                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                                            Date d = cell.getDateCellValue();
                                            cellValue = DateUtil.getAllDate(d);
                                        } else {
                                            //cellValue=cell.getCellFormula(); 
                                            cellValue = Double.toString(cell.getNumericCellValue());
                                        }
                                        //values.append("'").append(cellValue).append("',");
                                        break;
                                    case HSSFCell.CELL_TYPE_BLANK:
                                        cellValue = "";
                                        break;
                                    case HSSFCell.CELL_TYPE_BOOLEAN:
                                        cellValue = StringUtils.trimToEmpty(cell.getStringCellValue());
                                        if (StringUtils.equalsIgnoreCase(cellValue, "是")) {
                                            cellValue = "1";
                                        } else if (StringUtils.equalsIgnoreCase(cellValue, "否")) {
                                            cellValue = "0";
                                        }
                                        break;
                                    case HSSFCell.CELL_TYPE_ERROR:
                                        cellValue = "";
                                        break;
                                    case HSSFCell.CELL_TYPE_STRING:
                                    default:
                                        cellValue = StringUtils.trimToEmpty(cell.getStringCellValue());
                                        if (StringUtils.equalsIgnoreCase(fileItem.getFType(), "N") && StringUtils.isEmpty(cellValue)) {
                                            cellValue = "0";
                                        } else if (StringUtils.equalsIgnoreCase(fileItem.getFType(), "N") && StringUtils.isNotEmpty(cellValue)) {
                                            if (!cellValue.matches(num_matche)) {
                                                ImportExcelVO ie = new ImportExcelVO();
                                                ie.setIsSuccess(false);
                                                ie.setRow(rowIndex);
                                                ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此【" + cellValue + "】是错误内容，此条记录已跳过，不记入系统！");
                                                excelInfo.add(ie);
                                                rvalue.put("importExcel", excelInfo);
                                                rvalue.put("successRows", successRows);
                                                rvalue.put("errorRows", errorRows);
                                                isContinue = false;
                                                break OK;
                                            }
                                        } else if (StringUtils.equalsIgnoreCase(fileItem.getFType(), "S")){
                                            if(StringUtils.isEmpty(cellValue)){
                                                ImportExcelVO ie = new ImportExcelVO();
                                                ie.setIsSuccess(false);
                                                ie.setRow(rowIndex);
                                                ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此不能为空，此条记录已跳过，不记入系统！");
                                                excelInfo.add(ie);
                                                rvalue.put("importExcel", excelInfo);
                                                rvalue.put("successRows", successRows);
                                                rvalue.put("errorRows", errorRows);
                                                isContinue = false;
                                                break OK;
                                            } else {
                                                //获取配置的选择项
                                                String formatterSelect = fileItem.getFormatterSelect();
                                                if(StringUtils.isEmpty(formatterSelect)){
                                                    ImportExcelVO ie = new ImportExcelVO();
                                                    ie.setIsSuccess(false);
                                                    ie.setRow(rowIndex);
                                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "配置选项列表为空，此条记录已跳过，不记入系统！");
                                                    excelInfo.add(ie);
                                                    rvalue.put("importExcel", excelInfo);
                                                    rvalue.put("successRows", successRows);
                                                    rvalue.put("errorRows", errorRows);
                                                    isContinue = false;
                                                    break OK;
                                                }
                                                List<String> formatterSelectArrays = JSON.parseArray(formatterSelect, String.class);
                                                if(!formatterSelectArrays.contains(StringUtils.trimToEmpty(cellValue))){
                                                    ImportExcelVO ie = new ImportExcelVO();
                                                    ie.setIsSuccess(false);
                                                    ie.setRow(rowIndex);
                                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此【" + cellValue + "】不存在配置选项列表中，此条记录已跳过，不记入系统！");
                                                    excelInfo.add(ie);
                                                    rvalue.put("importExcel", excelInfo);
                                                    rvalue.put("successRows", successRows);
                                                    rvalue.put("errorRows", errorRows);
                                                    isContinue = false;
                                                    break OK;
                                                }
                                            }
                                        }
                                        break;
                                }
                            }
                            if (StringUtils.contains(cellValue, "贝佳软件")) {
                                isover = true;
                                isContinue = true;
                                i = i - 1;
                                break;
                            }

//									Map<String,List<Object>> mapList=new HashMap<String, List<Object>>();
//									mapList.get(fileItem.getFieldName().toLowerCase());
                            if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "distid")) {
                                cell_distid = cellValue;
                                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(Common.replaceFun(sql_distid), new Object[]{topyears, topDistid + "%", cellValue, cellValue});
                                if (null == list || list.size() == 0 || list.get(0).size() == 0) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列地区号【" + cellValue + "】不存在，此条记录已跳过，不记入系统！");
                                    excelInfo.add(ie);
                                    rvalue.put("importExcel", excelInfo);
                                    rvalue.put("successRows", successRows);
                                    rvalue.put("errorRows", errorRows);
                                    break OK;

                                }

                                if (null != list && list.size() != 0 && list.get(0).size() != 0) {
                                    cellValue = (String) list.get(0).values().toArray()[0];
                                    temp_distid.append(cellValue);
//											distIdList.add(cellValue);
                                    //isContinue=true;
                                }

                                String sumFlag = this.queryTabSumFlag(fileList, topyears, cellValue, tableName);
                                if (null != sumFlag && StringUtils.equalsIgnoreCase(sumFlag, "是")) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行是汇总数据,跳过导入，请在汇总时生成！");
                                    excelInfo.add(ie);
                                    rvalue.put("importExcel", excelInfo);
                                    rvalue.put("successRows", successRows);
                                    rvalue.put("errorRows", errorRows);
                                    break OK;
                                }

                            }

                            if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "distname")) {
                                sql_distname_cur.append(sql_distname);
                                if (temp_distid.length() > 0) {
                                    sql_distname_cur.append(sql_distwhere.replace("${curdistid}", temp_distid));
                                    temp_distid.setLength(0);
                                }
                                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(Common.replaceFun(sql_distname_cur.toString()), new Object[]{topyears, topDistid + "%", cellValue, cellValue});
                                sql_distname_cur.setLength(0);
                                if (null == list || list.size() == 0 || list.get(0).size() == 0) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列地区名【" + cellValue + "】不存在，此条记录已跳过，不记入系统！");
                                    excelInfo.add(ie);
                                    if (rvalue.size() <= 20) {
                                        rvalue.put("importExcel", excelInfo);
                                    }

                                    //rvalue.put("successRows", successRows);
//												successRows++;
                                    //rvalue.put("errorRows", errorRows);
//												errorRows++;
                                    break;
                                }

                                //cellValue=(String);
                                temp_distid.append(list.get(0).values().toArray()[0]);
                                distIdList.add(list.get(0).values().toArray()[0].toString());
                                cellValue = list.get(0).values().toArray()[1].toString();
                                isContinue = true;
                                //	}

                            }
                            if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "lx")) {
//										if(!isContinue){
                                //isContinue=false;

                                List<Map<String, Object>> gd_dist = jdbcTemplatePrimary.queryForList(gz_sql, new Object[]{topyears, DataConstants.excel_dist + "%", cell_distid});

                                if (null != gd_dist && gd_dist.size() > 0) {

                                }

                                if (null != cellValue && StringUtils.equalsIgnoreCase(cellValue, "汇总数")) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此【" + cellValue + "】此数据是汇总数据，跳过导入，请在汇总时生成！");
                                    excelInfo.add(ie);
                                    if (rvalue.size() <= 20) {
                                        rvalue.put("importExcel", excelInfo);
                                    }
                                    break;
                                }

                                String sumFlag_lx = this.queryTabSumFlag(fileList, topyears, cell_distid, tableName, cellValue);
                                if (null != sumFlag_lx && StringUtils.equalsIgnoreCase(sumFlag_lx, "是")) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此表【" + cellValue + "】类型不存在，已跳过此记录，不记入系统！");
                                    excelInfo.add(ie);
                                    if (rvalue.size() <= 20) {
                                        rvalue.put("importExcel", excelInfo);
                                    }
                                    break;
                                }
//										if(null!=cellValue && StringUtils.isNotEmpty(cellValue)){
//											List<Object> links=tongjiJdbcTemplate.queryForList(Common.replaceFun(sql_linkex), false,new Object[]{topyears,tableName,cellValue});
//											if(null==links || links.size()==0){
//												isContinue=false;			
//												ImportExcelVO ie=new ImportExcelVO();
//												ie.setIsSuccess(false);
//												ie.setRow(rowIndex);
//												ie.setInfo("EXCEL第"+(templateInfo.getSheetIndex()+1)+"表，第"+(rowIndex+1)+"行,第"+(columnIndex+1)+"列此表【"+cellValue+"】类型不存在，已跳过此记录，不记入系统！");
//												excelInfo.add(ie);
//												if(rvalue.size()<=20){
//													rvalue.put("importExcel", excelInfo);
//												}
//												break;
//											}
//											
//										}

                                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(Common.replaceFun(sql_lx), new Object[]{topyears, tableType, cellValue});
                                if (null == list || list.size() == 0) {
                                    isContinue = false;
                                    ImportExcelVO ie = new ImportExcelVO();
                                    ie.setIsSuccess(false);
                                    ie.setRow(rowIndex);
                                    ie.setInfo("EXCEL第" + (templateInfo.getSheetIndex() + 1) + "表，第" + (rowIndex + 1) + "行,第" + (columnIndex + 1) + "列此【" + cellValue + "】此内容为非法单位类型，已跳过此记录，不记入系统！");
                                    excelInfo.add(ie);
                                    if (rvalue.size() <= 20) {
                                        rvalue.put("importExcel", excelInfo);
                                    }
                                    break;
                                }

                                isContinue = true;
                                lxid = (null != list.get(0).values().toArray()[0]) ? list.get(0).values().toArray()[0].toString() : "";
//										}

                            }

                            if (isContinue) {

                                //判断是否导入此字段
                                if(Objects.nonNull(fileItem.getImportFlag()) && fileItem.getImportFlag()){

                                    headColumns.append(fileItem.getFieldName().toUpperCase()).append(",");

                                    if (StringUtils.equalsIgnoreCase(fileItem.getFType(), "N")) {
                                        if (StringUtils.isEmpty(cellValue)) {
                                            cellValue = "0";
                                        }
                                        values.append(StringUtils.trimToEmpty(cellValue)).append(",");

                                        //将导入的item的value对应添加
                                        insertItemValue.put(fileItem.getFieldName().toUpperCase(), cellValue);

                                    } else {
                                        values.append("'").append(StringUtils.trimToEmpty(cellValue)).append("',");

                                        //将导入的item的value对应添加
                                        insertItemValue.put(fileItem.getFieldName().toUpperCase(), "'" + cellValue + "'");
                                    }

                                    //11
                                    if (Objects.nonNull(fileItem.getIsSumColumn()) && fileItem.getIsSumColumn()) {
                                        if(StringUtils.equalsIgnoreCase(fileItem.getFType(), "C") || StringUtils.equalsIgnoreCase(fileItem.getFType(), "S")){
                                            headSql.append(fileItem.getFieldName().toUpperCase())
                                                    .append("='")
                                                    .append(cellValue)
                                                    .append("',");

                                            //将导入的item的value对应添加
                                            updateItemValue.put(fileItem.getFieldName().toUpperCase(), cellValue);
                                        } else {
                                            headSql.append(fileItem.getFieldName().toUpperCase())
                                                    .append("=")
                                                    .append(cellValue)
                                                    .append(",");

                                            //将导入的item的value对应添加
                                            updateItemValue.put(fileItem.getFieldName().toUpperCase(), cellValue);
                                        }
                                    } else {
                                        whereSql.append(fileItem.getFieldName())
                                                .append("='").append(cellValue).append("' and ");
                                    }
//
                                    if (temp_distid.length() > 0) {
                                        headColumns.append("distid").append(",");
                                        values.append("'").append(temp_distid).append("',");
                                        insertItemValue.put("distid", "'" + temp_distid + "'");
                                        whereSql.append(" distid='").append(temp_distid).append("' and ");
                                        temp_distid.setLength(0);
                                    }
                                }


                            }
                        }
                    }

                    //处理完导入的字段后，处理表内关系
                    for (RuleInner ruleInner : ruleInners) {
                        //如果表间稽核开启
                        if(ruleInner.getUseFlag()){
                            String fieldName = ruleInner.getFieldName();
                            String insertExpress = ruleInner.getExpress().toUpperCase();
                            String updateExpress = ruleInner.getExpress().toUpperCase();

                            //处理插入的语句的指标值
                            for (String s : insertItemValue.keySet()) {
                                insertExpress = insertExpress.replace("[" + s + "]", insertItemValue.get(s));
                            }
                            insertItemValue.put(fieldName.toUpperCase(), String.valueOf(CalculationUtils.calculation(insertExpress)));


                            //处理更新语句的指标值的语句的指标值
                            for (String s : updateItemValue.keySet()) {
                                updateExpress = updateExpress.replace("[" + s + "]", updateItemValue.get(s));
                            }
                            updateItemValue.put(fieldName.toUpperCase(), String.valueOf(CalculationUtils.calculation(updateExpress)));

                        }
                    }


                    //将更新的语句改成处理指标后的更新语句
                    if(!StringUtils.isBlank(headSql)){
                        String updateItemValueSql
                                = updateItemValue.keySet().stream().map(e -> e + "=" + updateItemValue.get(e)).collect(Collectors.joining(", "));

                        headSql.setLength(0);
                        headSql.append(updateItemValueSql).append(", ");
                    }

                    //将新增的语句改成处理指标后的新增语句
                    if(!StringUtils.isBlank(headColumns)){

                        String items
                                = insertItemValue.keySet().stream().collect(Collectors.joining(", "));
                        headColumns.setLength(0);
                        headColumns.append(items).append(", ");

                        String values2
                                = insertItemValue.values().stream().collect(Collectors.joining(", "));
                        values.setLength(0);
                        values.append(values2).append(", ");

                    }


                    //		String sql=StringUtils.substring(headColumns.toString(),0,headColumns.lastIndexOf(","));
                    //headColumns.append(")");
                    //	String sql=StringUtils.substring(headColumns.toString(),0,headColumns.lastIndexOf(","))+")";
                    i++;
                    if (isContinue && keyCount != keySize) {
                        if (!isover) {
                            ImportExcelVO ie = new ImportExcelVO();
                            ie.setIsSuccess(false);
                            ie.setRow(1);
                            ie.setInfo("模板文件第" + (templateInfo.getSheetIndex() + 1) + "张表错误，操作取消！" + "\r\n" + "上传文件与当前模板列数不对。");
                            excelInfo.add(ie);
                            if (rvalue.size() <= 20) {
                                rvalue.put("importExcel", excelInfo);
                            }
                            rvalue.put("successRows", successRows);
                            rvalue.put("errorRows", errorRows);
                            return rvalue;
                        }
                    }

                    if (!isContinue) {
                        rowIndex++;
                        continue;
                    }
                    List<Map<String, Object>> list = null;
                    Integer count = 0;
                    if (isContinue && whereSql.length() > 0) {

                        StringBuffer isExistSql = new StringBuffer();
                        isExistSql.append("select count(id) conts from  ")
                                .append(tableName)
                                .append(" where years=")
                                .append(topyears)
                                .append(" and ")
                                .append(whereSql)
                                .append("  distid like '")
                                .append(topDistid)
                                .append("%'");
                        if (tableTypeObj.getOptType() == 2 || tableTypeObj.getOptType() == 3) {
                            isExistSql.append(" and ${isnull}(months,'')=")
                                    .append(topmonths);
                        }
                        //log.info("检查数据："+isExistSql);
                        list = jdbcTemplatePrimary.queryForList(Common.replaceFun(isExistSql.toString()), new Object[]{});
                        count = Integer.parseInt(list.get(0).get("conts").toString());
                        //log.info("检查数据总数据："+list.size());
                        //log.info("检查数据count："+count);
                    }
                    if (count == 0) {
                        if (isContinue && headColumns.length() > 0 && values.length() > 0) {
                            headColumns.append("years");
                            values.append(topyears);
                            if (tableTypeObj.getOptType() == 2 || tableTypeObj.getOptType() == 3) {
                                headColumns.append(",months");
                                values.append(",").append(topmonths);
                            }

                            if (StringUtils.isNotEmpty(lxid)) {
                                headColumns.append(",lxid");
                                values.append(",").append(lxid);
                            }

                            inserSql.append("insert into ")
                                    .append(fileList.getTableName())
                                    .append("(balflag,sumflag,saveflag,")
                                    .append(headColumns.toString())
                                    .append(")")
                                    .append("values")
                                    .append("('否','否','否',")
                                    .append(values.toString())
                                    .append(")");
                            inserList.add(Common.replaceFun(inserSql.toString()));
                        }
                    }

                    if (count > 0) {
                        if (isContinue && headSql.length() > 0 && whereSql.length() > 0) {
                            whereSql.append(" years=").append(topyears);
                            if (tableTypeObj.getOptType() == 2 || tableTypeObj.getOptType() == 3) {
                                whereSql.append(" and ${isnull}(months,'')=").append(topmonths);
                            }
                            if (StringUtils.isNotEmpty(lxid)) {
                                headSql.append("lxid=").append(lxid).append(",");
                            }

                            upSql.append(" update ")
                                    .append(tableName)
                                    .append(" set ")
                                    .append(" balflag='否',")
                                    .append(StringUtils.substring(headSql.toString(), 0, headSql.lastIndexOf(",")))
                                    .append(" where distid like '")
                                    .append(topDistid)
                                    .append("%'   and (saveflag='否' or ${ISNULL}(saveflag,'')='') and  ")
                                    .append(whereSql.toString());
                            uplist.add(Common.replaceFun(upSql.toString()));
                        }
                    }


                    //if(i>=500){


                    if (i > 500) {
                        int[] num = null;
                        int size = 0;
                        Boolean isExceAfert = false;
                        if (null != inserList && inserList.size() > 0) {
//                            num = jdbcTemplatePrimary.batchUpdateByArray(inserList, null);
                            num = jdbcTemplatePrimary.batchUpdate(inserList.toArray(new String[inserList.size()]));
                            if (null != num) size = num.length;
//										successRows+=num.length;
//										errorRows+=inserList.size()-num.length;
                            inserList.clear();
                            isExceAfert = true;
                        }
                        if (null != uplist && uplist.size() > 0) {
//                            num = jdbcTemplatePrimary.batchUpdateByArray(uplist, null);
                            num = jdbcTemplatePrimary.batchUpdate(uplist.toArray(new String[uplist.size()]));
                            if (null != num) size = size + num.length;
//										successRows+=num.length;
//										errorRows+=uplist.size()-num.length;
                            uplist.clear();
                            isExceAfert = true;
                        }

                        if (isExceAfert) {
                            successRows += size;
                            errorRows += i - size;
                            log.info("excel 成功导入：" + size + "条");
                            i = 0;
                            size = 0;
                            afertExec("excel_导入", param, distIdList);
                            distIdList.clear();
                        }
                    }


                    rowIndex++;
                }
                int[] num = null;
                Boolean isExceAfert = false;
                int size = 0;
                if (isContinue) {
                    if (null != inserList && inserList.size() > 0) {
//                        num = jdbcTemplatePrimary.batchUpdateByArray(inserList, null);
                        num = jdbcTemplatePrimary.batchUpdate(inserList.toArray(new String[inserList.size()]));
//								successRows+=num.length;
//								errorRows+=i-num.length;
                        if (null != num) size = num.length;
                        inserList.clear();
                        isExceAfert = true;
                    }

                    if (uplist != null && uplist.size() > 0) {
//                        num = jdbcTemplatePrimary.batchUpdateByArray(uplist, null);
                        num = jdbcTemplatePrimary.batchUpdate(uplist.toArray(new String[uplist.size()]));
//								successRows+=num.length;
//								errorRows+=i-num.length;
                        if (null != num && num[0] != 0) size = size + num.length;
                        uplist.clear();
                        isExceAfert = true;
                    }

                    if (isExceAfert) {
                        successRows += size;
                        errorRows += i - size;
                        log.info(tableName + "excel 已成功导入：" + size + "条");
                        i = 0;
                        size = 0;
                        afertExec("excel_导入", param, distIdList);
                        distIdList.clear();
                    }
                }

                if (successRows == 0 && errorRows == 0) {
                    errorRows += i - size;
                }
                //}
            }

            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);


        } catch (ExcelException e) {
            log.error("" + e.getMessage());
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("模板文件错误，操作取消！" + "\r\n" + e.getMessage());
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;
        } catch (SQLException e) {
            log.error("error:" + e.getMessage());
            ImportExcelVO ie = new ImportExcelVO();
            ie.setIsSuccess(false);
            ie.setRow(1);
            ie.setInfo("模板文件错误，操作取消！" + "\r\n" + e.getMessage());
            excelInfo.add(ie);
            rvalue.put("importExcel", excelInfo);
            rvalue.put("successRows", successRows);
            rvalue.put("errorRows", errorRows);
            return rvalue;

        } finally {
	/*		try {
				if(null!=wk)
				wk.close();
			} catch (IOException e) {
				log.error("error:"+e.getMessage());
			}*/
        }

        return rvalue;
    }

    @Override
    public Map<String, Object> listLxsAndLxNames(String typeCode, String tableName, String distNo, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String userDistNo = loginUser.getTjDistNo();
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        List<List<Object>> data = this.getAddDistEx(fileList, years, tableName, distNo);
        List<TabInLimitLx> tabInLimitLxs = tabInLimitLxManager.listTabInLimitLxs(years, fileList.getTableName(), "baseData");

        if (null != tabInLimitLxs && tabInLimitLxs.size() > 0) {
            for (TabInLimitLx tx : tabInLimitLxs) {
                int i = 0;
                for (; i < data.size(); i++) {
                    List<Object> list = data.get(i);
                    String lx = (String) list.get(0);
                    if (StringUtils.equalsIgnoreCase(lx, tx.getPrjItem())) {
                        if (!tx.getIsAdd()) {
                            data.remove(i);
                            i = -1;
                        }
                    }


                }
            }
        }
        Map<String, Object> map = new HashMap<>(16);
        map.put("rvalue", data);
        return map;
    }

    @Override
    public Map<String, Object> listExistData(ExistDataQuery existDataQuery) {
        Integer years = existDataQuery.getYears();
        Integer months = existDataQuery.getMonths();
        String tableName = existDataQuery.getTableName();
        String distNo = existDataQuery.getDistNo();
        String[] lxs = StringUtils.split(existDataQuery.getLxs(), ",");

        if (Objects.isNull(lxs)) {
            lxs = ArrayUtils.toArray("null");
        }
        String[] lxNames = StringUtils.split(existDataQuery.getLxNames(), ",");
        String typeCode = existDataQuery.getTypeCode();

        User user = SecurityUtil.getLoginUser();
        user.setDists(new ArrayList<>());
        String userDistNo = user.getTjDistNo();
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);

        String msg = "";

        boolean rvalue = false;
        List<String> returnLxs = new ArrayList<>(), returnLxNames = new ArrayList<>(), returnZtIds = new ArrayList<>();
        List<Integer> returnLxIds = new ArrayList<>();
        if (lxNames != null) {
            int i = 0;
            for (String lxName : lxNames) {
                boolean existData = this.getExistData(fileList, years, months, distNo, lxName);
                if (!existData) {
                    LxOrder lxOrder = lxOrderManager.getLxOrder(years, typeCode, lxs[i]);
                    Integer lxId = lxOrder == null ? null : lxOrder.getLxId();

                    returnLxs.add(lxs[i]);
                    returnLxIds.add(lxId);
                    returnLxNames.add(lxNames[i]);
                }

                i++;
            }
        }

        if (fileList.getFileItemLinkExExsMap() != null && fileList.getFileItemLinkExs().size() > 0) {
            rvalue = (fileList.getFileItemLinkExExsMap() != null && fileList.getFileItemLinkExs().size() > 0) ? (returnLxNames.size() > 0) : !this.getExistData(fileList, years, months, distNo, null);
        } else {

            log.info("user:" + user.getUsername() + "--distname:" + user.getDistName() + "--dist:" + user.getDists().size());
            Map<Object, Object> param = new HashMap<Object, Object>();
            param.put("years", years);
            param.put("distid", distNo);
            param.put("udistid", (null == user.getDists() || user.getDists().size() == 0) ? user.getDistNo() : user.getDists().get(0).getDistId());
            param.put("distType", fileList.getRoleGrade());
            param.put("tabletype", user.getTableType().getTableType());
            Integer couts = this.isexstsDistInlimit(param);
            if (couts > 0) {
                rvalue = (fileList.getFileItemLinkExExsMap() != null && fileList.getFileItemLinkExs().size() > 0) ? (returnLxNames.size() > 0) : !this.getExistData(fileList, years, months, distNo, null);
            } else {
                rvalue = false;
                msg = "当前表不允许在此级录入！";
            }
        }


        if (StringUtils.equalsIgnoreCase(lxs[0], "调整数")) {
            for (String lxName : returnLxNames) {
                String lxNameNew = lxName.substring(0, lxName.indexOf("调整数"));
                DistEx distEx = distExManager.getDistEx(years, fileList.getTableName(), distNo, lxNameNew);
                returnZtIds.add(distEx == null ? null : distEx.getZtId());
            }
        } else {
            for (String lxName : returnLxNames) {
                DistEx distEx = distExManager.getDistEx(years, fileList.getTableName(), distNo, lxName);
                returnZtIds.add(distEx == null ? null : distEx.getZtId());
            }
        }

        Map<String, Object> map = new HashMap<>(16);
        map.put("lxs", returnLxs);
        map.put("lxIds", returnLxIds);
        map.put("lxNames", returnLxNames);
        map.put("ztIds", returnZtIds);
        map.put("rvalue", rvalue);
        map.put("msg", msg);

        return map;
    }

    @Override
    public Map<String, Object> getPreviousYearData(String typeCode, String tableName, Integer years, Integer months, String paramJson) {
        Map<Object, Object> paramMap = ListJsonUtil.queryMapJson(paramJson);
        User user = SecurityUtil.getLoginUser();
        String userDistNo = user.getTjDistNo();
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        List<FileItem> fileItems = fileList.getFileItems();
        List<String> collect = fileItems.stream().map(fileItem -> fileItem.getFieldName()).collect(Collectors.toList());
        collect.add("id");
        String filed = String.join(", ", collect);
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(filed)
                .append(" from ").append(fileList.getTableName()).append(" where ");

        String whereSql = Common.paramChange(fileList.getPriorWtr(), paramMap);
        sql.append(whereSql);

        List<Map<String, Object>> mapList = jdbcTemplatePrimary.queryForList(sql.toString());
        Map<String, Object> map = new HashMap<>(16);
        map.put("data", mapList);
        return map;
    }



    @Override
    public List<FileList> listBaseTables(String typeCode, Integer years, Integer months) {
        User user = SecurityUtil.getLoginUser();
        String userDistNo = user.getTjDistNo();

        List<FileList> fileLists = fileListManager.listFileListsOnly(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months);

        List<FileList> collects = fileLists.stream().filter(fileList -> {
            if (org.apache.commons.lang3.StringUtils.isBlank(fileList.getBelongDistNo()) ||
                    (fileList.getBelongDistNo().startsWith(userDistNo) || userDistNo.startsWith(fileList.getBelongDistNo()))) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());


        if(CollectionUtils.isEmpty(collects)){
            throw new RuntimeException("未配置基础表！");
        }

        return collects;
    }

    @Override
    public List<Integer> getDistAllGrade() {
        List<Integer> distAllGrade = distManager.getDistAllGrade();
        return distAllGrade;
    }


    private Map<String, Object> getSqlCondition(List<Condition> conditions) {
        Map<String, Object> parameterMap = new HashMap<>(16);
        String sqlParameterCondition = "";
        for (Condition condition : conditions) {
            switch (condition.getExpress()) {
                case "eq":
                    sqlParameterCondition += "and " + condition.getFieldName() + " = :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                case "ne":
                    sqlParameterCondition += "and " + condition.getFieldName() + " <> :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                case "like":
                    sqlParameterCondition += "and " + condition.getFieldName() + " like :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
//                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), "%" + condition.getFieldValue() + "%");
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), "%" + condition.getFieldValue() + "%");
                    }
                    break;
                case "likeLeft":
                    sqlParameterCondition += "and " + condition.getFieldName() + " like :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
//                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), "%" + condition.getFieldValue());
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), "%" + condition.getFieldValue());
                    }
                    break;
                case "likeRight":
                    sqlParameterCondition += "and " + condition.getFieldName() + " like :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
//                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue() + "%");
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue() + "%");
                    }
                    break;
                case "gt":
                    sqlParameterCondition += "and " + condition.getFieldName() + " > :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                case "ge":
                    sqlParameterCondition += "and " + condition.getFieldName() + " >= :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                case "lt":
                    sqlParameterCondition += "and " + condition.getFieldName() + " < :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                case "le":
                    sqlParameterCondition += "and " + condition.getFieldName() + " <= :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
//                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
                    break;
                default:
                    sqlParameterCondition += "and " + condition.getFieldName() + " = :" + condition.getFieldName();
                    switch (condition.getFieldType()) {
                        case "N":
                            parameterMap.put(condition.getFieldName(), new BigDecimal(condition.getFieldValue()));
                            break;
                        case "C":
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                            break;
                        default:
                            parameterMap.put(condition.getFieldName(), condition.getFieldValue());
                    }
            }

        }
        Map<String, Object> parameter = new HashMap<>(16);
        parameter.put("sqlParameterCondition", sqlParameterCondition);
        parameter.put("parameterMap", parameterMap);
        return parameter;
    }

    private List<CheckVO> checksStype1(FileList fileList, TableType tableType, Object[] ids, List<RuleOuter> outers, Integer years, Integer months, String distNo, Integer grade, Boolean isAllDist, Boolean isSb) {

        //System.out.println("0000.checksStype1");
        List<CheckVO> rvalue = new ArrayList<>();
        ;
        Map<Object, Object> params = new HashMap<>();
        params.put("years", years);
        params.put("months", months);
        params.put("distid", distNo);
        params.putAll(bsConfigManager.querySysParam());
        int i = 0;
        int currentLen = distNo.length();
        int nextLen = DataConstants.getMaxDistNoLength(distNo, 2);
        params.put("currentlen", currentLen);
        params.put("nextlen", nextLen);
        String keyword = "";
        //////////////////////////////////////
        StringBuffer distId = new StringBuffer();
        StringBuffer distName = new StringBuffer();
        StringBuffer lx = new StringBuffer();
        StringBuffer lxName = new StringBuffer();
        StringBuffer defCond = new StringBuffer();
        String monthsColumn = tableType.getOptType() == 1 ? " 0 as months" : "months";
        //System.out.println("outers:::"+outers.size());

        Object[] tmIds = ids;
        StringBuffer sqlbf2 = new StringBuffer();
        StringBuffer sqlbf3 = new StringBuffer();
        String sql2 = "select id from " + fileList.getTableName() + " where years=? ";
        sql2 += (isAllDist) ? "and distid like case when '" + distNo + "'='0' then '%' else '" + distNo + "%' end " : " and distid='" + distNo + "'";
        String sql3 = "select distid,distname,lx,lxname,years," + monthsColumn + " from " + fileList.getTableName() + " where id=? ";
        List<Map<String, Object>> data = null;
        boolean isCheck = false;


        for (; i < outers.size(); i++) {
            RuleOuter outer = outers.get(i);


            if (outer.getSType() != 1) {
                continue;
            }
            sqlbf2.setLength(0);
            sqlbf3.setLength(0);
            sqlbf2.append(sql2);
            sqlbf3.append(sql3);
            if (outer.getSType() == 1) {
                sqlbf2.append(" and ${ISNULL}(sumflag,'')='是' ");
                sqlbf3.append(" and ${ISNULL}(sumflag,'')='是'");

            }
            if (isCheck) {
                isCheck = false;
                tmIds = null;
            }
            if (null == tmIds || tmIds.length == 0) {

                data = jdbcTemplatePrimary.queryForList(Common.replaceFun(sqlbf2.toString()), false, new Object[]{years});
                tmIds = data.stream().map(stringObjectMap -> stringObjectMap.get("id")).collect(Collectors.toList()).toArray(new Object[data.size()]);
                if (outer.getSType() == 1) isCheck = true;
            }
            for (int k = 0; k < tmIds.length; k++) {
                Integer id = (tmIds[k] != null) ? Integer.valueOf(tmIds[k].toString()) : -1;
                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(Common.replaceFun(sqlbf3.toString()), new Object[]{id});
                //System.out.println("111."+id);
                if (list.size() == 0) {
                    continue;
                }
                distId.setLength(0);
                distName.setLength(0);
                lx.setLength(0);
                lxName.setLength(0);
//						String distId = null, distName = null, lx = null, lxName = null;
                //Integer years = null, months = null;
                if (list.size() > 0) {

                    distId.append((String) list.get(0).get("distid"));
                    distName.append((String) list.get(0).get("distname"));
                    lx.append((String) list.get(0).get("lx"));
                    lxName.append((String) list.get(0).get("lxname"));
                    years = (Integer) list.get(0).get("years");
                    months = (Integer) list.get(0).get("months");
                    params.put("distid", distId);
                    params.put("lx", lx);
                    params.put("lxname", lxName);
                    params.put("currentlen", distId.length());
                    params.put("nextlen", DataConstants.getMaxDistNoLength(distId.toString(), 2));
                }
                if (outer.getLx() != null && StringUtils.isNotEmpty(outer.getLx()) && !StringUtils.equals(outer.getLx(), lx.toString())) {
                    continue;
                }
                defCond.setLength(0);
                defCond.append(fileList.getFileItemLink() != null ? "lxName='" + lxName + "'" : "");
                BigDecimal value1 = this.analysisExpress(params, years, months, distId.toString(), outer.getDecExpress(), outer.getExCond(), tableType.getTableType(), defCond.toString());
                BigDecimal value2 = this.analysisExpress(params, years, months, distId.toString(), outer.getSourceExpress(), outer.getSourceCondExpress(), tableType.getTableType(), defCond.toString());
                BigDecimal value3 = value1.subtract(value2);//v1-v2
                if (!compareData(value1, value2, outer.getOpt())) {
                    //isCheckOk = false;
                    CheckVO checkReturnValue = new CheckVO(id, fileList.getTableName(), fileList.getShortDis(),
                            distNo.toString(), distName.toString(), lx.toString(), lxName.toString(), outer.getDetail() + "不满足！", value1, value2, value3
                    );
                    rvalue.add(checkReturnValue);
                }
            }

        }
        return rvalue;

    }


    private List<CheckVO> checksStype2(FileList fileList, TableType tableType, Object[] ids, List<RuleOuter> outers, Integer years
            , Integer months, String distNo, Integer grade, Boolean isAllDist, Boolean isSb) {

        List<CheckVO> rvalue = new ArrayList<>();

        Map<Object, Object> params = new HashMap<>();
        params.put("years", years);
        params.put("months", months);
        params.put("distid", distNo);
        params.putAll(bsConfigManager.querySysParam());
        int currentLen = distNo.length();
        int nextLen = DataConstants.getMaxDistNoLength(distNo, 2);
        params.put("currentlen", currentLen);
        params.put("nextlen", nextLen);
        String monthsColumn = tableType.getOptType() == 1 ? " 0 as months" : "months";

        StringBuffer distId = new StringBuffer();
        StringBuffer distName = new StringBuffer();
        StringBuffer lx = new StringBuffer();
        StringBuffer lxName = new StringBuffer();

        List<Map<String, Object>> data = null;

        Object[] tmIds = ids;

        StringBuffer sqlbf2 = new StringBuffer();
        StringBuffer sqlbf3 = new StringBuffer();
        String sql2 = "select id from " + fileList.getTableName() + " where years=? ";
        sql2 += (isAllDist) ? "and distid like case when '" + distNo + "'='0' then '%' else '" + distNo + "%' end " : " and distid='" + distNo + "'";
        String sql3 = "select distid,distname,lx,lxname,years," + monthsColumn + " from " + fileList.getTableName() + " where id=? ";


        for (int i1 = 0; i1 < outers.size(); i1++) {
            RuleOuter outer = outers.get(i1);

            if(outer.getSType() != 2){
                continue;
            }

            sqlbf2.setLength(0);
            sqlbf3.setLength(0);
            sqlbf2.append(sql2);
            sqlbf3.append(sql3);


            String decExpress = outer.getDecExpress();
            String decCond = outer.getExCond();
            String sourceExpress = outer.getSourceExpress();
            String sourceCond = outer.getSourceCondExpress();
            String opt = outer.getOpt();


            //解析decExpress,得到要比较的表和字段
            String temDecExpress = decExpress.substring(decExpress.indexOf("[") + 1, decExpress.indexOf("]"));
            String decExpressTableName = temDecExpress.substring(0, temDecExpress.indexOf("|"));
            String decField = temDecExpress.substring(temDecExpress.indexOf("|") + 1);
            String decCondTemp = StringUtils.isEmpty(decCond) ? null : decCond.substring(decCond.indexOf("[") + 1, decCond.indexOf("]"));



            //解析sourceExpress,得到要比较的表和字段
            String temSourceExpress = sourceExpress.substring(sourceExpress.indexOf("[") + 1, sourceExpress.indexOf("]"));
            String sourceExpressTableName = temSourceExpress.substring(0, temSourceExpress.indexOf("|"));
            String sourceField = temSourceExpress.substring(temSourceExpress.indexOf("|") + 1);
            String sourceCondTemp = StringUtils.isEmpty(sourceCond) ? null : sourceCond.substring(sourceCond.indexOf("[") + 1, sourceCond.lastIndexOf("]"));

            String[] sourceFields = sourceField.split(",");

            //解析opt
            String[] optList = opt.split(",");
            if(optList.length < 2){
                throw new RuntimeException("配置不匹配!");
            }

            String decOpt = optList[0];
            String sourceOpt = optList[1];

            //如果需要处理数据的id列表为空
            if (null == tmIds || tmIds.length == 0) {

                //如果指定了额外查询条件
                //是否指定了类型 则加上类型查询条件
                if(decCondTemp != null){
                    String decExtraName = decCondTemp.split("\\|")[0];
                    String decExtraValue = decCondTemp.split("\\|")[1];
                    sqlbf2.append(" and " + decExtraName + " = '" + decExtraValue + "' ") ;
                }

                //查询数据的id
                data = jdbcTemplatePrimary.queryForList(Common.replaceFun(sqlbf2.toString()), new Object[]{years});
                tmIds = data.stream().map(stringObjectMap -> stringObjectMap.get("id")).collect(Collectors.toList()).toArray(new Object[data.size()]);
            }

            //循环查询出需要稽核的当前表的所有数据
            for (int i = 0; i < tmIds.length; i++) {

                //根据id查询出相关的数据信息
                Integer id = (tmIds[i] != null) ? Integer.parseInt(tmIds[i].toString()) : -1;
                List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(Common.replaceFun(sqlbf3.toString()), new Object[]{id});

                //没查询数据跳过
                if (list.size() <= 0) {
                    continue;
                }

                distId.setLength(0);
                distName.setLength(0);
                lx.setLength(0);
                lxName.setLength(0);

                if (list.size() > 0) {
                    distId.append((String) list.get(0).get("distid"));
                    distName.append((String) list.get(0).get("distname"));
                    lx.append((String) list.get(0).get("lx"));
                    lxName.append((String) list.get(0).get("lxname"));
                    years = (Integer) list.get(0).get("years");
                    months = (Integer) list.get(0).get("months");
                    params.put("distid", distId);
                    params.put("lx", lx);
                    params.put("lxname", lxName);
                    params.put("currentlen", distId.length());
                    params.put("nextlen", DataConstants.getMaxDistNoLength(distId.toString(), 2));
                }


                //查询是否符合条件和结果值
                //1 符合表达式的条件
                //0 不符合
                String dec = " select case when sum(" + decField + ") " + decOpt + " then 1 else 0 end as sumvalue" +
                        ", sum(" + decField + ") as decvalue from " + decExpressTableName
                        + " where 1=1 and id =" + id;

                List<Map<String, Object>> decList = jdbcTemplatePrimary.queryForList(dec);


                //如果符合表达式一，则需要判断表达式二
                if(decList.get(0).get("sumvalue").equals(1)){
                    //表达式一的值
                    BigDecimal value1 = (BigDecimal) decList.get(0).get("decvalue");

                    StringBuilder sourceIdSql = new StringBuilder();
                    StringBuilder sourceDataSql = new StringBuilder();

                    sourceIdSql.append("select id from " + sourceExpressTableName + " where years=? ");
                    sourceIdSql.append((isAllDist) ? "and distid like case when '" + distNo + "'='0' then '%' else '" + distNo + "%' end " : " and distid='" + distNo + "'");
                    sourceDataSql.append("select distid,distname,lx,lxname,years," + monthsColumn + " from " + sourceExpressTableName + " where id=? ");
                    List<Map<String, Object>> sourceDataList = null;
                    Object[] sourceIds = null;

                    //如果指定了额外查询条件
                    //是否指定了类型 则加上类型查询条件
                    if(sourceCondTemp != null){
                        //可能指定了多个
                        String[] sourceCondList = sourceCondTemp.split("\\]\\[");

                        //如果是多个 拼接多个条件
                        if(sourceCondList.length > 1){
                            sourceIdSql.append( " and ( ");
                            for (int i2 = 0; i2 < sourceCondList.length; i2++) {
                                String decExtraName = sourceCondList[i2].split("\\|")[0];
                                String decExtraValue = sourceCondList[i2].split("\\|")[1];
                                sourceIdSql.append(decExtraName + " = '" + decExtraValue + "' ");
                                if(i2 < sourceCondList.length - 1){
                                    sourceIdSql.append(" or ");
                                }
                            }
                            sourceIdSql.append(" ) ");
                        } else {
                            //如果是单个
                            String decExtraName = sourceCondTemp.split("\\|")[0];
                            String decExtraValue = sourceCondTemp.split("\\|")[1];
                            sourceIdSql.append(" and " + decExtraName + " = '" + decExtraValue + "' ") ;
                        }
                    }

                    //查询出表达式二表的数据
                    sourceDataList = jdbcTemplatePrimary.queryForList(Common.replaceFun(sourceIdSql.toString()), new Object[]{years});
                    sourceIds = sourceDataList.stream().map(stringObjectMap -> stringObjectMap.get("id")).collect(Collectors.toList()).toArray(new Object[sourceDataList.size()]);

                    //如果表达式二的表有数据
                    if(sourceIds.length > 0){

                        for (int sourceIdInt = 0; sourceIdInt < sourceIds.length; sourceIdInt++) {

                            //表达式二可能存在多个字段的比较
                            StringBuilder sourceSql = new StringBuilder(" select ");
                            for (int i2 = 0; i2 < sourceFields.length; i2++) {
                                sourceSql.append("case when sum(" + sourceFields[i2] + ") "
                                        + sourceOpt
                                        + " then 1 else 0 end as sumflag" + i2
                                        + ", sum(" + sourceFields[i2] + ") as sumvalue" + i2);
                                if(i2 < sourceFields.length - 1){
                                    sourceSql.append(", ");
                                }
                            }
                            sourceSql.append(" from " + sourceExpressTableName
                                    + " where 1=1 and id = " + sourceIds[sourceIdInt]);

                            List<Map<String, Object>> sourceList = jdbcTemplatePrimary.queryForList(sourceSql.toString());

                            //逐个进行表达式二的字段判断
                            for (Map<String, Object> map : sourceList) {
                                for (int i2 = 0; i2 < map.keySet().size(); i2++) {
                                    Integer sumFlag = (Integer) map.get("sumflag" + i2);
                                    BigDecimal sumValue = (BigDecimal) map.get("sumvalue" + i2);

                                    if(Objects.isNull(sumValue)){
                                        sumValue = new BigDecimal(0);
                                    }

                                    if(Objects.isNull(sumFlag)){
                                        continue;
                                    }

                                    if(sumFlag.equals(0)){
                                        CheckVO checkReturnValue = new CheckVO(id, fileList.getTableName(), fileList.getShortDis(),
                                                distId.toString(), distName.toString(), lx.toString()
                                                , lxName.toString()
                                                ,  outer.getDetail()+ "不满足！", value1
                                                , sumValue, value1.subtract(sumValue));
                                        rvalue.add(checkReturnValue);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return rvalue;

    }

    private String analysisExpress(String sqlzb, int years, int months, String distNo, FileList fileList, Boolean isAllDist, String express, String cond, String defCond, TableType tableType) {

        StringBuffer sbf = new StringBuffer();

        String otherExpress = express;
        String otherCond = cond;
        String value = "";
        String sql = null;
        String groupSql = "";
        String sqlzbHeand = " select b.rid,b.zid,b.years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "b.months," : "") + "b.tabledec,b.shortdist, b.distid,b.distname,b.lx,b.lxname,b.detail,b.opt";
        sqlzb += ")b";
        try {

            //BigDecimal zero = new BigDecimal("0.0");
            String symbol = "";
            String tabStr = "";
            String sqlon = "";
            if (otherExpress.indexOf("[") == -1) value = otherExpress + "+0.1-0.1";

            int i = 0;
            while (otherExpress.indexOf("[") != -1) {
                i++;
                tabStr = "r" + i;
                String tmpExpress = otherExpress.substring(otherExpress.indexOf("[") + 1, otherExpress.indexOf("]"));
                String tmpCond = StringUtils.isEmpty(otherCond) ? null : otherCond.substring(otherCond.indexOf("[") + 1, otherCond.indexOf("]"));
                String expressTableName = tmpExpress.substring(0, tmpExpress.indexOf("|")), expressFieldName = tmpExpress.substring(tmpExpress.indexOf("|") + 1, tmpExpress.length());

                //解析cond
                String condFieldName = tmpCond == null ? null : tmpCond.substring(0, tmpCond.indexOf("|"));
                String subCond = tmpCond == null ? null : tmpCond.substring(tmpCond.indexOf("|") + 1);
                String condFieldValue = null, condOptChar = "=";
                boolean isEquals = true, isThreeColumn = false;
                if (subCond == null || subCond.indexOf("|") == -1) {
                    condFieldValue = StringUtils.isNumeric(subCond) ? subCond : "'" + subCond + "'";
                } else {
                    condFieldValue = subCond.substring(0, subCond.indexOf("|"));
                    String v = subCond.substring(subCond.indexOf("|") + 1, subCond.length());

                    isThreeColumn = true;
                    if (!StringUtils.equals(v, "1")) {
                        isEquals = false;
                        condOptChar = "<>";
                    }

                    condFieldValue = "'" + condFieldValue + "'";
                }

                //sql
                //q 20161203

                String sqllink = "select id from fileitemlink where years=? and tablename=?";
                List<Map<String, Object>> islink = jdbcTemplatePrimary.queryForList(sqllink, new Object[]{years, expressTableName});

                //sql = "SELECT years,distid,SUM(" + expressFieldName + ") FROM " + expressTableName + " WHERE years=? and distId=?";
                sql = "SELECT years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "months," : "") + "distid";
                groupSql = " group by years," + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? "months," : "") + "distid";

                if (null != islink && islink.size() > 0 && StringUtils.isNotEmpty(defCond)) {
                    sql = sql + ",lx,lxname";
                    groupSql += ",lx,lxname";
                    sqlon = " and b.lxname=" + tabStr + ".lxname";
                    //sql += " and " + defCond;

                }

                sql = sql + ",SUM(" + expressFieldName + ") as cc FROM " + expressTableName + " where 1=1 and years=" + years + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and months=" + months : "")
                        + " and distid like '" + distNo + "%'";
                if (StringUtils.isNotEmpty(tmpCond)) {
                    sql = sql + " and " + condFieldName + condOptChar + condFieldValue;
                    //sql += " and " + condFieldName + condOptChar + condFieldValue;
                }

                symbol += otherExpress.substring(0, otherExpress.indexOf("["));

                sql += " " + groupSql;
                symbol += tabStr + ".cc";
                sqlzb += " join (" + sql + ")" + tabStr + " on  b.years=" + tabStr + ".years " + ((tableType.getOptType() == 2 || tableType.getOptType() == 3) ? " and b.months=" + tabStr + ".months" : "") + " and b.distid=" + tabStr + ".distid  " + sqlon;
                otherExpress = otherExpress.substring(otherExpress.indexOf("]") + 1);
                otherCond = StringUtils.isNotEmpty(otherCond) ? otherCond.substring(otherCond.indexOf("]") + 1) : null;
            }
            if (null == symbol || StringUtils.isEmpty(symbol)) symbol = "0";
            sqlzbHeand += ",${isnull}(" + symbol + ",0) bb from (" + sqlzb;

        } catch (Exception e) {
            log.error("analySqlStr error:" + e.toString() + sql);
        }

        return sqlzbHeand;

    }

    private BigDecimal analysisExpress(Map<Object, Object> params, Integer years, Integer months, String distNo, String express, String cond, String tableType, String defCond) {
        BigDecimal rvalue = null;
        List<DataProcessNew> processs = null;
        String sql3 = "";
        BigDecimal zero = new BigDecimal("0.0");
        String otherCond = cond;
        if (StringUtils.isNotEmpty(express)) {
            String keyword = express.substring(0, express.indexOf("["));
            String temExpress = express.substring(express.indexOf("[") + 1, express.indexOf("]"));
            String expressTableName = temExpress.substring(0, temExpress.indexOf("|"));
            String field = temExpress.substring(temExpress.indexOf("|") + 1);
            params.put("tablename", expressTableName);
            params.put("field", field);
            params.put("keyword", keyword);
            params.put("distid", distNo);

            processs = dataProcessNewManager.listDataProcessNews(keyword, tableType, -1);

            //解析cond
            String tmpCond = StringUtils.isEmpty(otherCond) ? null : otherCond.substring(otherCond.indexOf("[") + 1, otherCond.indexOf("]"));
            String condFieldName = tmpCond == null ? null : tmpCond.substring(0, tmpCond.indexOf("|"));
            String subCond = tmpCond == null ? null : tmpCond.substring(tmpCond.indexOf("|") + 1);
            String condFieldValue = null;
            String condOptChar = "=";
            boolean isEquals = true;
            boolean isThreeColumn = false;
            if (subCond == null || subCond.indexOf("|") == -1) {
                condFieldValue = StringUtils.isNumeric(subCond) ? subCond : "'" + subCond + "'";
            } else {
                condFieldValue = subCond.substring(0, subCond.indexOf("|"));
                String v = subCond.substring(subCond.indexOf("|") + 1, subCond.length());

                isThreeColumn = true;
                if (!StringUtils.equals(v, "1")) {
                    isEquals = false;
                    condOptChar = "<>";
                }

                condFieldValue = "'" + condFieldValue + "'";
            }
            ////////////////////////////////////////DataConstants.getMaxDistNoLength(distNo, grade);
            if (processs != null) {
                String sql = processs.get(0).getProcessSql();
                sql = Common.replaceByMap(sql, params).toString();
                sql = ReplaceSqlUtil.getSql(sql, keyword);
                String sqllink = "select id from fileitemlink where years=? and tablename=?";
                List<Map<String, Object>> islink = jdbcTemplatePrimary.queryForList(sqllink, false, new Object[]{years, expressTableName});

                if (null != islink && islink.size() > 0 && (StringUtils.isEmpty(tmpCond) || tmpCond.indexOf("charindex('汇总数',lxname)|0") > -1 || tmpCond.toLowerCase().indexOf("lx|汇总数|0") > -1) && StringUtils.isNotEmpty(defCond)) {
                    sql += " and " + defCond;
                }

                if (months != 0) sql += " and months=" + months;
                if (StringUtils.isNotEmpty(tmpCond)) {
                    sql += " and " + condFieldName + condOptChar + condFieldValue;
                }
                System.out.println(express + "===" + sql);
                List<Map<String, Object>> data = jdbcTemplatePrimary.queryForList(sql);
                //System.out.println(express+"express222."+sql);
                if (data.size() > 0) rvalue = new BigDecimal(data.get(0).values().toArray()[0].toString());
                if (rvalue == null) rvalue = zero;
            }
        }

        return rvalue;
    }

    private boolean compareData(BigDecimal value1, BigDecimal value2, String opt) {

        if (value1.compareTo(BigDecimal.ZERO) == 0 && value2.compareTo(BigDecimal.ZERO) == 0) {
            return true;
        }
        if (StringUtils.equals(opt, "=")) {
            return value1.compareTo(value2) == 0;
        }
        if (StringUtils.equals(opt, ">")) {
            return value1.compareTo(value2) > 0;
        }
        if (StringUtils.equals(opt, ">=")) {
            return value1.compareTo(value2) >= 0;
        }
        if (StringUtils.equals(opt, "<")) {
            return value1.compareTo(value2) < 0;
        }
        if (StringUtils.equals(opt, "<=")) {
            return value1.compareTo(value2) <= 0;
        }
        if (StringUtils.equals(opt, "<>")) {
            return value1.compareTo(value2) != 0;
        }
        return false;
    }

    private List<CheckVO> ContrastData(List<Map<String, Object>> datas) {
        List<CheckVO> rvalue = new ArrayList<>();
        for (Map<String, Object> map : datas) {
            Object key = map.get("rid");
            if (null == key || StringUtils.isEmpty(key.toString())) {
                continue;
            }
            BigDecimal value1 = new BigDecimal(map.get("b1").toString());
            BigDecimal value2 = new BigDecimal(map.get("b2").toString());
            BigDecimal value3 = new BigDecimal(map.get("b3").toString());
//			if(!compareData(value1, value2, opt)) {
            //isCheckOk = false;
            CheckVO checkReturnValue = new CheckVO(Integer.valueOf(key.toString()), map.get("tablename").toString(), map.get("shortdist").toString(),
                    map.get("distid").toString(), map.get("distname").toString(), (null == map.get("lx")) ? "" : map.get("lx").toString(), (null == map.get("lxname")) ? "" : map.get("lxname").toString(), map.get("detail").toString() + "不满足！", value1, value2, value3
            );
            rvalue.add(checkReturnValue);

//			}else{
//				listinid.add(key);
//			}
        }
        return rvalue;
    }

    private List<ExcelTemplateInfo> queryExcelTemplateInfo(ExcelTemplate excelTemplate, String fileNamePath) throws ExcelException {
        List<ExcelTemplateInfo> rvalue = new ArrayList<ExcelTemplateInfo>();
        HSSFWorkbook wb = WorkbookUtils.openWorkbook(fileNamePath);
        int sheetCount1 = wb.getNumberOfSheets();
        log.info("sheetCount1----------------------------sheetCount1:" + sheetCount1);
        System.out.println("sheetCount----------------------------sheetCount:" + sheetCount1);
        try {
            int sheetCount = wb.getNumberOfSheets();
            System.out.println("sheetCount----------------------------sheetCount:" + sheetCount);

            log.info("sheetCount----------------------------sheetCount:" + sheetCount);
            for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
                HSSFSheet sheet = wb.getSheetAt(sheetIndex);
                if (excelTemplate.getJxMode() == 2) {
                    ExcelTemplateInfo templateInfo = queryExcelTemplateInfoByUser(excelTemplate, sheet, sheetIndex);
                    if (templateInfo == null) continue;
                    rvalue.add(templateInfo);
                } else {
                    log.info("sheetIndex----------------------------sheetIndex:" + sheetIndex);
                    ExcelTemplateInfo templateInfo = queryExcelTemplateInfoSub(excelTemplate, sheet, sheetIndex);
                    log.info("templateInfo----------------------------sheetIndex:" + templateInfo + "---" + sheetIndex);
                    if (templateInfo == null) continue;
                    rvalue.add(templateInfo);
                }

            }

        } finally {
		/*	try {
				wb.close();
			} catch (IOException e) {
				log.error(""+e.getMessage());
				e.printStackTrace();
			}*/
        }

        return rvalue;
    }

    private ExcelTemplateInfo queryExcelTemplateInfoByUser(ExcelTemplate excelTemplate3, HSSFSheet sheet, int sheetIndex) {


        int[] distPosition = null;
        int[] ztNamePosition = null;
        int[] yearsPosition = null;
        int[] monthsPosition = null;
        int beginRow = 0;
        String tableName = "";
        String[] tabArry = null;
        Map<String, Object> columns = new HashMap<>();
        ExcelTemplateInfo templateInfo = null;
        int distPositionIndex = 0;
        int ztNameIndex = 0;
        int yearsIndex = 0;
        int monthsIndex = 0;
        int rownum = sheet.getFirstRowNum();
        int cellNum = 0;
        boolean isTab = false;
        boolean isList = false;
        String bfuhao = "${";
        String efuhao = "}";
        List<Map<String, Object>> tags = new ArrayList<>();
        try {
            for (; rownum <= sheet.getLastRowNum(); rownum++) {
                HSSFRow rows = sheet.getRow(rownum);
                if (rows == null) {
                    continue;
                }

                for (cellNum = rows.getFirstCellNum(); cellNum < rows.getLastCellNum(); cellNum++) {
                    HSSFCell cell = rows.getCell(cellNum);

                    if (cell == null)
                        continue;

                    String cellValue = StringUtils.trimToEmpty(cell.getStringCellValue());
                    if (cellValue == null || StringUtils.isEmpty(cellValue)) {
                        continue;
                    }
                    Map<Object, Object> tagMap = new HashMap<Object, Object>();
                    isTab = true;
                    if (!(cellValue.startsWith("$") || cellValue.startsWith("["))) {
                        continue;
                    }
                    int keyTag = cellValue.indexOf("$");
                    if (keyTag < 0) {
                        isList = false;
                        bfuhao = "[";
                        efuhao = "]";
                        keyTag = cellValue.indexOf("[");
                        if (keyTag < 0) {
                            continue;
                        }
                    } else {
                        isList = true;
                        bfuhao = "${";
                        efuhao = "}";
                    }

                    if (keyTag >= cellValue.length() - 1) {
                        continue;
                    }
                    int indexValued = cellValue.indexOf(bfuhao);
                    int indexValued2 = cellValue.lastIndexOf(efuhao);
                    Object value = null;
                    if ((indexValued == 0) && (indexValued2 > 0)) {
                        cellValue = cellValue.substring(indexValued + bfuhao.length(), indexValued2);
                    }

                    Map<String, Object> map = new HashMap<>();//(Map<Object,Object>)obj;
                    map.put("distposition", new int[]{rownum, cellNum});
                    map.put("expr", cellValue);
                    map.put("islist", isList);
                    tags.add(map);
//					if(cellValue.indexOf("${distname}")!=-1){
//						distPositionIndex=cellValue.indexOf("${distname}");
//						distPosition=new int[]{rownum,cellNum};
//
//					}


//					if(cellValue.indexOf("${ztname}")!=-1){
//						ztNameIndex=cellValue.indexOf("${ztname}");
//						ztNamePosition=new int[]{rownum,cellNum};
//					}


//					if(cellValue.indexOf("${years}")!=-1){
//						yearsIndex=cellValue.indexOf("${years}");
//						yearsPosition=new int[]{rownum,cellNum};
//					}


//					if(cellValue.indexOf("${months}")!=-1){
//						monthsIndex=cellValue.indexOf("${months}");
//						monthsPosition=new int[]{rownum,cellNum};
//					}

//					if(StringUtils.indexOf(cellValue, "begin")==0){
//						beginRow=rownum;
//						String tabStr=StringUtils.substring(cellValue, StringUtils.indexOf(cellValue,"${")+"${".length(),StringUtils.indexOf(cellValue, "}"));
//						//tableName=
//						tabArry=tabStr.split(",");
//						tableName=tabArry[0];
////						if(StringUtils.indexOf(tableName, ".")!=-1){
////							tableName=StringUtils.substring(tableName, StringUtils.indexOf(tableName, ".")+1);
////						}
//					}
//					if((rownum==beginRow+1) && (StringUtils.indexOf(cellValue, "${")==0) && (StringUtils.indexOf(cellValue,".")>0)){
//						String column=StringUtils.substring(cellValue, StringUtils.indexOf(cellValue, ".")+1, StringUtils.indexOf(cellValue, "}"));
//						columns.put(column.toUpperCase(), cellNum);
//					}
                }
            }
            //if(StringUtils.isNotEmpty(tableName) && beginRow!=0 && columns.size()!=0){
            if (isTab) {
                templateInfo = new ExcelTemplateInfo();
                templateInfo.setTableName(tableName);
                templateInfo.setBeginRow(beginRow);
                templateInfo.setColumns(columns);
                templateInfo.setDistPosition(distPosition);
                templateInfo.setZtNamePosition(ztNamePosition);
                templateInfo.setSheetIndex(sheetIndex);
                templateInfo.setDistPositionIndex(distPositionIndex);
                templateInfo.setZtNamePositionIndex(ztNameIndex);
                templateInfo.setYearsIndex(yearsIndex);
                templateInfo.setYearsPosition(yearsPosition);
                templateInfo.setMonthsIndex(monthsIndex);
                templateInfo.setMonthsPosition(monthsPosition);
                templateInfo.setTabArray(tabArry);
                templateInfo.setCellTags(tags);
            }

            //}
        } catch (Exception e) {
            log.error("sheet:" + sheetIndex + ",rownum:" + rownum + ",cell:" + cellNum);
            e.printStackTrace();
        }

        return templateInfo;
    }

    private ExcelTemplateInfo queryExcelTemplateInfoSub(ExcelTemplate excelTemplate, HSSFSheet sheet, int sheetIndex) {

        int[] distPosition = null;
        int[] ztNamePosition = null;
        int[] yearsPosition = null;
        int[] monthsPosition = null;
        int beginRow = 0;
        String tableName = "";
        Map<String, Object> columns = new HashMap<>();
        ExcelTemplateInfo templateInfo = null;
        int distPositionIndex = 0;
        int ztNameIndex = 0;
        int yearsIndex = 0;
        int monthsIndex = 0;
        int rownum = sheet.getFirstRowNum();
        int cellNum = 0;
        try {
            for (; rownum <= sheet.getLastRowNum(); rownum++) {
                HSSFRow rows = sheet.getRow(rownum);
                if (rows == null) {
                    continue;
                }

                for (cellNum = rows.getFirstCellNum(); cellNum < rows.getLastCellNum(); cellNum++) {
                    HSSFCell cell = rows.getCell(cellNum);

                    if (cell == null)
                        continue;

                    String cellValue = StringUtils.trimToEmpty(cell.getStringCellValue());
                    if (cellValue == null || StringUtils.isEmpty(cellValue)) {
                        continue;
                    }

                    if (cellValue.indexOf("${distname}") != -1) {
                        distPositionIndex = cellValue.indexOf("${distname}");
                        distPosition = new int[]{rownum, cellNum};

                    }


                    if (cellValue.indexOf("${ztname}") != -1) {
                        ztNameIndex = cellValue.indexOf("${ztname}");
                        ztNamePosition = new int[]{rownum, cellNum};
                    }


                    if (cellValue.indexOf("${years}") != -1) {
                        yearsIndex = cellValue.indexOf("${years}");
                        yearsPosition = new int[]{rownum, cellNum};
                    }


                    if (cellValue.indexOf("${months}") != -1) {
                        monthsIndex = cellValue.indexOf("${months}");
                        monthsPosition = new int[]{rownum, cellNum};
                    }

                    if (StringUtils.indexOf(cellValue, "#foreach") == 0) {
                        beginRow = rownum;

                        tableName = StringUtils.substring(cellValue, StringUtils.indexOf(cellValue, "${"), StringUtils.indexOf(cellValue, "}"));
                        if (StringUtils.indexOf(tableName, ".") != -1) {
                            tableName = StringUtils.substring(tableName, StringUtils.indexOf(tableName, ".") + 1);
                        }
                    }
                    if ((rownum == beginRow + 1) && (StringUtils.indexOf(cellValue, "${") == 0) && (StringUtils.indexOf(cellValue, ".") > 0)) {
                        String column = StringUtils.substring(cellValue, StringUtils.indexOf(cellValue, ".") + 1, StringUtils.indexOf(cellValue, "}"));
                        columns.put(column.toUpperCase(), cellNum);
                    }
                }
            }
            if (StringUtils.isNotEmpty(tableName) && beginRow != 0 && columns.size() != 0) {
                templateInfo = new ExcelTemplateInfo();
                templateInfo.setTableName(tableName);
                templateInfo.setBeginRow(beginRow);
                templateInfo.setColumns(columns);
                templateInfo.setDistPosition(distPosition);
                templateInfo.setZtNamePosition(ztNamePosition);
                templateInfo.setSheetIndex(sheetIndex);
                templateInfo.setDistPositionIndex(distPositionIndex);
                templateInfo.setZtNamePositionIndex(ztNameIndex);
                templateInfo.setYearsIndex(yearsIndex);
                templateInfo.setYearsPosition(yearsPosition);
                templateInfo.setMonthsIndex(monthsIndex);
                templateInfo.setMonthsPosition(monthsPosition);
            }
        } catch (Exception e) {
            log.error("sheet:" + sheetIndex + ",rownum:" + rownum + ",cell:" + cellNum);
            e.printStackTrace();
        }

        return templateInfo;
    }

    private String queryTabSumFlag(FileList fileList, Integer years, String distid, String tableName) {
        String sumflag = "否";
        boolean forceflag = false;
        int distlen = DataConstants.getMaxDistNoLength(distid, 1);
        String sql = "select  distinct d.distid from dist d inner join tabinLimit t 	on d.distId like t.distid+'%' and d.distType=t.grade and d.years=t.years where d.years=? and d.distid=?";
        String sql_link = "select d.distid,d.distType from dist d join  fileItemLinkExEx fx on d.distType=fx.distType and d.years=fx.years and tableName=? where d.years=?   and distId=?";
        String sql_distex = "select d.distid,d.distname from dist d join distEx dx on d.years=dx.years and d.distId=dx.distId and d.distName=dx.distName and dx.tableName=? where d.years=? and d.distId=?";
        String sql_filelist = "select d.distId,d.distName from  dist d join filelist f on d.years=f.years and d.distType=ISNULL(f.roleGrade,'村') and f.tableType='基本表' and f.tableName=? where d.years=?  and d.distId=?";


        String gzSql = "select distid,distname from dist where years=? and distid like ? and distid=?";
        try {
            if (DataConstants.ISMYSQL == 1) {
                sql = "select  distinct d.distid from dist d inner join tabinLimit t on d.distId like CONCAT(t.distid,'%') and d.distType=t.grade and d.years=t.years where d.years=? and d.distid=?";
                //sql_link="select d.distid,d.distType from dist d join  fileItemLinkExEx fx on d.distType=fx.distType and d.years=fx.years and tableName=? where d.years=?   and distId=?";
                sql_filelist = "select d.distId,d.distName from  dist d join filelist f on d.years=f.years and d.distType=IFNULL(f.roleGrade,'村') and f.tableType='基本表' and f.tableName=? where d.years=?  and d.distId=?";

            }
            List<Map<String, Object>> gd_datas = jdbcTemplatePrimary.queryForList(gzSql, new Object[]{years, DataConstants.excel_dist + "%", distid});

            if (null != gd_datas && gd_datas.size() > 0) {
                forceflag = true;

            }
            if (null != tableName && (StringUtils.equalsIgnoreCase(tableName, "rep901") || StringUtils.equalsIgnoreCase(tableName, "rep905") || StringUtils.equalsIgnoreCase(tableName, "rep906"))) {
                if (forceflag) {
                    sumflag = "否";
                } else if (distlen <= 6) {
                    sumflag = "是";
                }
            } else {
                if (distlen <= 6 && null == fileList.getFileItemLink()) {
                    List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, distid});
                    if (forceflag) {
                        sumflag = "否";
                    } else if (null == datas || datas.size() == 0) {
                        sumflag = "是";
                        List<Map<String, Object>> lists = jdbcTemplatePrimary.queryForList(sql_filelist, new Object[]{tableName, years, distid});
                        if (null != lists && lists.size() > 0) {
                            sumflag = "否";
                        } else {
                            sumflag = "是";
                        }
                    }
                } else if (distlen <= 6) {
                    sumflag = "是";
                    if (distlen <= 6 && null != fileList.getFileItemLink()) {
                        if (null != fileList.getFileItemLinkExExsMap()) {
                            List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql_link, new Object[]{tableName, years, distid});
                            if (null != datas && datas.size() > 0) {
                                sumflag = "否";
                            } else {
                                sumflag = "是";
                            }
                        } else {
                            List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql_distex, new Object[]{tableName, years, distid});
                            if (null != datas && datas.size() > 0) {
                                sumflag = "否";
                            } else {
                                sumflag = "是";
                            }
                        }

                    }
                }

            }
        } catch (Exception e) {
            log.info("error：" + e.getMessage());
        }


        return sumflag;
    }

    private String queryTabSumFlag(FileList fileList, Integer years, String distid, String tableName, String lxstr) {
        String sumflag = "否";
        int distlen = DataConstants.getMaxDistNoLength(distid, 1);
        String sql_link = "select d.distid,d.distType from dist d join  fileItemLinkExEx fx on d.distType=fx.distType and d.years=fx.years and tableName=? where d.years=?   and distId=? and fx.prjItem=?";
        String sql_distex = "select d.distid,d.distname from dist d join distEx dx on d.years=dx.years and d.distId=dx.distId and d.distName=dx.distName and dx.tableName=? where d.years=? and d.distId=?  and dx.lx=?";
        String gd_dist = "select distid,distname from dist where years=? and distid like ? and distid=?";
        boolean forceflag = false;
        try {

            List<Map<String, Object>> gd_datas = jdbcTemplatePrimary.queryForList(gd_dist, new Object[]{years, DataConstants.excel_dist + "%", distid});
            if (null != gd_datas && gd_datas.size() > 0) {
                forceflag = true;
            }
            if (null != tableName && (StringUtils.equalsIgnoreCase(tableName, "rep901") || StringUtils.equalsIgnoreCase(tableName, "rep905") || StringUtils.equalsIgnoreCase(tableName, "rep906"))) {
                if (forceflag) {
                    sumflag = "否";
                } else if (distlen <= 6) {
                    sumflag = "是";
                }
            } else {
                sumflag = "是";
                if (null != fileList.getFileItemLink()) {
                    if (null != lxstr && StringUtils.equalsIgnoreCase(lxstr, "汇总数") && forceflag) {
                        sumflag = "否";
                    } else if (null != fileList.getFileItemLinkExExsMap()) {
                        List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql_link, new Object[]{tableName, years, distid, lxstr});
                        if (null != datas && datas.size() > 0) {
                            sumflag = "否";
                        } else {
                            sumflag = "是";
                        }
                    } else {
                        List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sql_distex, new Object[]{tableName, years, distid, lxstr});
                        if (null != datas && datas.size() > 0) {
                            sumflag = "否";
                        } else {
                            sumflag = "是";
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.info("error：" + e.getMessage());
        }
        return sumflag;
    }

    private void afertExec(String keyword, Map<Object, Object> param, List<String> distIdlist) throws SQLException {
        log.info("进入:" + keyword);
        if (null == distIdlist || distIdlist.size() == 0) return;
        List<Map<String, Object>> listMap = queryExcelDataProcess(keyword);
        if (listMap == null || listMap.size() == 0) return;
        StringBuffer sql = new StringBuffer();
        String sqls = "";
        int ii = 0;
        List<String> sqlList = new ArrayList<String>();
        for (Map<String, Object> map : listMap) {
            sqls = (String) map.get("processSql");
            Integer alertType = (Integer) map.get("alertType");
            String alerts = (String) map.get("alert");
            if (alertType == 203) {
                sql.setLength(0);
                for (Object key : param.keySet()) {
                    String value = (null != param.get(key)) ? param.get(key).toString() : "";
                    sqls = StringUtils.replace(sqls.toLowerCase(), "${" + key.toString().toLowerCase() + "}", value);
                }
                String[] array = distIdlist.toArray(new String[distIdlist.size()]);
                for (int i = 0; i < distIdlist.size(); i++) {
                    //tongjiJdbcTemplate.execute(StringUtils.replace(sqls, "${distids}", array.toString()));
                    jdbcTemplatePrimary.execute(StringUtils.replace(sqls, "${distid}", distIdlist.get(i)));
                }
            }
        }
    }

    private List<Map<String, Object>> queryExcelDataProcess(String keyword) throws SQLException {
        String sql = "select keyword,processDesc,processSql,disId,isAlert,alertType,alert,visible,cruxColumn" +
                " from excel_dataProcess where keyword=? and visible=1 order by disId";
        List<Map<String, Object>> listMap = jdbcTemplatePrimary.queryForList(sql, new Object[]{keyword});

        return listMap;
    }

    private List<List<Object>> getAddDistEx(FileList fileList, int years, String tableName, String distNo) {
        List<String> dist = getDistGrade(years, distNo);

        String sql = "SELECT lx, lxname FROM distEx where years=? and tableName=? and distId=?";
        List<Map<String, Object>> data1 = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, tableName, distNo});

        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> map : data1) {
            List<Object> objectList = new ArrayList<>();
            for (Object value : map.values()) {
                objectList.add(value);
            }
            data.add(objectList);
        }

        //为空时，取fileItemLinkExEx
        if (data.size() == 0 && dist.size() > 0) {
            String distName = dist.get(1), distGrade = dist.get(2);
            if (StringUtils.isNotEmpty(distGrade)) {
                Map<String, List<FileItemLinkExEx>> fileItemLinkExExsMap = fileList.getFileItemLinkExExsMap();
                if (!CollectionUtils.isEmpty(fileItemLinkExExsMap)) {
                    List<FileItemLinkExEx> fileItemLinkExExs = fileItemLinkExExsMap.get(distGrade);
                    if (fileItemLinkExExs != null) {
                        for (FileItemLinkExEx fileItemLinkExEx : fileItemLinkExExs) {
                            List<Object> dataSub = new ArrayList<>();

                            dataSub.add(fileItemLinkExEx.getPrjItem());
                            dataSub.add(distName + fileItemLinkExEx.getPrjItem());
                            data.add(dataSub);
                        }
                    }
                }

            }
        }
        String sql1 = "select prjItem from fileItemLinkExEx_tzx where visible=1 and years=? and tablename=? and distType=?";

        if (dist.size() > 0) {
            String distName = dist.get(1);
            List<Map<String, Object>> lists1 = jdbcTemplatePrimary.queryForList(sql1, new Object[]{years, tableName, dist.get(2)});

            List<List<Object>> lists = new ArrayList<>();
            for (Map<String, Object> map : lists1) {
                List<Object> objectList = new ArrayList<>();
                for (Object value : map.values()) {
                    objectList.add(value);
                }
                lists.add(objectList);
            }


            if (null != lists && lists.size() > 0) {
                for (List<Object> list : lists) {
                    List<Object> dataSub = new ArrayList<Object>();
                    dataSub.add(list.get(0));
                    dataSub.add(distName + list.get(0));
                    data.add(dataSub);
                }
            }
        }


        //为空时，取fileItemLinkEx
//		if(data.size() == 0 && dist.size() > 0) {
//			String distName = dist.get(1);
//
//			List<FileItemLinkEx> fileItemLinkExs = fileList.getFileItemLinkExs();
//			if(fileItemLinkExs != null) {
//				for(FileItemLinkEx fileItemLinkEx:fileItemLinkExs) {
//					List<Object> dataSub = new ArrayList<Object>();
//
//					dataSub.add(fileItemLinkEx.getPrjItem());
//					dataSub.add(distName + fileItemLinkEx.getPrjItem());
//
//					data.add(dataSub);
//				}
//			}
//		}

        return data;
    }

    private List<String> getDistGrade(int years, String distNo) {
        List<String> rvalue = new ArrayList<>();

        String sql = "SELECT distId, distName, distType FROM dist WHERE years=? and distId=?";
        List<Map<String, Object>> data = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, distNo});
        if (data.size() > 0) {
            rvalue.add((String) data.get(0).values().toArray()[0]);
            rvalue.add((String) data.get(0).values().toArray()[1]);
            rvalue.add((String) data.get(0).values().toArray()[2]);
        }

        return rvalue;
    }

    private Boolean getExistData(FileList fileList, int years, int months, String distNo, String lxName) {
        boolean existsLx = fileList.getFileItemLinkExs() != null && fileList.getFileItemLinkExs().size() > 0;

        //String sql = "select id from tableType where 1=1 and optType=2 and exists(select * from fileList where years=? and tableName=? and tableType=? and typeCode=tabletype.tableType)";
        String sql = "select id from tableType where 1=1 and  optType in(2,3)  and exists(select * from fileList where years=? and tableName=? and tableType=? and typeCode=tabletype.tableType)";
        boolean existsMonth = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, fileList.getTableName(), fileList.getTableType()}).size() > 0;

        //参数
        Object[] parameters = new Object[2 + (existsMonth ? 1 : 0) + (existsLx ? 1 : 0)];
        parameters[0] = years;
        parameters[1] = distNo;
        int i = 2;
        if (existsMonth) {
            parameters[i] = months;
            i++;
        }
        if (existsLx) {
            parameters[i] = lxName;
            i++;
        }

        //sql
        sql = "select id from " + fileList.getTableName() + " where years=? and distId=?";
        if (existsMonth) sql += " and months=?";
        if (existsLx) sql += " and lxName=?";


        return jdbcTemplatePrimary.queryForList(sql, parameters).size() != 0;
    }

    private Integer isexstsDistInlimit(Map<Object, Object> param) {
        Integer rvalue = 0;
        try {

            String sql = "${dist_tlimit}";
            sql = Common.replaceFun1(sql, this.funcontrast(2));
            if (null == param || param.size() == 0) {
                return rvalue;
            }
            for (Object key : param.keySet()) {
                sql = StringUtils.replace(sql.toLowerCase(), "${" + key.toString().toLowerCase() + "}", param.get(key).toString());
            }

            List<Map<String, Object>> list = jdbcTemplatePrimary.queryForList(sql);
            if (null != list && list.size() > 0) {
                rvalue = Integer.parseInt(list.get(0).values().toArray()[0].toString());
            }
        } catch (DataAccessException e) {
            log.error(" isexstsDistInlimit error:" + e.getMessage());

        }
        return rvalue;
    }

    public List<Map<String, Object>> funcontrast(int ttype) {
        List<Map<String, Object>> rvalue = null;
        String sql = "select mysql_fun, sql_fun, valuedec, sqlstr from fun_contrast where visible=1 and ttype=? order by disid";
        rvalue = jdbcTemplatePrimary.queryForList(sql, new Object[]{ttype});
        return rvalue;
    }



    @Override
    @Transactional(rollbackFor = Throwable.class)
    public SummaryVO summaryCodingRun(SummaryDTO summaryDTO) {
        Integer years = summaryDTO.getYears();
        Integer months = summaryDTO.getMonths();
        String distNo = summaryDTO.getDistNo();
        String tableName = summaryDTO.getTableName();
        String typeCode = summaryDTO.getTypeCode();
        String userDistNo = summaryDTO.getUserDistNo();
        String tableType = summaryDTO.getTypeCode();
        Integer distType = summaryDTO.getDistType();


        SumBaseDataDTO sumBaseDataDTO = new SumBaseDataDTO();
        sumBaseDataDTO.setTableType(tableType);
        sumBaseDataDTO.setYears(years);
        sumBaseDataDTO.setMonths(months);
        sumBaseDataDTO.setTableName(tableName);
        sumBaseDataDTO.setDistId(distNo);
        sumBaseDataDTO.setDistIdType(distType);



        List<FileList> fileLists = new ArrayList<>();

        SummaryVO summaryVO = new SummaryVO();
        summaryVO.setRvalue(false);

        List<Integer> grades = distManager.getDistAllGrade();

        if ("0".equals(distNo)) {
            distNo = "";
            sumBaseDataDTO.setDistId("");
        }

        //如果汇总所有表
        if (StringUtils.isEmpty(tableName)) {
            fileLists = fileListManager.listFileLists(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months, userDistNo);
        } else {
            fileLists.add(fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo));
        }

        //当前年份所有地区级别
//        Set<Integer> listDistGrades = distManager.listDistGrades(years);

        //汇总类型  默认是2
        if(distType == null){
            distType = 2;
            sumBaseDataDTO.setDistIdType(2);
        }

        //查询最大长度
//        int maxDistLen = distManager.getMaxDistNoLength(distNo, years, 1);
        int maxDistLen = distManager.getCurrMaxDistNoLength(distNo, years);

        if(distType == 4){
            if(StringUtils.isBlank(distNo)){
                maxDistLen = distManager.getCurrMaxDistNoLength("0", years);
            }
        }


        //是否存在数据
        Boolean isExistsDataType = true;

        //是否存在类型
        Boolean isExistsLx = false;




        try {
//            log.info("开始汇总数据。。。");

            for(FileList tempFileList : fileLists){
                StringBuilder columns = new StringBuilder();
                StringBuilder sumColumns = new StringBuilder();
                String fileListTableName = tempFileList.getTableName();

//                log.info("(typeCode: {}, years: {}, months: {}, fileName: {}, distNo: {})  开始汇总。。。", typeCode, years, months, fileListTableName, distNo);

                if(Objects.isNull(tempFileList.getFileItemLink())){
                    isExistsLx = false;
                } else {
                    isExistsLx = true;
                }

                List<FileItem> fileItems = tempFileList.getFileItems();

                //没有启用或者不是数值类型的不汇总
                fileItems = fileItems.stream().filter(e -> {
                    if(Objects.isNull(e.getDisFlag())
                            || !"1".equalsIgnoreCase(e.getDisFlag())
                            || !"N".equalsIgnoreCase(e.getFType())){
                        return false;
                    }
                    return true;
                }).collect(Collectors.toList());


                List<String> columnsList = fileItems.stream().map(FileItem::getFieldName).collect(Collectors.toList());
                List<String> sumColumnsList = fileItems.stream().map(fileItem -> "sum(" + fileItem.getFieldName() + ") as " + fileItem.getFieldName()).collect(Collectors.toList());

                //汇总基础数据的字段
                columns.append(String.join(",", columnsList));
                sumColumns.append(String.join(",", sumColumnsList));


                Boolean tempBit = false;
                if(distType == 4){
                    tempBit = true;
                }

                if(distType == 1 || distType == 2 || distType == 4){
                    if(isExistsDataType){
                        //拼接删除sql语句
                        StringBuilder deleteSql = new StringBuilder();
                        deleteSql.append("delete from ")
                                .append(fileListTableName)
                                .append(" where years = ")
                                .append(years);

                        if (months != null && months != 0){
                            deleteSql.append(" and months = ").append(months);
                        }

                        deleteSql.append(" and saveFlag<>'是' and sumFlag='是' ");

                        if(distType == 4){
                            if(StringUtils.isBlank(distNo)){
                                deleteSql.append(" and distId = ").append("'0'");
                            } else {
                                deleteSql.append(" and distId = ")
                                        .append("'")
                                        .append(distNo)
                                        .append("'");
                            }
                        } else {
                            deleteSql.append(" and distId like ")
                                    .append("'")
                                    .append(distNo)
                                    .append("%'");
                        }

                        if(isExistsLx){
                            deleteSql.append(" and (charindex('汇总数',lxname) <> 0 or (select count(*) from ")
                                    .append(fileListTableName)
                                    .append(" aaa where aaa.years = ")
                                    .append(fileListTableName)
                                    .append(".years");

                            if(months != null && months != 0){
                                deleteSql.append(" and months = ").append(months);
                            }

                            deleteSql.append(" and aaa.lx = ")
                                    .append(fileListTableName)
                                    .append(".lx and aaa.distId <> ")
                                    .append(fileListTableName)
                                    .append(".distId and aaa.distId like ")
                                    .append(fileListTableName)
                                    .append(".distId || '%' ) > 0 or (lxname = distname || lx and (select count(*) from ")
                                    .append(fileListTableName)
                                    .append(" aa where years = ")
                                    .append(years);

                            if(months != null && months != 0){
                                deleteSql.append(" and months = ").append(months);
                            }

                            deleteSql.append(" and distId = ")
                                    .append(fileListTableName)
                                    .append(".distId and aa.lx=")
                                    .append(fileListTableName)
                                    .append(".lx) > 1))");

                        } else {
                            deleteSql.append(" and ((select count(*) from ")
                                    .append(fileListTableName)
                                    .append(" aaa where aaa.years = ")
                                    .append(fileListTableName)
                                    .append(".years and ")
                                    .append(" aaa.distId <> ")
                                    .append(fileListTableName)
                                    .append(".distId and aaa.distId like ")
                                    .append(fileListTableName)
                                    .append(".distId || '%') > 0)");
                        }

                        //删除基础数据
//                        log.info("执行语句：{}", deleteSql.toString());
                        jdbcTemplatePrimary.update(deleteSql.toString());


                        //基础模式
                        int tempInt = maxDistLen;

                        for(int i = grades.size() - 2; i >= 0; i--){
                            if(distNo.length() <= tempInt){

                                sumBaseDataDTO.setTableName(fileListTableName);
                                sumBaseDataDTO.setColumns(columns.toString());
                                sumBaseDataDTO.setSumColumns(sumColumns.toString());
                                sumBaseDataDTO.setDistIdInt(tempInt);
                                sumBaseDataDTO.setMaxDistIdInt(maxDistLen);
                                sumBaseDataDTO.setDistIdLen(0);
                                sumBaseDataDTO.setDistIdType(1);
                                sumBaseDataDTO.setLx("");
                                sumBaseDataDTO.setIsHzsOtherLx(false);
                                sumBaseDataDTO.setExistsDataType(isExistsDataType);
                                sumBaseDataDTO.setIsExistsLx(isExistsLx);
                                sumBaseDataDTO.setLinkDist(tempBit);
                                sumBaseDataDTO.setParentLx("");

                                if(tempInt == 1){
                                    sumOffBeat(sumBaseDataDTO);
                                } else {
                                    sumBaseData(sumBaseDataDTO);
                                }

                                if(isExistsLx){
                                    String parentLxsSql = "select parentLx,parentLxId " +
                                            "from (select parentLx,(select lxid from lxorder a where a.lx=lxorder.parentLx " +
                                            "and a.years=lxorder.years and a.typecode=lxorder.typecode) parentLxId " +
                                            "from lxorder where years=? and typecode=? and parentlx is not null and parentLx<>'' " +
                                            "group by parentlx,years,typeCode) aaa order by parentLxId desc";

                                    List<String> parentLx = jdbcTemplatePrimary.queryForList(parentLxsSql, new Object[]{years, tableType}, String.class);
//                                List<String> parentLx = jdbcTemplatePrimary.queryForList(parentLxsSql, String.class);
                                    if(!CollectionUtils.isEmpty(parentLx)){
                                        for (String lx : parentLx) {
                                            sumBaseDataDTO.setParentLx(lx);
                                            if(tempInt == 1){
                                                sumOffBeat(sumBaseDataDTO);
                                            } else {
                                                sumBaseData(sumBaseDataDTO);
                                            }
                                        }
                                    }
                                }
                            }

                            tempInt = grades.get(i);
                        }

                        //经济社汇总数
                        //汇总数
                        if(isExistsLx){

                            sumBaseDataDTO.setDistIdLen(0);
                            sumBaseDataDTO.setDistIdType(0);
                            sumBaseDataDTO.setLx("汇总数");
                            sumBaseDataDTO.setIsHzsOtherLx(true);
                            sumBaseDataDTO.setParentLx("");

                            if(StringUtils.isBlank(distNo)){
                                sumOffBeat(sumBaseDataDTO);
                                sumBaseDataDTO.setIsHzsOtherLx(false);
                                sumOffBeat(sumBaseDataDTO);
                            }else{
                                sumBaseData(sumBaseDataDTO);
                                sumBaseDataDTO.setIsHzsOtherLx(false);
                                sumBaseData(sumBaseDataDTO);
                            }
                        }
                    }
                }

                StringBuilder updateSql = new StringBuilder();
                updateSql.append("update ")
                        .append(fileListTableName)
                        .append(" set gradeId=dist.distType from dist where ")
                        .append(fileListTableName)
                        .append(".years=dist.years and ")
                        .append(fileListTableName)
                        .append(".years = ")
                        .append(years)
                        .append(" and ")
                        .append(fileListTableName)
                        .append(".distId = dist.distId and ")
                        .append(fileListTableName)
                        .append(".distId");

                if(distType == 4){
                    updateSql.append(" = ")
                            .append("'")
                            .append(distNo)
                            .append("'");
                } else {
                    updateSql.append(" like ")
                            .append("'")
                            .append(distNo)
                            .append("%'");
                }

//                log.info("执行语句：{}", updateSql.toString());
                jdbcTemplatePrimary.update(updateSql.toString());

                StringBuffer dataProcessSql = new StringBuffer();
                dataProcessSql.append("select processSql from dataProcess where hzRun = 1 and tableType = '")
                        .append(tableType)
                        .append("' and years = ")
                        .append(years)
                        .append(" and (tableName = '")
                        .append(fileListTableName)
                        .append("' or isnull(tableName,'') = '' or tableName = '')")
                        .append(" order by orderId");


                List<String> dataProcessList = jdbcTemplatePrimary.queryForList(dataProcessSql.toString(), String.class);
                if(!CollectionUtils.isEmpty(dataProcessList)){
                    for (String temp : dataProcessList) {
                        String newTemp = temp.replaceAll(":年", years.toString())
                                .replaceAll(":月", months.toString())
                                .replaceAll(":地区", distNo)
                                .replaceAll(":id", "0")
                                .replaceAll("#tableType#", tableType)
                                .replaceAll("#distType#", distType.toString());
                        jdbcTemplatePrimary.update(newTemp);
                    }
                }
            }

            summaryVO.setRvalue(true);
        }catch (Exception e){
            throw e;
        }

        return summaryVO;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void sumBaseData(SumBaseDataDTO sumBaseDataDTO){
        String tableType = sumBaseDataDTO.getTableType();
        Integer years = sumBaseDataDTO.getYears();
        Integer months = sumBaseDataDTO.getMonths();
        String tableName = sumBaseDataDTO.getTableName();
        String distId = sumBaseDataDTO.getDistId();
        String columns = sumBaseDataDTO.getColumns();
        String sumColumns = sumBaseDataDTO.getSumColumns();
        Integer distIdInt = sumBaseDataDTO.getDistIdInt();
        Integer maxDistIdInt = sumBaseDataDTO.getMaxDistIdInt();
        Integer distIdLen = sumBaseDataDTO.getDistIdLen();
        Integer distIdType = sumBaseDataDTO.getDistIdType();
        String lx = sumBaseDataDTO.getLx();
        Boolean isHzsOtherLx = sumBaseDataDTO.getIsHzsOtherLx();
        Boolean existsDataType = sumBaseDataDTO.getExistsDataType();
        Boolean isExistsLx = sumBaseDataDTO.getIsExistsLx();
        Boolean linkDist = sumBaseDataDTO.getLinkDist();
        String parentLx = sumBaseDataDTO.getParentLx();

        StringBuilder insert = new StringBuilder();
        insert.append("insert into ").append(tableName).append(" (years, ");

        if(months != null && months != 0){
            insert.append(" months, ");
        }

        insert.append("saveFlag,sumFlag,balFlag,distId,distName,");
        if(isExistsLx){
            insert.append("lx,lxname,lxid,");
        }
        insert.append(columns).append(" ) select ").append(years).append(" as years, ");
        if(months != null && months != 0){
            insert.append(months).append(" as months, ");
        }
        insert.append("'否' as saveFlag,'是' as sumFlag,'否' as balFlag,");

        if(!"汇总数".equalsIgnoreCase(lx)){
            insert.append(" left(distId, ")
                    .append(distIdInt)
                    .append(") as distId, (select top 1 distName from dist where years = ")
                    .append(years)
                    .append(" and distId = left(")
                    .append(tableName)
                    .append(".distId,").append(distIdInt).append(")) as distName ");

            if(isExistsLx){
                if(StringUtils.isBlank(parentLx)){
                    insert.append(", lx");
                } else {
                    insert.append(", '").append(parentLx).append("' as lx");
                }

                insert.append(", CONCAT((select top 1 distName from dist where years = ")
                        .append(years)
                        .append(" and distId = left(")
                        .append(tableName)
                        .append(".distId,")
                        .append(distIdInt)
                        .append(")), ");

                if(StringUtils.isBlank(parentLx)){
                    insert.append(" lx )");
                } else {
                    insert.append("'").append(parentLx).append("')");
                }

                insert.append(" as lxname,(select lxid from lxorder where typeCode = '")
                        .append(tableType)
                        .append("' and years= ")
                        .append(years);

                if(StringUtils.isBlank(parentLx)){
                    insert.append(" and lx = ").append(tableName).append(".lx) as lxid");
                } else {
                    insert.append(" and lx = '").append(parentLx).append("') as lxid ");
                }
            }
        } else {
            insert.append(" distId,distName ");

            if(isExistsLx){
                insert.append(" , ");

                if(!isHzsOtherLx){
                    insert.append("'汇总数' as ");
                }
                insert.append(" lx,distName ");

                if(isHzsOtherLx){
                    insert.append("|| lx");
                }

                insert.append("|| '汇总数' as lxname,(select lxid from lxorder where typeCode='")
                        .append(tableType)
                        .append("' and years = ")
                        .append(years)
                        .append(" and lx = ");

                if(!isHzsOtherLx){
                    insert.append("'汇总数'");
                } else {
                    insert.append(tableName)
                            .append(".lx");
                }
                insert.append(") as lxid");

            }
        }

        insert.append(", ")
                .append(sumColumns)
                .append(" from ")
                .append(tableName)
                .append(" where distId like '")
                .append(distId)
                .append("%'");

        if(isExistsLx){
            if((isHzsOtherLx || StringUtils.isBlank(lx)) && StringUtils.isBlank(parentLx)){
                insert.append(" and isnull(charindex('汇总数', lxname), 0) = 0");
            } else {
                insert.append(" and (isnull(charindex('汇总数',lxname), 0) = 0 or lx in (select parentLx from lxorder where years= ")
                        .append(years)
                        .append(" and typeCode = '")
                        .append(tableType)
                        .append("' and parentLx is not null and parentLx <> ''))");
            }

            if(StringUtils.isBlank(parentLx)
                    && !isHzsOtherLx
                    && "汇总数".equalsIgnoreCase(lx)){
                insert.append("  and lx not in (select lx from lxorder where years = ")
                        .append(years)
                        .append(" and typeCode = '")
                        .append(tableType)
                        .append("' and parentLx is not null and parentLx <> '')");
            }
        }

        insert.append(" and years = ").append(years);


        if(!"汇总数".equalsIgnoreCase(lx)){
            insert.append(" and len(distId) = ");
            if(distIdType == 0){
                insert.append(distIdInt);
            } else if (distIdType == 1) {
                if(distIdInt < maxDistIdInt){
                    insert.append("(select top 1 LEN(distId) from dist where LEN(distId)>")
                            .append(distIdInt)
                            .append(" + 1 group by LEN(distId) order by LEN(distId))");
                } else {
                    insert.append(distIdInt);
                }
            } else if (distIdType == 2 || linkDist) {
                insert.append(maxDistIdInt);
            } else if (distIdType == 3){
                insert.append(distIdLen);
            }
        } else {
            if(linkDist){
                insert.append(" and len(distId) = ").append(distId.length());
            }
        }

        if(StringUtils.isNotBlank(lx) && !"汇总数".equalsIgnoreCase(lx) && isExistsLx){
            insert.append(" and lx = '").append(lx).append("'");
        }

        if(months != null && months != 0){
            insert.append(" and months = ").append(months);
        }

        if(!"汇总数".equalsIgnoreCase(lx)){
            if(isExistsLx){
                insert.append(" and not exists ( select concat(distId, lx) from ")
                        .append(tableName);
            } else {
                insert.append(" and left(distId, ")
                        .append(distIdInt)
                        .append(") not in (select distId from ")
                        .append(tableName);
            }

            insert.append(" aa where years = ")
                    .append(years)
                    .append(" and distId like '")
                    .append(distId)
                    .append("%'");

            if(months != null && months != 0){
                insert.append(" and months = ").append(months);
            }

            if(StringUtils.isNotBlank(lx) && isExistsLx){
                insert.append(" and lx = '")
                        .append(lx)
                        .append("'");
            }

            if(isExistsLx){
                insert.append(" and aa.distId = left(")
                        .append(tableName)
                        .append(".distId, ")
                        .append(distIdInt)
                        .append(") and aa.lx = ");

                if(StringUtils.isBlank(parentLx)){
                    insert.append(tableName).append(".lx");
                } else {
                    insert.append("'")
                            .append(parentLx)
                            .append("'");
                }
            }

            insert.append(")");

            //基础模式
            if (isExistsLx && StringUtils.isBlank(lx) && StringUtils.isBlank(parentLx)){

                insert.append("and lx not in (select lx from lxorder where years=")
                        .append(years)
                        .append(" and typecode = '")
                        .append(tableType)
                        .append("' and not (parentLx is null or parentLx = ''))");
            }


            //村级汇总数
            if(isExistsLx && StringUtils.isBlank(lx) && StringUtils.isNotBlank(parentLx)){

                insert.append(" and lx in (select lx from lxorder where years = ")
                        .append(years)
                        .append(" and typecode = '")
                        .append(tableType)
                        .append("' and parentLx = '")
                        .append(parentLx)
                        .append("' union select '")
                        .append(parentLx)
                        .append("' lx where not exists(select id from ")
                        .append(tableName)
                        .append(" where years = ")
                        .append(years);

                if(months != null && months != 0){
                    insert.append(" and months = ").append(months);
                }

                insert.append(" and distId like '")
                        .append(distId)
                        .append("%' and len(distId) = ");

                if(distIdType == 0){
                    insert.append(distIdInt);
                } else if (distIdType == 1) {
                    if(distIdInt < maxDistIdInt){
                        insert.append(" (select top 1 LEN(distId) from dist where LEN(distId)>")
                                .append(distIdInt)
                                .append(" + 1 group by LEN(distId) order by LEN(distId))");
                    } else {
                        insert.append(distIdInt);
                    }
                } else if (distIdType == 2 || linkDist) {
                    insert.append(maxDistIdInt);
                } else if (distIdType == 3){
                    insert.append(distIdLen);
                }

                insert.append(" and lx in (select lx from lxorder where years=")
                        .append(years)
                        .append(" and typeCode = '")
                        .append(tableType)
                        .append("' and parentLx = '")
                        .append(parentLx)
                        .append("')))");
            }

            insert.append(" group by left(distId,").append(distIdInt).append(")");

            if(isExistsLx && StringUtils.isBlank(parentLx)){
                insert.append(", lx");
            }

        } else {
            if(!isHzsOtherLx){
                insert.append(" and CONCAT(distId,'汇总数') not in (select CONCAT(distId,'汇总数')");
            } else {
                insert.append("and not exists (select CONCAT(distId,lxname)");
            }

            insert.append(" from ").append(tableName).append(" aa where years = ").append(years);

            if(months != null && months != 0){
                insert.append(" and months = ").append(months);
            }

            insert.append(" and distId like '").append(distId).append("%' and charindex('汇总数',lxname)<>0");

            if(isExistsLx){
                if(!isHzsOtherLx){
                    insert.append(" and lx = '").append(lx).append("'");
                } else {
                    insert.append(" and lx = ")
                            .append(tableName)
                            .append(".lx and distId = ")
                            .append(tableName)
                            .append(".distId");
                }
            }

            insert.append(")");

            if(isHzsOtherLx){
                insert.append(" and (select count(*) from ")
                        .append(tableName)
                        .append(" bb where bb.years = ")
                        .append(years);

                if(months != null && months != 0){
                    insert.append(" and bb.months = ").append(months);
                }

                insert.append(" and bb.lx = ")
                        .append(tableName)
                        .append(".lx and bb.distId = ")
                        .append(tableName)
                        .append(".distId) > 1");
            }

            if(isHzsOtherLx){
                insert.append(" and lx not in (select lx from lxorder where not (parentLx is null or parentLx=''))");
            }

            insert.append(" group by distId,distName ");

            if(isHzsOtherLx){
                insert.append(" ,lx");
            }
        }

        try {
//            log.info("执行语句：{}", insert.toString());
            jdbcTemplatePrimary.update(insert.toString());
        } catch (Exception exception) {
            throw exception;
        }

    }



    public void sumOffBeat(SumBaseDataDTO sumBaseDataDTO){

     String tableType = sumBaseDataDTO.getTableType();
     Integer years = sumBaseDataDTO.getYears();
     Integer months = sumBaseDataDTO.getMonths();
     String tableName = sumBaseDataDTO.getTableName();
     String distId = sumBaseDataDTO.getDistId();
     String columns = sumBaseDataDTO.getColumns();
     String sumColumns = sumBaseDataDTO.getSumColumns();
     Integer distIdInt = sumBaseDataDTO.getDistIdInt();
     Integer maxDistIdInt = sumBaseDataDTO.getMaxDistIdInt();
     Integer distIdLen = sumBaseDataDTO.getDistIdLen();
     Integer distIdType = sumBaseDataDTO.getDistIdType();
     String lx = sumBaseDataDTO.getLx();
     Boolean isHzsOtherLx = sumBaseDataDTO.getIsHzsOtherLx();
     Boolean existsDataType = sumBaseDataDTO.getExistsDataType();
     Boolean isExistsLx = sumBaseDataDTO.getIsExistsLx();
     Boolean linkDist = sumBaseDataDTO.getLinkDist();
     String parentLx = sumBaseDataDTO.getParentLx();



        StringBuilder insert = new StringBuilder();
        StringBuilder colSql = new StringBuilder();
        StringBuilder sql = new StringBuilder();

        insert.append("insert into ").append(tableName).append(" (years, ");
        if(months != null && months != 0){
            insert.append(" months, ");
        }
        insert.append("saveFlag,sumFlag,balFlag,distId,distName,");
        if(isExistsLx){
            insert.append("lx,lxname,lxid,");
        }
        insert.append(columns).append(") ");

        colSql.append("select years, ");
        sql.append("select years, ");

        if(months != null && months != 0){
            colSql.append("months, ");
            sql.append("months, ");
        }


        colSql.append("'否' as saveFlag,'是' as sumFlag,'否' as balFlag, ");
        sql.append("'否' as saveFlag,'是' as sumFlag,'否' as balFlag, ");


        if(!"汇总数".equalsIgnoreCase(lx)){
            colSql.append("distid,distName");
            sql.append("(select distid from dist where years=")
                    .append(years)
                    .append(" and LENGTH(distid)=")
                    .append(distIdInt)
                    .append(" LIMIT 1) as distId,(select distName from dist where years=")
                    .append(years)
                    .append(" and LENGTH(distId)=")
                    .append(distIdInt)
                    .append(" LIMIT 1) as distName ");

            if (isExistsLx){
                if (StringUtils.isBlank(parentLx)){
                    colSql.append(", lx");
                    sql.append(", lx");
                } else {
                    colSql.append(", '").append(parentLx).append("' as lx");
                    sql.append(", '").append(parentLx).append("' as lx");
                }

                sql.append(",CONCAT((select distName from dist where years=")
                        .append(years)
                        .append(" and len(distId)=")
                        .append(distIdInt)
                        .append(" LIMIT 1),");

                if(StringUtils.isBlank(parentLx)){
                    sql.append(" lx");
                } else {
                    sql.append(" '").append(parentLx).append("'");
                }

                colSql.append(",lxname,lxid");
                sql.append(") as lxname,(select lxid from lxorder where typeCode='")
                        .append(tableType)
                        .append("' and years = ")
                        .append(years);

                if(StringUtils.isBlank(parentLx)){
                    sql.append(" and lx=")
                            .append(tableName)
                            .append(".lx) as lxid");
                } else {
                    sql.append(" and lx='")
                            .append(parentLx)
                            .append("') as lxid");
                }
            }
        } else {
            sql.append("distId,distName");

            if(isExistsLx){
                sql.append(" , ");

                if(!isHzsOtherLx){
                    sql.append("'汇总数' as ");
                }

                sql.append(" lx,CONCAT(distName ");

                if(isHzsOtherLx){
                    sql.append(", lx");
                }

                sql.append(", '汇总数') as lxname,(select lxid from lxorder where typeCode='")
                        .append(tableType)
                        .append("' and years = ")
                        .append(years)
                        .append(" and lx = ");

                if(!isHzsOtherLx){
                    sql.append("'汇总数'");
                } else {
                    sql.append(tableName)
                            .append(".lx");
                }
                sql.append(") as lxid");
            }
        }

        colSql.append(" , ").append(sumColumns).append(" from (");
        sql.append(" , ").append(sumColumns).append(" from ( ").append(tableName);

        if ("汇总数".equalsIgnoreCase(lx)){
            sql.append(" left join ");

            if(isExistsLx){
                sql.append(" (select distId as rdistid,lx as rlx from ").append(tableName);
            } else {
                sql.append(" (select distId as rdistid from ").append(tableName);
            }

            sql.append(" aa where years=")
                    .append(years)
                    .append(" and distId like case when '")
                    .append(distId)
                    .append("' = '0' then  '%' else '")
                    .append(distId).append("%' end ");


            if(months != null && months != 0){
                sql.append(" and months = ").append(months);
            }

            if(isExistsLx && StringUtils.isNotBlank(lx)){
                sql.append(" and lx = '").append(lx).append("'");
            }

            sql.append(" ) rr");

            if(isExistsLx){
                sql.append(" on ifnull(LENGTH(left(")
                        .append(tableName)
                        .append(".distid,")
                        .append(distIdInt)
                        .append(" )),0)=ifnull(LENGTH(rr.rdistid),0) and ");
                if (StringUtils.isBlank(parentLx)){
                    sql.append(tableName).append(".lx=rr.rlx");
                } else {
                    sql.append(" rr.rlx='").append(parentLx).append("'");
                }
            } else {
                sql.append(" on ifnull(LENGTH(left('")
                        .append(tableName)
                        .append(".distid,")
                        .append(distIdInt)
                        .append(" )),0)=ifnull(LENGTH(rr.rdistid),0)");
            }

        }

        sql.append(" where  years=")
                .append(years)
                .append(" and distId like case when '")
                .append(distId).append("' = '0' then '%' else '").append(distId).append("%' end");

        if (isExistsLx){
            if((isHzsOtherLx || StringUtils.isBlank(lx)) && StringUtils.isBlank(parentLx)){
                sql.append(" and instr(lxname,''汇总数'') = 0 ");
            } else {
                sql.append(" and (instr(lxname,''汇总数'')=0 or lx in (select parentLx from lxorder where years = ")
                        .append(years)
                        .append(" and typeCode='")
                        .append(tableType)
                        .append("' and parentLx is not null and parentLx<>''))");
            }

            if (StringUtils.isBlank(parentLx) && !isHzsOtherLx && "汇总数".equalsIgnoreCase(lx)){
                sql.append(" and lx not in (select lx from lxorder where years=")
                        .append(years)
                        .append(" and typeCode='")
                        .append(tableType)
                        .append("' and parentLx is not null and parentLx<>'')");
            }
        }

    }

    protected static boolean sqlValidate(String str){
        String s = str.toLowerCase();//统一转为小写
        String badStr =
                "select|update|and|or|delete|insert|truncate|char|into|substr|ascii|declare|exec|count|master|into|drop|execute|table|"+
                        "char|declare|sitename|xp_cmdshell|like|from|grant|use|group_concat|column_name|" +
                        "information_schema.columns|table_schema|union|where|order|by|" +
                        "'\\*|\\;|\\-|\\--|\\+|\\,|\\//|\\/|\\%|\\#";//过滤掉的sql关键字，特殊字符前面需要加\\进行转义
        //使用正则表达式进行匹配
        boolean matches = s.matches(badStr);
        return matches;
    }
}
