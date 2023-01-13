package com.bright.stats.pojo.dto;

import com.bright.stats.pojo.po.second.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/6/7 11:33
 * @Description
 */
@Data
public class ReportDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String dpName;
    private String keyword;

    private Integer years;
    private Integer months;

    private User user;

}
