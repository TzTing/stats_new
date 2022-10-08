package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/15 11:25
 * @Description
 */
@Data
public class DistVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String distNo;
    private String distName;
    private String parentDistNo;
    private String distType;
    private Integer childrenExistFlag;
}
