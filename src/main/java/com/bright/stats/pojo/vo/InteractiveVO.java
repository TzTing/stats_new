package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Tz
 * @Date: 2022/09/29 16:37
 */
@Data
public class InteractiveVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 1: 普通提示信息
     * 2: 跳转
     */
    private Integer windowType;

    private String windowInfo;

    private Boolean sbFlag;
}
