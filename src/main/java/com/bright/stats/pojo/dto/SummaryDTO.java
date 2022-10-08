package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/6/7 11:33
 * @Description
 */
@Data
public class SummaryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private String tableName;
    private String typeCode;
}
