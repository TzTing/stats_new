package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.Navigate;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 17:29
 * @Description
 */
public interface NavigateManager {

    /**
     * 获取导航菜单集合
     * @return 导航菜单集合
     */
    List<Navigate> listNavigates();
}
