package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.LxOrder;

/**
 * @Author txf
 * @Date 2022/8/3 10:43
 * @Description
 */
public interface LxOrderManager {

    /**
     * 获取LxOrder
     * @param years 年份
     * @param typeCode 模式
     * @param lx 类型
     * @return LxOrder
     */
    LxOrder getLxOrder(Integer years, String typeCode, String lx);
}
