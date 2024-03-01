package com.bright.common.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

/**
 * <p> Project: stats - DynamicThreadPoolChangePublisher </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/01/19 12:45
 * @since 1.0.0
 */
@Service
public class DynamicThreadPoolChangePublisher {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    /**
     * PostConstruct: 初始化执行发布
     */
    @PostConstruct
    public void init() {
        applicationEventPublisher.publishEvent(new DynamicThreadPoolChangeEvent(this));
    }
}
