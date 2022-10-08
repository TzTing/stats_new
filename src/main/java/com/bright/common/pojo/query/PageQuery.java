package com.bright.common.pojo.query;

import lombok.Data;

/**
 * @Author txf
 * @Date 2022/5/18 10:07
 * @Description 分页参数类
 */
@Data
public class PageQuery extends SortQuery {

    private static final long serialVersionUID = 1L;

    private Integer pageNumber;
    private Integer pageSize;

}
