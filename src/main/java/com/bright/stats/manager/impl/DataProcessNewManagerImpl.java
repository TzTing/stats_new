package com.bright.stats.manager.impl;

import com.bright.stats.manager.DataProcessNewManager;
import com.bright.stats.pojo.po.primary.DataProcessNew;
import com.bright.stats.repository.primary.DataProcessNewRepository;
import com.bright.stats.util.Common;
import com.bright.stats.util.DataConstants;
import com.bright.stats.util.ReplaceSqlUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/6/28 11:41
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataProcessNewManagerImpl implements DataProcessNewManager {

    private final DataProcessNewRepository dataProcessNewRepository;
    private final JdbcTemplate jdbcTemplatePrimary;

    @Override
//    @Cacheable(value = "DataProcessNew", key = "#root.methodName + #keyword + #tableType")
    public List<DataProcessNew> listDataProcessNews(String keyword, String tableType, Integer priorEndDisId) {
        List<DataProcessNew> dataProcessNews = dataProcessNewRepository.findDataProcessNew(keyword, tableType, priorEndDisId);
        return dataProcessNews;
    }

    @Override
    public Map<Object, Object> parseProcess(List<DataProcessNew> processs, String keyword, Map<Object, Object> params) {
        Map<Object, Object> maps = new HashMap<Object, Object>();
        if (null == processs || processs.size() == 0)
            return maps;

        StringBuffer sqlSb = new StringBuffer();
        StringBuffer alertSb = new StringBuffer();
        StringBuffer alertSubSb = new StringBuffer();
        StringBuffer errorInfo = new StringBuffer();
        List<Map<String, Object>> tmpLists = null;
        List<Map<String, Object>> tmpData = null;
        Boolean isError = false;
        boolean isShow = false;
        maps.put("isError", isError);
        String sql = "";
        int pid = 0;
        try {
            for (DataProcessNew process : processs) {
                pid = process.getId();
                sqlSb.setLength(0);
                alertSb.setLength(0);
                alertSubSb.setLength(0);
                sqlSb.append(process.getProcessSql());
                alertSb.append(process.getAlert());
                alertSubSb.append(process.getAlertSub());


                sql = ReplaceSqlUtil.getSql(sqlSb.toString(), keyword);
                String alert = ReplaceSqlUtil.getSql(alertSb.toString(), keyword);
                String alertSub = ReplaceSqlUtil.getSql(alertSubSb.toString(), keyword);

                sql = (null == sql || StringUtils.isEmpty(sql)) ? "" : Common.replaceByMap(sql, DataConstants.sysparams).toString();
                alert = (null == alert || StringUtils.isEmpty(alert)) ? "" : Common.replaceByMap(alert, DataConstants.sysparams).toString();
                alertSub = (null == alertSub || StringUtils.isEmpty(alertSub)) ? "" : Common.replaceByMap(alertSub, DataConstants.sysparams).toString();

                sql = (null == sql || StringUtils.isEmpty(sql)) ? "" : Common.replaceByMap(sql, params).toString();
                alert = (null == alert || StringUtils.isEmpty(alert)) ? "" : Common.replaceByMap(alert, params).toString();
                alertSub = (null == alertSub || StringUtils.isEmpty(alertSub)) ? "" : Common.replaceByMap(alertSub, params).toString();

                if (!process.getIsAlert()) {    //非提示信息
                    jdbcTemplatePrimary.update(sql);
                } else if (process.getAlertType() != 99) {
                    tmpData = null;
                    tmpLists = jdbcTemplatePrimary.queryForList(sql);
                    if (null != tmpLists && tmpLists.size() > 0) {
                        for (Map<String, Object> map : tmpLists) {
                            alert = Common.replaceByMap(alert, map).toString();
                            if (null != alertSub && StringUtils.isNotEmpty(alertSub)) {
                                alertSub = Common.replaceByMap(alertSub, map).toString();
                            }
                        }
                    }
                }

                if (process.getIsAlert()) {
                    switch (process.getAlertType()) {
                        case 1:
                        case 2:
                        case 3:
                        case 98:
                        case 101:
                        case 102:
                        case 103:
                            tmpData = jdbcTemplatePrimary.queryForList(alert);
                            if (tmpData.size() > 0) {
                                alert = (String) tmpData.get(0).values().toArray()[0];
                            }
                            tmpData = null;
                            break;
                        default:
                            break;
                    }
                }

                if ((tmpData != null && tmpData.size() != 0) || (tmpLists != null && tmpLists.size() > 0)) {
                    isShow = true;
                }

                if (isShow) {
                    maps.put("isError", isShow);
                    maps.put("alert", alert);
                    break;
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            isError = true;
            errorInfo.append("访问服务器失败");
            log.error("dataProcess error:" + e.getMessage());
            maps.put("isError", isError);
            maps.put("alert", errorInfo);

        }

        return maps;

    }

}
