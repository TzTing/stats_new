package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.Dist;
import com.bright.stats.pojo.vo.DistAdapterVO;
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


    /**
     * 获取地区最大级别
     * @return
     */
    List<Integer> getDistAllGrade();

    /**
     * 根据年份和地区编号获取地区
     * @param years 年份
     * @param distNo 地区编号
     * @return
     */
    Dist getDistByYearAndDistNo(Integer years, String distNo);

    /**
     * 按年份和用户关联地区编号和地区编号获取地区集合
     * @param years 年份
     * @param userDistNo 用户关联地区编号
     * @param distNo 地区编号
     * @param distNoLength 地区编号长度
     * @return 地区集合
     */
    List<DistAdapterVO> listAdapterDistForList(Integer years, String userDistNo, String distNo, Integer distNoLength);

    /**
     * 获取当前地区的最大长度
     * @param distNo
     * @param years
     * @return
     */
    int getCurrMaxDistNoLength(String distNo, Integer years);


    /**
     * 获取当前地区的最大长度
     * @param distNo
     * @param years
     * @return
     */
    String getDistFullName(String distNo, Integer years);
}
