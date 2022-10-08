package com.bright.stats.manager.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.stats.manager.AnalysisCenterManager;
import com.bright.stats.manager.SqlInfoManager;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.SqlInfo;
import com.bright.stats.pojo.po.primary.SqlInfoItem;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.vo.SqlInfoVO;
import com.bright.stats.util.Common;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/5 9:50
 * @Description
 */
@Component
@RequiredArgsConstructor
public class AnalysisCenterManagerImpl implements AnalysisCenterManager {

    @PersistenceContext
    private EntityManager entityManagerPrimary;

    private final SqlInfoManager sqlInfoManager;

    @Override
    public List<SqlInfoVO> listAnalysisSchemes(Integer years, String typeCode) {
        List<SqlInfo> sqlInfos = sqlInfoManager.listSqlInfos(years, typeCode);
        List<SqlInfoVO> sqlInfoVOS = new ArrayList<>();
        for (SqlInfo sqlInfo : sqlInfos) {
            SqlInfoVO sqlInfoVO = new SqlInfoVO();
            sqlInfoVO.setSqlNo(sqlInfo.getSqlNo());
            sqlInfoVO.setGroupName(sqlInfo.getGroupName());
            sqlInfoVO.setModalName(sqlInfo.getModalName());
            sqlInfoVOS.add(sqlInfoVO);
        }

        List<Integer> sqlInfoVOCounts = sqlInfoVOS.stream().map(sqlInfoVO -> sqlInfoVO.getSqlNo().length()).collect(Collectors.toSet()).stream().collect(Collectors.toList());
        Collections.sort(sqlInfoVOCounts);

        List<String> groupNames = sqlInfoVOS.stream().map(sqlInfoVO -> sqlInfoVO.getGroupName()).collect(Collectors.toSet()).stream().collect(Collectors.toList());

        Map<String, List<SqlInfoVO>> map = new HashMap<>(16);
        for (String groupName : groupNames) {
            List<SqlInfoVO> list = new ArrayList<>();
            for (SqlInfoVO sqlInfoVO : sqlInfoVOS) {
                if (groupName.equals(sqlInfoVO.getGroupName())) {
                    list.add(sqlInfoVO);
                }
            }
            map.put(groupName, list);
        }

        List<SqlInfoVO> result = new ArrayList<>();
        for (String s : map.keySet()) {
            List<SqlInfoVO> sqlInfoVOList = map.get(s);
            for (SqlInfoVO sqlInfoVO : sqlInfoVOList) {
                if (2 == sqlInfoVO.getSqlNo().length()) {
                    List<SqlInfoVO> children = sqlInfoVOList.stream().filter(sqlInfoVO1 -> sqlInfoVO1.getSqlNo().length() != 2).collect(Collectors.toList());
                    sqlInfoVO.setChildren(children);
                    result.add(sqlInfoVO);
                }
            }
        }

        return result;
    }

    @Override
    public List<TableHeader> listTableHeaders(Integer years, String typeCode, String sqlNo) {
        SqlInfo sqlInfo = sqlInfoManager.getSqlInfo(years, typeCode, sqlNo);
        return sqlInfo.getTableHeaders();
    }

    @Override
    public <T> T listTableData(AnalysisCenterQuery analysisCenterQuery, boolean isPage, Class<T> typeClass) {
        Integer years = analysisCenterQuery.getYears();
        Integer months = analysisCenterQuery.getMonths();
        String distNo = analysisCenterQuery.getDistNo();
        Integer grade = analysisCenterQuery.getGrade();
        Boolean isGrade = analysisCenterQuery.getIsGrade();
        String sqlNo = analysisCenterQuery.getSqlNo();
        String unitDataType = analysisCenterQuery.getUnitDataType();
        String typeCode = analysisCenterQuery.getTypeCode();
        List<Condition> conditions = analysisCenterQuery.getConditions();
        Integer pageNumber = analysisCenterQuery.getPageNumber();
        Integer pageSize = analysisCenterQuery.getPageSize();
        List<String> sorts = analysisCenterQuery.getSorts();

        SqlInfo sqlInfo = sqlInfoManager.getSqlInfo(years, typeCode, sqlNo);
        List<SqlInfoItem> sqlInfoItems = sqlInfo.getSqlInfoItems();
        List<String> sqlInfoItemCollect = sqlInfoItems.stream().map(fileItem -> fileItem.getFieldName() + " as " + fileItem.getFieldName()).collect(Collectors.toList());
//        sqlInfoItemCollect.add("id");
        String filed = String.join(", ", sqlInfoItemCollect);

        String sqlStr = sqlInfo.getSqlStr();
        Map<Object, Object> paramMap = new HashMap<>(16);
        paramMap.put("年", years);
        paramMap.put("地区", distNo);
        paramMap.put("层级", grade);
        paramMap.put("years", years);
        paramMap.put("months", months);
        paramMap.put("distNo", distNo);

        Integer distGrade = DataConstants.getMaxDistNoLength(distNo, grade);
        paramMap.put("grade", distGrade);
        paramMap.put("unitDataType", unitDataType);

        if (isGrade) {
            paramMap.put("otherSql", "");
            paramMap.put("isgrade", 1);
        } else {
            paramMap.put("otherSql", Common.replaceFun("and ${LEN}(distid)=" + DataConstants.getMaxDistNoLength(distNo, grade)));
            paramMap.put("isgrade", 0);
        }

        sqlStr = Common.replaceParamskeynoempty(sqlStr, paramMap);
        sqlStr = Common.replaceParamsbyKeyNotNull(sqlStr, paramMap);
        sqlStr = " (" + sqlStr + ") as data ";
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select ").append(filed).append(" from ").append(sqlStr);
        StringBuffer sqlWhereStringBuffer = new StringBuffer(" where 1=1 ");

        Map<String, Object> parameterMap = new HashMap<>(16);
        List<Condition> parameterConditions = new ArrayList<>();

        if (!CollectionUtils.isEmpty(conditions)) {
            for (Condition condition : conditions) {
                for (SqlInfoItem sqlInfoItem : sqlInfoItems) {
                    if (condition.getFieldName().equalsIgnoreCase(sqlInfoItem.getFieldName())) {
                        condition.setFieldType(sqlInfoItem.getFType().toUpperCase());
                        parameterConditions.add(condition);
                    }
                }
            }
        }

        Map<String, Object> sqlCondition = this.getSqlCondition(parameterConditions);

        sqlWhereStringBuffer.append(sqlCondition.get("sqlParameterCondition"));
        parameterMap.putAll((Map<String, Object>) sqlCondition.get("parameterMap"));
        sqlStringBuffer.append(sqlWhereStringBuffer);

        StringBuffer sqlOrderByStringBuffer = new StringBuffer();
        String orderSql = sqlInfo.getOrderSql();
        if (!StringUtils.isEmpty(orderSql)) {
            sqlOrderByStringBuffer.append(" order by ");
            sqlOrderByStringBuffer.append(orderSql);
        }


        if (!CollectionUtils.isEmpty(sorts)) {
            if (StringUtils.isEmpty(orderSql)) {
                sqlOrderByStringBuffer.append(" order by ");
            } else {
                sqlOrderByStringBuffer.append(", ");
            }

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
            sqlCountStringBuffer.append(sqlStr);
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
