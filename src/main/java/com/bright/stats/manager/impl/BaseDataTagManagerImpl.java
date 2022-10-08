package com.bright.stats.manager.impl;

import com.bright.stats.manager.BaseDataTagManager;
import com.bright.stats.manager.TemplatesParamManager;
import com.bright.stats.pojo.po.primary.TemplatesParam;
import com.bright.stats.util.Common;
import com.bright.stats.util.ListJsonUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 11:57
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BaseDataTagManagerImpl implements BaseDataTagManager {

    private final JdbcTemplate jdbcTemplatePrimary;
    private final TemplatesParamManager templatesParamManager;

    @Override
    public List<Map<String, Object>> queryDataBytemplate(String ecx, Map<Object, Object> tags, Map<Object, Object> params) {
        //Map<String,Object> rvalues=new HashMap<String, Object>();
        List<Map<String, Object>> rvalues = null;
        Map<String, Object> rmps = new HashMap<>();
        String expr = "";
        if (null == tags || tags.size() == 0) return rvalues;
        expr = (String) tags.get("expr");
        boolean isList = (Boolean) tags.get("islist");
        rvalues = queryDataByExpr(ecx, expr, tags, params);
        if (null == rvalues || rvalues.size() == 0) {
            rmps.put(expr, params.get(expr));
            rvalues = new ArrayList<>();
            rvalues.add(rmps);
        }
        return rvalues;

    }

    @Override
    public String queryStr(String expr){
        String columns="";
        if(null==expr || StringUtils.isEmpty(expr))return columns;
        columns=expr;
        if(expr.indexOf(".")==-1) return columns;
        if(expr.indexOf(".")>expr.indexOf(";")) return columns;
        columns=expr.substring(expr.indexOf(".")+1,expr.indexOf(";"));
        return columns;
    }

    private List<Map<String, Object>> queryDataByExpr(String exc, String expr, Map<Object, Object> tags, Map<Object, Object> params) {
        List<Map<String, Object>> rvalues = null;
        if (null == expr || StringUtils.isEmpty(expr)) return rvalues;
        if (expr.indexOf(".") == -1) return rvalues;
        if (expr.indexOf(".") > expr.indexOf(";")) return rvalues;
        String skey = expr.substring(0, expr.indexOf("."));
        String columns = expr.substring(expr.indexOf(".") + 1, expr.indexOf(";"));
        String condition = expr.substring(expr.indexOf("(") + 1, expr.indexOf(")"));
        condition = condition.replaceAll("：", ":");
        condition = condition.replaceAll("；", ";");
        System.out.println("b12" + condition);
        log.info("b12" + condition);
        Map<Object, Object> condsMap = ListJsonUtil.queryMap5Bystr(condition);
        condsMap.put(skey, columns);
        tags.put("skey", skey);
        tags.put(skey, columns);
        System.out.println("b13" + condsMap);
        log.info("b13" + condsMap);
        List<TemplatesParam> templates = templatesParamManager.listTemplatesParams("userDefinedExcel", skey);
        if (null == templates || templates.size() == 0) return rvalues;
        rvalues = new ArrayList<>();
        for (TemplatesParam info : templates) {
            if (null == info) continue;
            String sql = info.getSelSql();
            String order_sql = info.getOrderBySql();
            String keyword = info.getProcessKy();
            if (null != keyword && StringUtils.isNotEmpty(keyword.trim())) {
                Map<Object, Object> msgs = templatesParamManager.execDataProcess(keyword, params);
                boolean isError = (Boolean) msgs.get("isError");
                if (isError) {
                    log.info(" export by template error:" + msgs.get("alert"));
                    continue;
                }
            }
            StringBuffer sqlbf = new StringBuffer();
            sqlbf.append(sql).append(" ");
            if (null != order_sql && StringUtils.isNotEmpty(order_sql)) sqlbf.append(order_sql);
            sqlbf = Common.replaceParamToStrBuf(sqlbf, params);
            sqlbf = Common.replaceParamsbyStrBuf(sqlbf, condsMap);
            System.out.println("b14" + sqlbf);
            log.info("b14" + sqlbf);
            List<Map<String, Object>> datas = jdbcTemplatePrimary.queryForList(sqlbf.toString(), new ArrayList<Object>());
            if (null != datas && datas.size() > 0) rvalues.addAll(datas);
        }

        return rvalues;

    }


}
