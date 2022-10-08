package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/8/9 16:48
 * @Description
 */
@Data
public class ExportExcelNoTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private Integer grade;
    private Boolean isGrade;
    private String typeCode;
    private String sqlNo;
}
