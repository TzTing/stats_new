package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/6/7 11:34
 * @Description
 */
@Data
public class SummaryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Boolean rvalue;
    private Integer counts;
    private String shortTableName;
}
