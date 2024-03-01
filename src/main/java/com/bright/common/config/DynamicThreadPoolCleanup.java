package com.bright.common.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * <p> Project: stats - DynamicThreadPoolCleanup </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/02/23 16:48
 * @since 1.0.0
 */
@Component
public class DynamicThreadPoolCleanup implements ApplicationListener<ContextClosedEvent> {

    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    public DynamicThreadPoolCleanup(DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        dynamicThreadPoolManager.shutdownAllThreadPools();
    }
}
