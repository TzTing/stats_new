package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.DistEx;

/**
 * @Author txf
 * @Date 2022/8/3 11:14
 * @Description
 */
public interface DistExManager {
    DistEx getDistEx(Integer years, String tableName, String distNo, String lxName);
}
