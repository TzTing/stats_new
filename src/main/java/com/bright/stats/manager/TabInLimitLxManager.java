package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.TabInLimitLx;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/3 9:58
 * @Description
 */
public interface TabInLimitLxManager {

    /**
     * 获取TabInLimitLx列表
     * @param years 年份
     * @param tableName 表名
     * @param prjType
     * @return
     */
    List<TabInLimitLx> listTabInLimitLxs(Integer years, String tableName, String prjType);
}
