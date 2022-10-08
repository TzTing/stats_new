package com.bright.stats.manager;

import java.util.Map;

/**
 * @Author txf
 * @Date 2022/7/25 15:27
 * @Description
 */
public interface BsConfigManager {

    /**
     * 查询系统参数
     * @return
     */
    Map<String, Object> querySysParam();
}
