package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.LxSummary;

import java.util.List;

/**
 * <p> Project: stats - LxSummaryManager </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/10/21 10:00
 * @since 1.0.0
 */
public interface LxSummaryManager {

    /**
     * 获取汇总合计的名称列表
     * @param typeCode  模式名
     * @return          汇总合计的名称列表
     */
    List<String> summaryNameList(String typeCode);

    /**
     * 获取需要添加额外汇总的配置列表
     * @param typeCode  模式名
     * @return          额外汇总的配置列表
     */
    List<LxSummary> findLxSummaryListByTypeCode(String typeCode);

}
