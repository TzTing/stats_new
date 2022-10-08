package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author txf
 * @Date 2022/7/22 14:42
 * @Description
 */
@Data
public class ExcelTemplateVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private Integer years;
    private String shortDis;
    private String type;
    private String excelType;
    private String fileName;
    private Integer jxMode;
    private String writer;
    private Date writeDate;
}
