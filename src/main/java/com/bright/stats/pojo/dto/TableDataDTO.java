package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/5 17:11
 * @Description
 */
@Data
public class TableDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String tableName;
    private String typeCode;

    private String insertData;
    private String updateData;
    private String deleteData;
}
