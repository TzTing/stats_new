package com.bright.common.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author txf
 * @Date 2022/7/14 10:45
 * @Description
 */
@Getter
@Setter
@Component
public class StatsProperties {

    @Value("${stats.asset-database}")
    private String statsAssetDatabase;
}
