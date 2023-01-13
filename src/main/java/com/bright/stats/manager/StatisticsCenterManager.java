package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.query.StatisticsCenterQuery;

import java.util.List;

/**
 * @author: Tz
 * @Date: 2022/09/29 0:26
 */
public interface StatisticsCenterManager {

    /**
     * 查询统计表的表信息
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    List<FileList> listStatisticsTables(String typeCode, Integer years, Integer months);

    /**
     * 分页获取统计数据
     * @param statisticsCenterQuery
     * @param isPage
     * @param pageResultClass
     * @return
     */
    <T> T listTableData(StatisticsCenterQuery statisticsCenterQuery, boolean isPage, Class<T> pageResultClass);
}
