package com.bright.stats.service;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.query.StatisticsCenterQuery;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/4 11:18
 * @Description
 */
public interface StatisticsCenterService {

    /**
     * 获取表头列表
     * @param typeCode 模式
     * @param tableName 表名称
     * @param years 年份
     * @param months 月份
     * @return 表头
     */
    List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months);

    /**
     * 分页获取表数据
     * @param statisticsCenterQuery
     * @return 表数据
     */
    PageResult<Map<String, Object>> listTableDataForPage(StatisticsCenterQuery statisticsCenterQuery);
}
