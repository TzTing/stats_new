package com.bright.stats.pojo.dto;

import com.bright.stats.pojo.po.primary.TableType;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/6/7 11:00
 * @Description
 */
@Data
public class CheckDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private String tableName;
    private String typeCode;
    private Object[] ids;
    private Integer grade;
    private Boolean isAllDist;
    private Boolean isSb;
    private Boolean isGrade;

    private String userDistNo;

    private TableType tableType;
}
