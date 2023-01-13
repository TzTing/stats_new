package com.bright.stats.manager.impl;

import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.DistExManager;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.pojo.dto.UnitDataDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.DistExQuery;
import com.bright.stats.repository.primary.DistExRepository;
import com.bright.stats.repository.primary.FileItemRepository;
import com.bright.stats.util.DataConstants;
import com.bright.stats.util.TableHeaderUtil;
import lombok.RequiredArgsConstructor;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/3 11:14
 * @Description
 */
@Component
@RequiredArgsConstructor
public class DistExManagerImpl implements DistExManager {

    @PersistenceContext
    private EntityManager entityManagerPrimary;

    private final JdbcTemplate jdbcTemplatePrimary;

    private final DistExRepository distExRepository;

    private final FileItemRepository fileItemRepository;

    private final FileListManager fileListManager;


    @Override
    public DistEx getDistEx(Integer years, String tableName, String distNo, String lxName) {
        DistEx rvalue = null;
        List<DistEx> distExs = distExRepository.findDistEx(years, tableName, distNo, lxName);
        if (distExs.size() > 0) {
            rvalue = distExs.get(0);
        }

        return rvalue;
    }

    @Override
    public PageResult<Map<String, Object>> listDistExForPage(DistExQuery distExQuery) {
        Integer years = distExQuery.getYears();
        Integer months = distExQuery.getMonths();
        String tableName = distExQuery.getTableName();
        Integer grade = distExQuery.getGrade();
        String distNo = distExQuery.getDistNo();
        String typeCode = distExQuery.getTypeCode();
        String userDistNo = distExQuery.getUserDistNo();
        Integer pageNumber = distExQuery.getPageNumber();
        Integer pageSize = distExQuery.getPageSize();

        if(StringUtils.isBlank(tableName)){
            tableName = "areaunit";
        }

        List<FileItem> fileItems = fileItemRepository.findFileItemByDisFlag(0, tableName);
        List<String> fileItemCollect = fileItems.stream().map(fileItem -> fileItem.getFieldName() + " as " + fileItem.getFieldName()).collect(Collectors.toList());
        String filed = String.join(", ", fileItemCollect);

        StringBuilder sqlCountStringBuffer = new StringBuilder();
        sqlCountStringBuffer.append("select count(*) from ")
                .append("(  select max(id) as id,DISTID,DISTNAME,YEARS,MAX(TABLENAME) AS TABLENAME,max(shortDis) as shortDis, " +
                        "LXNAME,LX,LXID,ACC_SET,ZTNAME,ZTID,ZTH_IMPORT,PASS_CODE from distEx d,   " +
                        "(select tablename as tb,shortDis from fileList where 1 = 1 ")
                .append(" and years =   ")
                .append(years)
                .append(" and  tableType='基本表' ")
                .append(" and useFlag='是' ")
                .append(" and typeCode='")
                .append(typeCode)
                .append("') f  ")
                .append(" where 1 = 1 ")
                .append(" and years =")
                .append(years)
                .append(" and  distId like '")
                .append("0".equalsIgnoreCase(distNo) ? "%" : distNo + "%")
                .append("' ")
                .append(" and len(distid) <= ")
                .append(DataConstants.getMaxDistNoLength(distNo, grade))
                .append(" and d.tableName=f.tb ")
                .append(" group by  distid,distname,years, lxname,lx,lxid,acc_set,ztname,ztid,zth_import,pass_code) a ");


        Query nativeQueryCount = entityManagerPrimary.createNativeQuery(sqlCountStringBuffer.toString());
        Long counts = Long.valueOf(nativeQueryCount.getSingleResult().toString());


        StringBuilder sqlStringBuffer = new StringBuilder();
        sqlStringBuffer.append("select ")
                .append(filed)
                .append(" from ")
                .append("(  select max(id) as id,DISTID,DISTNAME,YEARS,MAX(TABLENAME) AS TABLENAME,max(shortDis) as shortDis, " +
                        "LXNAME,LX,LXID,ACC_SET,ZTNAME,ZTID,ZTH_IMPORT,PASS_CODE from distEx d,   " +
                        "(select tablename as tb,shortDis from fileList where 1 = 1 ")
                .append(" and years =   ")
                .append(years)
                .append(" and  tableType='基本表' ")
                .append(" and useFlag='是' ")
                .append(" and typeCode='")
                .append(typeCode)
                .append("') f  ")
                .append(" where 1 = 1 ")
                .append(" and years =")
                .append(years)
                .append(" and  distId like '")
                .append("0".equalsIgnoreCase(distNo) ? "%" : distNo + "%")
                .append("' ")
                .append(" and len(distid) <= ")
                .append(DataConstants.getMaxDistNoLength(distNo, grade))
                .append(" and d.tableName=f.tb ")
                .append(" group by  distid,distname,years, lxname,lx,lxid,acc_set,ztname,ztid,zth_import,pass_code) a order by ztid");

        Query nativeQuery = entityManagerPrimary.createNativeQuery(sqlStringBuffer.toString());

        nativeQuery.setFirstResult(pageNumber * pageSize);
        nativeQuery.setMaxResults(pageSize);

        nativeQuery.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        List<Map<String, Object>> resultList = nativeQuery.getResultList();
        return PageResult.of(counts, resultList);

    }

