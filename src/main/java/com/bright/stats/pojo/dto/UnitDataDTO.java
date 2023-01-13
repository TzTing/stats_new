package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Tz
 * @Date: 2022/10/24 15:17
 */
@Data
public class UnitDataDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String tableName;
    private String typeCode;
    private String distNo;
    private String username;
    private String userDistNo;


    private String insertData;
    private String updateData;
    private String deleteData;

}
