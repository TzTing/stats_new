package com.bright.stats.pojo.dto;

import com.bright.common.pojo.query.Condition;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/28 16:32
 * @Description
 */
@Data
public class ExportExcelDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer excelTemplateId;
    private Integer years;
    private Integer months;
    private String distName;
    private String tableName;
    private String typeCode;
    private String lx;
    private String lxName;
    private Integer grade;
    private Boolean isBalanced;
    private Integer jxMode;
    private String distNo;
    private String userDistNo;

    private List<String> sorts;

    private List<Condition> conditions;
}