    @Override
    public List<TableHeader> listTableHeaders(String tableName, Integer years, Integer months) {

        List<FileItem> fileItems = fileItemRepository.findFileItemByDisFlag(0, tableName);

        List<TableHeader> tableHeaders = new ArrayList<>();
        fileItems.forEach(fileItem -> {
            String[] split = fileItem.getFieldDis().split("\\|");
            for (int i = 0; i < split.length; i++) {

                TableHeader tableHeader = new TableHeader();
                tableHeader.setField(fileItem.getFieldName());
                tableHeader.setTitle(split[i]);
                tableHeader.setFieldType(fileItem.getFType());
                tableHeader.setFieldFormat(fileItem.getDisFormat());
//                    tableHeader.setSort(fileItem.getDefId());
                tableHeader.setSort(fileItem.getDisId());
                tableHeader.setWidth(fileItem.getFLen());
                if (null != fileItem.getIsFrozen() && fileItem.getIsFrozen()) {
                    tableHeader.setFixed("left");
                }

                if(fileItem.getAlign() != null) {
                    if(fileItem.getAlign() == 1){
                        tableHeader.setAlign("left");
                    }
                    if(fileItem.getAlign() == 2){
                        tableHeader.setAlign("center");
                    }
                    if(fileItem.getAlign() == 3){
                        tableHeader.setAlign("right");
                    }
                } else {
                    if ("N".equalsIgnoreCase(fileItem.getFType())) {
                        tableHeader.setAlign("right");
                    } else {
                        tableHeader.setAlign("left");
                    }
                }

                String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                tableHeader.setId(Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i);
                String[] strings = Arrays.copyOfRange(split, 0, i);
                tableHeader.setPid(Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1)));
                if (!StringUtils.isEmpty(tableHeader.getPid())) {
                    tableHeader.setWidth(null);
                }
                tableHeaders.add(tableHeader);
            }
        });
        List<TableHeader> buildTree = TableHeaderUtil.buildTree(tableHeaders);

        return buildTree;
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void save(UnitDataDTO unitDataDTO) {

        Integer years = unitDataDTO.getYears();
        Integer months = unitDataDTO.getMonths();
        String tableName = unitDataDTO.getTableName();
        String typeCode = unitDataDTO.getTypeCode();
        String userDistNo = unitDataDTO.getUserDistNo();

        if (StringUtils.isBlank(tableName)) {
            tableName = "distEx";
        }

        JSONArray insertData = JSONArray.fromObject(unitDataDTO.getInsertData());
        JSONArray updateData = JSONArray.fromObject(unitDataDTO.getUpdateData());
        JSONArray deleteData = JSONArray.fromObject(unitDataDTO.getDeleteData());


        List<FileList> fileLists = fileListManager.listFileListsByCache(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, years, months);
        final List<FileItem> fileItems = fileItemRepository.findFileItemByDisFlag(0, "areaunit");


        String sqlEx = "select id from fileItemLinkEx where years=? and prjItem=? and tableName=? and prjType=?";
        String sqlLx = "select lxid,lx  from lxorder where years=? and typeCode=? and lx=?";
        String insertSql = "INSERT INTO " + tableName + " ( ";
        StringBuilder insertSqlBuilder = new StringBuilder();
        String insertColumns = "";
        String insertValues = "";
        List<Object> insertParameters = null;
        List<Object> exParam = new ArrayList<Object>();
        String updateSetColumns = "";


        for (FileItem fileItem : fileItems) {
            if (StringUtils.isEmpty(insertColumns)) {

                if (!StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "ID")
                        && !StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "SHORTDIS")) {
                    insertColumns += fileItem.getFieldName();
                    insertValues += "?";
                }

            } else {

                if (!StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "ID")
                        && !StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "SHORTDIS")) {
                    insertColumns += "," + fileItem.getFieldName();
                    insertValues += ",?";
                }

            }


            if (StringUtils.isEmpty(updateSetColumns)) {
                if (fileItem.getIsMayKey()) {
                    updateSetColumns += fileItem.getFieldName() + "=?";
                }
            } else {
                if (fileItem.getIsMayKey()) {
                    updateSetColumns += "," + fileItem.getFieldName() + "=?";
                }
            }
        }
        insertSqlBuilder.append(insertSql).append(insertColumns).append(")values(").append(insertValues).append(")");


        String lx = "";
        List<Map<String, Object>> listMap = null;
        List<Map<String, Object>> listMapLx = null;
        for (FileList fileList : fileLists) {
            String tempTableName = fileList.getTableName();
            FileItemLink fk = fileList.getFileItemLink();
            if (fk != null) {
                String prjType = fk.getPrjType();
                for (int i = 0; i < insertData.size(); i++) {
                    exParam = new ArrayList<Object>();
                    insertParameters = new ArrayList<Object>();
                    JSONObject jo = (JSONObject) insertData.get(i);
                    lx = (String) jo.get("LX");
                    exParam.add(years);
                    exParam.add(lx);
                    exParam.add(tempTableName);
                    exParam.add(prjType);
                    listMap = jdbcTemplatePrimary.queryForList(sqlEx, exParam.toArray(new Object[exParam.size()]));

                    if (listMap.size() > 0) {
                        listMapLx = jdbcTemplatePrimary.queryForList(sqlLx, new Object[]{years, fileList.getTypeCode(), lx});

                        for (FileItem fileItem : fileItems) {

                            if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "tablename")) {
                                insertParameters.add(tempTableName);
                            } else if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "lxid")) {
                                insertParameters.add(listMapLx.get(0).get("lxid"));
                            } else {
                                if (!StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "ID")
                                        && !StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "SHORTDIS")) {
                                    insertParameters.add(jo.get(fileItem.getFieldName().toUpperCase()));
                                }
                            }
                        }
                    } else {
                        break;
                    }
                }
                if (!CollectionUtils.isEmpty(insertParameters) && insertParameters.size() > 0) {
                    KeyHolder keyHolder = new GeneratedKeyHolder();
                    List<Object> finalInsertParameters = insertParameters;
                    jdbcTemplatePrimary.update(new PreparedStatementCreator() {
                        @Override
                        public PreparedStatement createPreparedStatement(Connection arg0)
                                throws SQLException {
                            PreparedStatement ps = arg0.prepareStatement(insertSqlBuilder.toString(), Statement.RETURN_GENERATED_KEYS);
                            for (int i = 0; i < finalInsertParameters.size(); i++) {
                                ps.setObject(i + 1, (finalInsertParameters.get(i) == null || StringUtils.equals(finalInsertParameters.get(i).toString(), "null")) ? "" : finalInsertParameters.get(i));
                            }
                            return ps;
                        }

                    }, keyHolder);
                }
            }
        }



        //update
        String updateSql = "update distEx set ";
        StringBuilder updateSqlBuilder = new StringBuilder();
        //更新条件
        String updateWhere = "";
        //更新的值
        List<Object> updateParamList = null;
        String sql1 = "select * from distex where id=?";

        //循环数据
        for (int i = 0; i < updateData.size(); i++) {
            Map<String, Object> selMap = null;

            updateParamList = new ArrayList<Object>();
            JSONObject jsonObj = updateData.getJSONObject(i);
            updateWhere = "";

            for (FileItem fileItem : fileItems) {
                if (fileItem.getIsMayKey()) {
                    if (StringUtils.isEmpty(jsonObj.get(fileItem.getFieldName().toUpperCase()).toString())
                            || StringUtils.equalsIgnoreCase("null", jsonObj.get(fileItem.getFieldName().toUpperCase()).toString())) {
                        if (StringUtils.equalsIgnoreCase(fileItem.getFType(), "N")) {
                            updateParamList.add(0);
                        } else {
                            updateParamList.add(null);
                        }
                    } else {
                        if (StringUtils.equalsIgnoreCase(fileItem.getFieldName(), "LXID")) {
                            lx = (String) jsonObj.get("LX");
                            //查询单位类型
                            listMapLx = jdbcTemplatePrimary.queryForList(sqlLx, new Object[]{years, typeCode, lx});
                            if (listMapLx.size() > 0) {
                                updateParamList.add(listMapLx.get(0).get("lxid"));
                            }
                        } else {
                            updateParamList.add(jsonObj.get(fileItem.getFieldName().toUpperCase()));
                        }

                    }
                }
            }



            //查询更新的记录
            Integer id = (Integer) jsonObj.get("ID");
            selMap = jdbcTemplatePrimary.queryForMap(sql1, new Object[]{id});

            //判断当前单位是否上报
            boolean bool = isNewspaper(selMap, typeCode);
            if (!bool) {
                throw new RuntimeException("当前单位已经上报！！");
            }

            if (selMap.size() > 0) {
                for (FileItem fileItem : fileItems) {
                    if (fileItem.getIsMayKey()) {
                        if (selMap.get(fileItem.getFieldName().toLowerCase()) != null
                                && StringUtils.isNotEmpty(selMap.get(fileItem.getFieldName().toLowerCase()).toString())) {
                            if (StringUtils.isEmpty(updateWhere)) {
                                updateWhere += fileItem.getFieldName() + "=?";
                            } else {
                                updateWhere += " and " + fileItem.getFieldName() + "=?";
                            }
                            updateParamList.add(selMap.get(fileItem.getFieldName().toLowerCase()));
                        }

                    }
                }
            }

            updateSqlBuilder.append(updateSql).append(updateSetColumns).append(" where ").append(updateWhere);
            String selectSql = "";
            selectSql = "select tableName from distEx where  years=? and distid=? and distname=? and  lxname=?   group by tableName";
            List lists = jdbcTemplatePrimary.queryForList(selectSql, new Object[]{selMap.get("years"), selMap.get("distid"), selMap.get("distname"), selMap.get("lxname")});

            String updateDataSql = "";


            try {
                jdbcTemplatePrimary.update(updateSqlBuilder.toString(), updateParamList.toArray(new Object[updateParamList.size()]));

                //关联更新表
                for (int b = 0; b < lists.size(); b++) {
                    Set<String> set2 = ((Map) lists.get(b)).keySet();
                    for (String key : set2) {

                        updateDataSql = "update " + ((Map) lists.get(b)).get(key) + " set lxname=?, lx=?, statusno=0, balflag='否', ztId=?  where  years=? and distid=? and distname=? and  lxname=? ";
                        int resultNum = jdbcTemplatePrimary.update(updateDataSql, new Object[]{jsonObj.get("LXNAME"), jsonObj.get("LX"), jsonObj.get("ZTID"), selMap.get("years"), selMap.get("distid"), selMap.get("distname"), selMap.get("lxname")});
                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
                throw e;
            }

        }


        //delete
        for (int i = 0; i < deleteData.size(); i++) {

            Map<Object, Object> returnMap = new HashMap<Object, Object>();
            Map<String, Object> map = com.alibaba.fastjson2.JSONObject.parseObject(deleteData.get(i).toString(), Map.class);
            map.put("TYPECODE", typeCode);
            map.put("TABLETYPE", FileListConstant.FILE_LIST_TABLE_TYPE_BASE);
            String deleteSql = "delete  from distEx where  years=? and distId=? and distName=? and lx=? and lxname=? ";

            //查询删除记录的类型有关联的表
            List<Map<String, Object>> tableMap = fileListTabs(map);

            long countData = 0;
            String existsSql = "";

            try {

                if (null == tableMap || tableMap.size() == 0) {
                    throw new RuntimeException("类型表未配置，删除无效！");
                }


                //循环表查询是否存在数据
                for (Map<String, Object> tabMap : tableMap) {
                    existsSql = "select COUNT(id) c from " + tabMap.get("TABLENAME") + " where  years=? and distid=? and distname=? and  lxname=? ";

                    List<Map<String, Object>> renum = jdbcTemplatePrimary.queryForList(existsSql, map.get("YEARS"), map.get("DISTID"), map.get("DISTNAME"),
                            (null == map.get("LXNAME") || StringUtils.isEmpty(map.get("LXNAME").toString())) ? "" : map.get("LXNAME"));

                    countData += CollectionUtils.isEmpty(renum) ? 0 : (Long) renum.get(0).get("c");


                    if (countData > 0) {
                        break;
                    }
                }

                //不存在关联的数据 删除
                if (countData == 0) {
                    jdbcTemplatePrimary.update(deleteSql, map.get("YEARS"), map.get("DISTID"), map.get("DISTNAME"), map.get("LX"), map.get("LXNAME"));
                } else {
                    throw new RuntimeException("基表存在此单位数据，不可删除！");
                }


            } catch (Exception e) {
                throw e;
            }
        }


    }

    @Override
    public List<Map<String, Object>> listLxOrder(Integer years, String typeCode) {

        String sql = "select lx as name,lx as text from lxorder where years=? and typeCode=? and pulldownShow=?";
        return jdbcTemplatePrimary.queryForList(sql, new Object[]{years, typeCode, 1});
    }


    public boolean isNewspaper(Map<String, Object> map, String typeCode) {
        boolean flag = false;
        String sql = "select * from uploadBase where years=? and tableType=? and distno=? and sbflag =1";

        List list = jdbcTemplatePrimary.queryForList(sql, new Object[]{map.get("years"), typeCode, map.get("distid")});
        if (list == null || list.size() == 0) {
            flag = true;
        }
        return flag;

    }


    private List<Map<String, Object>> fileListTabs(Map map) {
        if (map == null || map.size() == 0) {
            return null;
        }
        StringBuffer sql = new StringBuffer();

        sql.append(" select DISTINCT fe.tablename  from fileItemLinkEx fe join fileList f on fe.years=f.years and fe.tableName=f.tableName and f.tableType='")
                .append(map.get("TABLETYPE"))
                .append("' and f.typeCode='")
                .append(map.get("TYPECODE"))
                .append("' and f.useFlag='是' where fe.years=")
                .append(map.get("YEARS"))
                .append(" and prjItem='")
                .append(map.get("LX"))
                .append("'")
        ;
        List<Map<String, Object>> listTabs = null;
        try {
            listTabs = jdbcTemplatePrimary.queryForList(sql.toString(), new Object[]{});
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return listTabs;
    }
}
