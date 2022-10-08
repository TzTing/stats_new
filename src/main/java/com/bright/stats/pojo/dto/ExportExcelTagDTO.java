package com.bright.stats.pojo.dto;

import com.bright.common.pojo.query.Condition;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/1 11:26
 * @Description
 */
@Data
public class ExportExcelTagDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer excelTemplateId;
    private Integer years;
    private Integer months;
    private String distNo;
    private String distName;
    private Integer tabId;
    private String lx;
    private String lxName;
    private Integer grade;
    private Boolean isWanYuan;
    private String paramJson;

    private List<String> sorts;

    private List<Condition> conditions;
}
