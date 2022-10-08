package com.bright.stats.manager.impl;

import com.bright.stats.manager.ExcelTemplateManager;
import com.bright.stats.pojo.po.primary.ExcelTemplate;
import com.bright.stats.repository.primary.ExcelTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 15:00
 * @Description
 */
@Component
@RequiredArgsConstructor
public class ExcelTemplateManagerImpl implements ExcelTemplateManager {

    private final ExcelTemplateRepository excelTemplateRepository;

    @Override
    public List<ExcelTemplate> listExcelTemplates(Integer years, String typeCode, String username, String tableType) {
        List<ExcelTemplate> excelTemplates = excelTemplateRepository.findExcelTemplate(years, typeCode, username, tableType);
        return excelTemplates;
    }

    @Override
    public ExcelTemplate getExcelTemplateById(Integer excelTemplateId) {
        return excelTemplateRepository.findById(excelTemplateId).get();
    }
}
