package com.bright.stats.manager.impl;

import com.bright.stats.manager.ExcelTemplateManager;
import com.bright.stats.pojo.po.primary.ExcelTemplate;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.repository.primary.ExcelTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<ExcelTemplate> listExcelTemplates(Integer years, String typeCode, String username, String tableType, String userDistNo) {
        List<ExcelTemplate> excelTemplates = excelTemplateRepository.findExcelTemplate(years, typeCode, username, tableType);
        List<ExcelTemplate> collects = excelTemplates.stream().filter(excelTemplate -> {
            if (org.apache.commons.lang3.StringUtils.isBlank(excelTemplate.getBelongDistNo()) ||
                    (excelTemplate.getBelongDistNo().startsWith(userDistNo))) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());
        return collects;
    }

    @Override
    public ExcelTemplate getExcelTemplateById(Integer excelTemplateId) {
        return excelTemplateRepository.findById(excelTemplateId).get();
    }
}
