package com.bright.stats.manager.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.manager.StatisticsCenterManager;
import com.bright.stats.pojo.po.primary.FileItem;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.StatisticsCenterQuery;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: Tz
 * @Date: 2022/09/29 0:26
 */
@Component
@RequiredArgsConstructor
public class StatisticsCenterManagerImpl implements StatisticsCenterManager {


    @PersistenceContext
    private EntityManager entityManagerPrimary;

    private final FileListManager fileListManager;

    /**
     * 查询统计表的表信息
     *
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    @Override
    public List<FileList> listStatisticsTables(String typeCode, Integer years, Integer months) {
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

    /**
     * 分页获取统计数据
     *
     * @param statisticsCenterQuery
     * @param isPage
     * @param pageResultClass
     * @return
     */
    @Override
    public <T> T  listTableData(StatisticsCenterQuery statisticsCenterQuery, boolean isPage, Class<T> pageResultClass) {
        Integer years = statisticsCenterQuery.getYears();
        Integer months = statisticsCenterQuery.getMonths();
        String tableName = statisticsCenterQuery.getTableName();
        String lx = statisticsCenterQuery.getLx();
        String lxName = statisticsCenterQuery.getLxName();
        Integer grade = statisticsCenterQuery.getGrade();
        Boolean isGrade = statisticsCenterQuery.getIsGrade();
        Boolean isBalanced = statisticsCenterQuery.getIsBalanced();
        String distNo = statisticsCenterQuery.getDistNo();
        String typeCode = statisticsCenterQuery.getTypeCode();
        List<Condition> conditions = statisticsCenterQuery.getConditions();
        Integer pageNumber = statisticsCenterQuery.getPageNumber();
        Integer pageSize = statisticsCenterQuery.getPageSize();
        List<String> sorts = statisticsCenterQuery.getSorts();
        String userDistNo = statisticsCenterQuery.getUserDistNo();
        List<String> distNos = statisticsCenterQuery.getDistNos();

        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        List<FileItem> fileItems = fileList.getFileItems();
        List<String> fileItemCollect = fileItems.stream().map(fileItem -> fileItem.getFieldName() + " as " + fileItem.getFieldName()).collect(Collectors.toList());
        fileItemCollect.add("id as id");
        String filed = String.join(", ", fileItemCollect);
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select ").append(filed).append(" from ").append(fileList.getTableName());
        StringBuffer sqlWhereStringBuffer = new StringBuffer(" where 1=1 ");
        Map<String, Object> parameterMap = new HashMap<>(16);

        StringBuffer sqlWhereDistNo = new StringBuffer();

        int distNoLength = 0;
        for (String str : distNos) {
            if(distNoLength == 0){
                distNoLength = str.length();
                distNo = str;
            }
            if(distNoLength > str.length()) {
                distNoLength = str.length();
                distNo = str;
            }

            if(org.apache.commons.lang.StringUtils.isNotEmpty(sqlWhereDistNo.toString())){
                sqlWhereDistNo.append(" or ");
            }

            sqlWhereDistNo.append(" distId like ");
            sqlWhereDistNo.append("'" + str + "%'");
        }


        if(org.apache.commons.lang.StringUtils.isNotEmpty(sqlWhereDistNo.toString())){
            sqlWhereStringBuffer.append(" and (" + sqlWhereDistNo.toString() + ")");
        }

//        sqlWhereStringBuffer.append(" and distId like :distNo ");
//        if (distNo.equals("0")) {
//            parameterMap.put("distNo", "%");
//        } else {
//            parameterMap.put("distNo", distNo + "%");
//        }

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

//        if (Objects.nonNull(isGrade) && !isGrade) {
//            sqlWhereStringBuffer.append(" and len(distId)=:isGrade ");
//            parameterMap.put("isGrade", DataConstants.getMaxDistNoLength(distNo, grade));
//        }
//
//        if (Objects.nonNull(isBalanced) && !isBalanced) {
//            sqlWhereStringBuffer.append(" and balFlag='否' ");
//        }

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
}
