package com.bright.stats.pojo.query;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/8/3 10:23
 * @Description
 */
@Data
public class ExistDataQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String tableName;
    private String distNo;
    private String lxs;
    private String lxNames;
    private String typeCode;
}
