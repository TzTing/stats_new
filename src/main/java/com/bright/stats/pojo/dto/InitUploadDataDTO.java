package com.bright.stats.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Tz
 * @Date: 2022/10/10 19:26
 */
@Data
public class InitUploadDataDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;

}
