package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author: Tz
 * @Date: 2023/01/03 15:59
 */
@Data
public class CreateEmptyTableDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer beginMonths;
    private Integer endMonths;
    private String distNo;
    private String typeCode;
    private String tableName;
    private Integer optType;


}
