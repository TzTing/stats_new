package com.bright.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * <p> Project: stats - DynamicThreadPoolCleanupManager </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/02/23 16:52
 * @since 1.0.0
 */
@Slf4j
@Component
public class DynamicThreadPoolManager {

    private final Map<String, BizThreadPoolExt> bizThreadPoolExtMap = new HashMap<>();

    public void registerThreadPool(String poolName, BizThreadPoolExt bizThreadPoolExt) {
        bizThreadPoolExtMap.put(poolName, bizThreadPoolExt);
    }

    public void shutdownAllThreadPools() {

        for (BizThreadPoolExt threadPool : bizThreadPoolExtMap.values()) {
            log.info("关闭线程池：[{}]", threadPool.getPoolName());
            threadPool.shutdown();
        }
    }
}
