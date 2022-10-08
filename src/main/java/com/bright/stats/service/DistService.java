package com.bright.stats.service;

import com.bright.stats.pojo.vo.DistVO;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/15 11:23
 * @Description
 */
public interface DistService {

    /**
     * 获取地区树
     * @param years 年份
     * @param userDistNo 用户关联地区编号
     * @param distNo 地区编号
     * @return 地区树
     */
    List<DistVO> listDistTrees(Integer years, String userDistNo, String distNo);
}
