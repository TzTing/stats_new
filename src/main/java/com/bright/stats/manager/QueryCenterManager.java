package com.bright.stats.manager;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.query.QueryCenterQuery;

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

}
