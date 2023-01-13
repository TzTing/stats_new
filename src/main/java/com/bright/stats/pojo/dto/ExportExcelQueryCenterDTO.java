package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Tz
 * @Date: 2022/10/09 15:44
 */
@Data
public class ExportExcelQueryCenterDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private String typeCode;
    private Integer optType;
    private String userDistNo;
    private String tableName;
    private String lx;

}
