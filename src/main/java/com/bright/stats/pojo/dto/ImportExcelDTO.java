package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/7 10:50
 * @Description
 */
@Data
public class ImportExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer excelTemplateId;
    private Integer years;
    private Integer months;
    private String distNo;
    private String distName;
    private String tableName;
    private String filePath;
}
