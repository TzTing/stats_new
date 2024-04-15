package com.bright.stats.manager.impl;

import com.bright.common.pojo.query.Condition;
import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.manager.QueryCenterManager;
import com.bright.stats.pojo.po.primary.AnsTable;
import com.bright.stats.pojo.po.primary.FileItem;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.QueryCenterQuery;
import com.bright.stats.util.ListJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.beans.IntrospectionException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/3 17:57
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueryCenterManagerImpl implements QueryCenterManager {

    private final JdbcTemplate jdbcTemplatePrimary;
    private final FileListManager fileListManager;

    @Override
    public PageResult<Map<String, Object>> listTableDataForPage(QueryCenterQuery queryCenterQuery) {
        Integer years = queryCenterQuery.getYears();
        Integer months = queryCenterQuery.getMonths();
        String tableName = queryCenterQuery.getTableName();
        String lx = queryCenterQuery.getLx();
        String lxName = queryCenterQuery.getLxName();
        Integer grade = queryCenterQuery.getGrade();
        Boolean isGrade = queryCenterQuery.getIsGrade();
        Boolean isBalanced = queryCenterQuery.getIsBalanced();
        String distNo = queryCenterQuery.getDistNo();
        String typeCode = queryCenterQuery.getTypeCode();
        Integer optType = queryCenterQuery.getOptType();
        List<Condition> conditions = queryCenterQuery.getConditions();
        Integer page = queryCenterQuery.getPageNumber();
        Integer rows = queryCenterQuery.getPageSize();
        List<String> sorts = queryCenterQuery.getSorts();
        Boolean isExcel = queryCenterQuery.getIsExcel();
        String userDistNo = queryCenterQuery.getUserDistNo();

        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS, tableName, years, months, userDistNo);
        List<AnsTable> ansTables = fileList.getAnsTables();
        if (CollectionUtils.isEmpty(ansTables)) {
            throw new RuntimeException("数据公式未配置！");
        }

        Map<Object, Object> map1 = null;
        String sql = "";

        String paramSql = "";
        if (optType == 2 || optType == 3) {
            paramSql = " and months=" + months;
        }
        if (fileList.getFileItemLinkExs() != null && fileList.getFileItemLinkExs().size() > 0) {
            paramSql += " and lx='" + lx + "'";
        }

        int totalCount = ansTables.size();  //公式总数
        // pageSize * pageNo - (pageSize - 1) + pageSize - 1;
//        int maxRows = rows * page - (rows - 1) + rows - 1; //最大行数
//        int i = rows * (page - 1) - (rows - 1) + rows - 1; //开始行数

        int maxRows = rows * (page + 1) - (rows - 1) + rows - 1; //最大行数
        int i = rows * (page) - (rows - 1) + rows - 1; //开始行数

        List<Map<Object, Object>> list = new ArrayList<>();
        List<Object> objs = new ArrayList<>();

        List<FileItem> fileItems = fileList.getFileItems();

        if (maxRows > totalCount) {
            maxRows = totalCount;
        }
        for (; i < maxRows; i++) {

            AnsTable ansTable = ansTables.get(i);
            try {
                Map<Object, Object> map = ListJsonUtil.queryMapByBean(ansTable);
                int isize = fileItems.size();
                //map1=map;
                map1 = new HashMap<>(16);

                for (int j = 0; j < isize; j++) {
                    FileItem fItem = fileItems.get(j);
                    if (StringUtils.equalsIgnoreCase(fItem.getDisFlag(), "1") && !fItem.getIsFx()) {
                        if (isExcel) {
                            map1.put(fItem.getFieldName().toLowerCase(), map.get(fItem.getFieldName().toLowerCase()));
                        } else {
                            map1.put(fItem.getFieldName(), map.get(fItem.getFieldName().toLowerCase()));
                        }

                    }
                    if (fItem.getIsFx()) {
                        String expess = (map.get(fItem.getFieldName().toLowerCase()) == null) ? "" : map.get(fItem.getFieldName().toLowerCase()).toString();
                        if (null != expess && StringUtils.isNotEmpty(expess)) {
                            sql = this.jxExpress(expess, years, distNo, paramSql);
                            sql = sql.replace("${paramSql}", paramSql);
                            if (StringUtils.isNotEmpty(sql)) {
                                List<Map<String, Object>> listObject = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, distNo});
                                if (isExcel) {
                                    map1.put(fItem.getFieldName().toLowerCase(), (listObject != null && listObject.size() > 0) ? listObject.get(0).values().toArray()[0] : "");
                                } else {
                                    map1.put(fItem.getFieldName(), (listObject != null && listObject.size() > 0) ? listObject.get(0).values().toArray()[0] : "");
                                }
                            }
                        }


                    }
                }
                list.add(map1);

            } catch (IllegalArgumentException e) {
                log.error(" error:" + e.getMessage());
            } catch (IntrospectionException e) {
                log.error(" error:" + e.getMessage());
            } catch (IllegalAccessException e) {
                log.error(" error:" + e.getMessage());
            } catch (InvocationTargetException e) {
                log.error(" error:" + e.getMessage());
            }
        }

        Pageable pageable = PageRequest.of(page, rows);
        Page pageData = new PageImpl(list, pageable, ansTables.size());
        return PageResult.of(pageData.getTotalElements(), pageData.getContent());
    }

    /**
     * 查询分析表列表
     *
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    @Override
    public List<FileList> listAnalysisTables(String typeCode, Integer years, Integer months) {
        User user = SecurityUtil.getLoginUser();
        String userDistNo = user.getTjDistNo();

        List<FileList> fileLists = fileListManager.listFileListsOnly(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS, years, months);

        Set<FileList> collects = new LinkedHashSet<>();
        for (String tempUserDistNo : userDistNo.split(",")) {
            collects.addAll(fileLists.stream().filter(fileList -> {
                if (org.apache.commons.lang3.StringUtils.isBlank(fileList.getBelongDistNo()) ||
                        (fileList.getBelongDistNo().startsWith(tempUserDistNo) || tempUserDistNo.startsWith(fileList.getBelongDistNo()))) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList()));
        }

//        List<FileList> collects = fileLists.stream().filter(fileList -> {
//            if (org.apache.commons.lang3.StringUtils.isBlank(fileList.getBelongDistNo()) ||
//                    (fileList.getBelongDistNo().startsWith(userDistNo) || userDistNo.startsWith(fileList.getBelongDistNo()))) {
//                return true;
//            } else {
//                return false;
//            }
//        }).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(collects)){
            throw new RuntimeException("未配置基础表！");
        }

        return new ArrayList<>(collects);
    }

    private String jxExpress(String str, Integer years, String distNo, String paramSql) {
        String resql = "";
        String sql = "select id, fname, dtype, param,dftypeSql,selsql, whereSql, orderbySql, orderbyid, discribe  from ansTable_contrast where fname=? and dtype=?";
        if (StringUtils.isNotEmpty(str)) {
            String fname = StringUtils.substring(str, 0, str.indexOf("["));
            //int aa=str.indexOf(";");
            //String b=StringUtils.substring(str,0,str.indexOf(";"));
            //int a=StringUtils.substring(str,0,str.indexOf(";")).lastIndexOf("|")+1;
            //String firstType=(str.indexOf(";")!=-1 && str.indexOf(";")!=str.lastIndexOf(";"))?StringUtils.substring(str, StringUtils.substring(str,0,str.indexOf(";")).lastIndexOf("|")+1,str.indexOf(";")):"";
            String lastType = (str.lastIndexOf(";") != -1) ? StringUtils.substring(str, str.lastIndexOf(";") + 1, str.indexOf("]")) : "0";
            String value = StringUtils.substring(str, str.indexOf("[") + 1, str.lastIndexOf("]"));
            String[] value1 = StringUtils.split(value, ";");


            List<Map<String, Object>> list1 = jdbcTemplatePrimary.queryForList(sql, new Object[]{fname, (StringUtils.isNotEmpty(lastType)) ? lastType : 0});

            List<List<Object>> list = new ArrayList<>();
            for (Map<String, Object> map : list1) {
                List<Object> objectList = new ArrayList<>();
                for (Object o : map.values()) {
                    objectList.add(o);
                }
                list.add(objectList);
            }

            if (null != list) {
                int size = list.size();
                for (int i = 0; i < size; i++) {
                    List<Object> list2 = list.get(i);
                    String param = (String) list2.get(3);
                    String dfsql = (String) list2.get(4);
                    String selectSql = (String) list2.get(5);
                    String whereSql = (String) list2.get(6);
                    String orderbySql = (String) list2.get(7);


                    for (int j = 0; j < value1.length; j++) {
                        String tablename = "";
                        String columName1 = "";
                        String columName2 = "";
                        String dftype = "";
                        String[] v = StringUtils.split(value1[j], "|");
                        if (v.length > 0 && v.length > 1) {
                            String temSql = dfsql;

                            for (int k = 0; k < v.length; k++) {
                                if (k == 0) {
                                    tablename = v[k];
                                } else if (k == 1) {
                                    columName1 = v[k];
                                } else {
                                    dftype = v[k];
                                }
                                if (StringUtils.isNotEmpty(dfsql) && StringUtils.isNotEmpty(dftype) && StringUtils.isNumeric(dftype)) {
                                    temSql = temSql.replace("${paramSql}", paramSql);
                                    if (StringUtils.isNotEmpty(columName1)) {
                                        temSql = temSql.replace("${columnname}", columName1);
                                    }

                                    if (StringUtils.isNotEmpty(tablename)) {
                                        temSql = temSql.replace("${tablename}", tablename);
                                    }

                                    if (StringUtils.isNotEmpty(dftype)) {
                                        int ftype = Integer.parseInt(dftype);
                                        String fyears = years + "-" + ((ftype == 12) ? "1" : ftype);
                                        temSql = temSql.replace("${years}", fyears);
                                        temSql = temSql.replace("${distno}", distNo);
                                    }
                                }


                            }
//							if(j==0){
                            String fieldName = "columnname" + (j + 1);
                            if (StringUtils.isNotEmpty(dfsql) && StringUtils.isNotEmpty(dftype) && StringUtils.isNumeric(dftype)) {
                                selectSql = selectSql.replace("${" + fieldName + "}", temSql);
                            } else {
                                if (StringUtils.isNotEmpty(columName1)) {
                                    selectSql = selectSql.replace("${" + fieldName + "}", columName1);
                                    selectSql = selectSql.replace("${columnname}", columName1);
                                }
                                if (StringUtils.isNotEmpty(tablename)) {
                                    selectSql = selectSql.replace("${tablename}", tablename);
                                }
                            }


                        }


                    }
                    //
                    resql = selectSql;


                }
            }


        }

        return resql;

    }
}
