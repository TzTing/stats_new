package com.bright.common.config;

import org.springframework.context.ApplicationEvent;

/**
 * <p> Project: stats - DynamicThreadPoolChangeEvent </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/01/19 10:58
 * @since 1.0.0
 */
public class DynamicThreadPoolChangeEvent extends ApplicationEvent {


    public DynamicThreadPoolChangeEvent(Object source) {
        super(source);
    }

}
