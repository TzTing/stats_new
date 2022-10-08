package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.ExcelTemplate;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 15:00
 * @Description
 */
public interface ExcelTemplateManager {
    /**
     * 获取多个ExcelTemplate
     * @param years 年份
     * @param typeCode 模式/表名称
     * @param username 用户名称
     * @param tableType 基础表/分析表
     * @return 多个ExcelTemplate
     */
    List<ExcelTemplate> listExcelTemplates(Integer years, String typeCode, String username, String tableType);

    /**
     * 按id获取ExcelTemplate
     * @param excelTemplateId id
     * @return 单个ExcelTemplate
     */
    ExcelTemplate getExcelTemplateById(Integer excelTemplateId);
}
