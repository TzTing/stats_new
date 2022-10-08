package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.TemplatesParam;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 14:18
 * @Description
 */
public interface TemplatesParamManager {

    /**
     * 获取模板参数集合
     * @param templateName
     * @param skey
     * @return
     */
    List<TemplatesParam> listTemplatesParams(String templateName, String skey);

    Map<Object, Object> execDataProcess(String keyword, Map<Object, Object> params);
}
