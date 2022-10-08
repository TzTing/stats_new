package com.bright.common.pojo.query;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/23 17:22
 * @Description 排序参数类
 */
@Data
public class SortQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接收 sort=firstname,asc&sort=lastname,desc
     * 接收 ["firstname,asc","lastname,desc"]
     */
    private List<String> sorts;

}
