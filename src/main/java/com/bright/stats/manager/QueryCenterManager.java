package com.bright.stats.manager;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.query.QueryCenterQuery;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/3 17:57
 * @Description
 */
public interface QueryCenterManager {

    /**
     * 分页查询
     * @param queryCenterQuery
     * @return
     */
    PageResult<Map<String, Object>> listTableDataForPage(QueryCenterQuery queryCenterQuery);

    /**
     * 查询分析表列表
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    List<FileList> listAnalysisTables(String typeCode, Integer years, Integer months);
}
