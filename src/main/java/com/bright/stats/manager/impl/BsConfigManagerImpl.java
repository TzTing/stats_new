package com.bright.stats.manager.impl;

import com.bright.stats.manager.BsConfigManager;
import com.bright.stats.pojo.po.primary.BsConfig;
import com.bright.stats.repository.primary.BsConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/7/25 15:27
 * @Description
 */
@Component
@RequiredArgsConstructor
public class BsConfigManagerImpl implements BsConfigManager {

    private final BsConfigRepository bsConfigRepository;

    @Override
    @Cacheable(value = "BsConfig", key = "#root.methodName")
    public Map<String, Object> querySysParam() {
        List<BsConfig> bsConfigs = bsConfigRepository.findBsConfig();
        Map<String, Object> map = new HashMap<>(16);
        for (BsConfig bsConfig : bsConfigs) {
            map.put(bsConfig.getValueName(), bsConfig.getExpress());
        }
        return map;
    }
}
