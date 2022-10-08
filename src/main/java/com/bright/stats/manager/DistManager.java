package com.bright.stats.manager;

import com.bright.stats.pojo.vo.DistVO;

import java.util.List;
import java.util.Set;

/**
 * @Author txf
 * @Date 2022/7/27 16:50
 * @Description
 */
public interface DistManager {

    /**
     * 按年份获取地区等级集合
     * @param years 年份
     * @return 地区等级集合
     */
    Set<Integer> listDistGrades(Integer years);

    /**
     * 获取最大地区编号长度
     * @param distNo 地区编号
     * @param years 年份
     * @param grade 级别
     * @return 最大地区编号长度
     */
    int getMaxDistNoLength(String distNo, Integer years, Integer grade);

    /**
     * 按年份和用户关联地区编号和地区编号获取地区集合
     * @param years 年份
     * @param userDistNo 用户关联地区编号
     * @param distNo 地区编号
     * @param distNoLength 地区编号长度
     * @return 地区集合
     */
    List<DistVO> listDistForList(Integer years, String userDistNo, String distNo, Integer distNoLength);
}
