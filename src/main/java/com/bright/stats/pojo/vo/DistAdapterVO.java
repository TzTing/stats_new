package com.bright.stats.pojo.vo;

import lombok.Data;

import java.util.Date;

/**
 * @author: Tz
 * @Date: 2022/10/25 15:35
 */
@Data
public class DistAdapterVO extends DistVO{

    private static final long serialVersionUID = 1L;

    private String importDistNo;

    private String importName;

    private Integer years;

    private Date sbTime;
}
