package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/5 9:39
 * @Description
 */
@Data
public class SqlInfoVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sqlNo;
    private String groupName;
    private String modalName;
    private List<SqlInfoVO> children;
}
