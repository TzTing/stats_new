package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.DataProcessNew;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/6/28 11:41
 * @Description
 */
public interface DataProcessNewManager {

    /**
     * 获取多个DataProcessNew配置
     * @param keyword 待办事项_上报/待办事项_退回上报
     * @param tableType 模式
     * @param priorEndDisId
     * @return 多个DataProcessNew配置
     */
    List<DataProcessNew> listDataProcessNews(String keyword, String tableType, Integer priorEndDisId);

    Map<Object, Object> parseProcess(List<DataProcessNew> dataProecss, String keyword, Map<Object, Object> params);

}
