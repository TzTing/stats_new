package com.bright.stats.manager;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 11:57
 * @Description
 */
public interface BaseDataTagManager {


    List<Map<String, Object>> queryDataBytemplate(String ecx, Map<Object, Object> tags, Map<Object, Object> params);

    String queryStr(String expr);
}
