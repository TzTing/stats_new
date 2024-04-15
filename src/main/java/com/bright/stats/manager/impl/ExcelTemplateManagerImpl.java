package com.bright.stats.manager.impl;

import com.bright.stats.manager.ExcelTemplateManager;
import com.bright.stats.pojo.po.primary.ExcelTemplate;
import com.bright.stats.repository.primary.ExcelTemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

        Set<ExcelTemplate> excelTemplateSet = new LinkedHashSet<>();
        for (String tempUserDistNo : userDistNo.split(",")) {
            excelTemplateSet.addAll(excelTemplates.stream().filter(excelTemplate -> {
                if (org.apache.commons.lang3.StringUtils.isBlank(excelTemplate.getBelongDistNo())) {
                    return true;
                } else if(tempUserDistNo.length() >= excelTemplate.getBelongDistNo().length() && tempUserDistNo.startsWith(excelTemplate.getBelongDistNo())) {
                    return true;
                } else if(tempUserDistNo.length() < excelTemplate.getBelongDistNo().length() && excelTemplate.getBelongDistNo().startsWith(tempUserDistNo)) {
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList()));
        }

//        List<ExcelTemplate> collects = excelTemplates.stream().filter(excelTemplate -> {
//            if (org.apache.commons.lang3.StringUtils.isBlank(excelTemplate.getBelongDistNo())) {
//                return true;
//            } else if(userDistNo.length() >= excelTemplate.getBelongDistNo().length() && userDistNo.startsWith(excelTemplate.getBelongDistNo())) {
//                return true;
//            } else if(userDistNo.length() < excelTemplate.getBelongDistNo().length() && excelTemplate.getBelongDistNo().startsWith(userDistNo)) {
//                return true;
//            } else {
//                return false;
//            }
//        }).collect(Collectors.toList());


        return new ArrayList<>(excelTemplateSet);
    }

    @Override
    public ExcelTemplate getExcelTemplateById(Integer excelTemplateId) {
        return excelTemplateRepository.findById(excelTemplateId).get();
    }
}
