package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/12 15:33
 * @Description
 */
@Data
public class RuleInnerVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String express;
    private String detail;
    private String opt;
}
