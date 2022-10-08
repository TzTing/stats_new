package com.bright.common.result;

import lombok.Data;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/3/2 14:42
 * @Description 分页结果类
 */
@Data
public class PageResult<T> {

    /**
     * 总数
     */
    private Long total;

    /**
     * 数据
     */
    private List<T> data;


    public static <T> PageResult<T> of(Long total, List<T> data) {
        PageResult<T> pageResult = new PageResult<>();
        pageResult.setTotal(total);
        pageResult.setData(data);
        return pageResult;
    }

}
