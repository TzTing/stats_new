package com.bright.common.pojo.query;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/5 10:34
 * @Description
 */
@Data
public class Condition implements Serializable {

    private static final long serialVersionUID = 1L;

    private String fieldName;
    private String express;
    private String fieldValue;
    private String fieldType;
}
