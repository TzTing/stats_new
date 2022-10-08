package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/7/6 17:20
 * @Description
 */
@Data
public class ExportExcelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String userDistName;
    private String distname;
    private String dateStr;
    private String curyear;
    private String curmonth;
    private String curday;
    private Map<String, Object> data;

    private String excelTemplatePath;
    private String fileName;
}
