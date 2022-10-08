package com.bright.stats.manager.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.*;
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
import java.util.stream.Collectors;

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
        String filed = String.join(", ", fileItemCollect);
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select ").append(filed).append(" from ").append(fileList.getTableName());
        StringBuffer sqlWhereStringBuffer = new StringBuffer(" where 1=1 ");
        Map<String, Object> parameterMap = new HashMap<>(16);

        sqlWhereStringBuffer.append(" and distId like :distNo ");
        if (distNo.equals("0")) {
            parameterMap.put("distNo", "%");
        } else {
            parameterMap.put("distNo", distNo + "%");
        }

        sqlWhereStringBuffer.append(" and years=:years ");
        parameterMap.put("years", years);

        if (!StringUtils.isEmpty(lx) && !lx.equals("全部")) {
            sqlWhereStringBuffer.append(" and lx=:lx ");
            parameterMap.put("lx", lx);
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
        }

        if (Objects.nonNull(pageNumber) && Objects.nonNull(pageSize) && isPage) {
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

        if (StringUtils.isEmpty(tableName)) {
            tableName = "";
        }

        if ("0".equals(distNo)) {
            distNo = "";
        }

        Map<String, Object> parameter = new LinkedHashMap<>(16);
        parameter.put("years", years);
        parameter.put("months", months);
        parameter.put("tableType", typeCode);
        parameter.put("tableName", tableName);
        parameter.put("distId", distNo);

        //4汇总当前地区，2当前地区和下级地区，1所有地区汇总
        parameter.put("distType", 2);

        parameter.put("distLength", 0);
        parameter.put("existsDataType", 1);

        StoredProcedureQuery storedProcedureQuery = entityManagerPrimary.createStoredProcedureQuery("_sumData");
        storedProcedureQuery.registerStoredProcedureParameter("years", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("months", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("tableType", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("tableName", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("distId", String.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("distType", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("distLength", Integer.class, ParameterMode.IN);
        storedProcedureQuery.registerStoredProcedureParameter("existsDataType", Integer.class, ParameterMode.IN);

        for (String parameterKey : parameter.keySet()) {
            storedProcedureQuery.setParameter(parameterKey, parameter.get(parameterKey));
        }
        boolean flag = storedProcedureQuery.execute();

        SummaryVO summaryVO = new SummaryVO();
        summaryVO.setRvalue(flag);
        return summaryVO;
    }

    @Override
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

                                    } else {
                                        values.append("'").append(StringUtils.trimToEmpty(cellValue)).append("',");
                                    }

                                    //11
                                    if (Objects.nonNull(fileItem.getIsSumColumn()) && fileItem.getIsSumColumn()) {
                                        headSql.append(fileItem.getFieldName().toUpperCase())
                                                .append("=")
                                                .append(cellValue)
                                                .append(",");
                                    } else {
                                        whereSql.append(fileItem.getFieldName())
                                                .append("='").append(cellValue).append("' and ");
                                    }
//
                                    if (temp_distid.length() > 0) {
                                        headColumns.append("distid").append(",");
                                        values.append("'").append(temp_distid).append("',");
                                        whereSql.append(" distid='").append(temp_distid).append("' and ");
                                        temp_distid.setLength(0);
                                    }
                                }


                            }
                        }
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


            if (outer.getSType() == 0) continue;
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
}
