package com.bright.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * <p>Project: stats - BizJobConfiguration </p>
 *
 * @author: Tz
 * @Date: 2023/12/25 10:37
 * @Description: 业务任务的线程池
 * @version: 1.0.0
 */
@Configuration(proxyBeanMethods = false)
public class BizJobConfiguration {

    public static final String BIZ_THREAD_POOL_TASK_EXECUTOR = "BIZ_THREAD_POOL_TASK_EXECUTOR";

    @Bean(BIZ_THREAD_POOL_TASK_EXECUTOR)
    public ThreadPoolExecutor bizThreadPoolTaskExecutor() {

        BizThreadPoolExt executor = new BizThreadPoolExt(32
                , 64
                , 60
                , TimeUnit.SECONDS
                , new ArrayBlockingQueue<>(100, true)
                , new ThreadPoolExecutor.CallerRunsPolicy(), "biz-task");


        return executor;
    }
}
