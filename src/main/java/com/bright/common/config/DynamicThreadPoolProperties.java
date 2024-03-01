package com.bright.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * <p> Project: stats - DynamicThreadPoolProperties </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/01/19 11:04
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "dynamic.thread")
public class DynamicThreadPoolProperties {

    private Map<String, PoolConfig> pool;

    public Map<String, PoolConfig> getPool() {
        return pool;
    }

    public void setPool(Map<String, PoolConfig> pool) {
        this.pool = pool;
    }

    @Data
    public static class PoolConfig {
        /**
         * 线程池名称
         */
        private String poolName;
        /**
         * 动态注册的bean名称
         */
        private String beanName;
        /**
         * 核心线程池大小，即线程池中始终保持存活的线程数量
         */
        private int corePoolSize;
        /**
         * 最大线程池大小，即线程池中允许的最大线程数量
         */
        private int maximumPoolSize;
        /**
         * 空闲线程的存活时间，超过这个时间的空闲线程会被回收  默认单位为：second
         */
        private long keepAliveTime;
        /**
         * 用于保存等待执行的任务的阻塞队列 默认队列为 new ArrayBlockingQueue<>(blockQueueCapacity, true)
         */
        private int blockQueueCapacity;
        /**
         * 当线程池已满，且任务无法执行时的拒绝策略
         * <p>
         * <li>
         *     1: AbortPolicy 默认的处理策略，会直接抛出RejectedExecutionException异常，阻止系统继续接受新的任务
         * <li>
         *     2: CallerRunsPolicy 当任务被拒绝时，会在调用者的线程中执行该任务。
         * <li>
         *     3: DiscardPolicy 当任务被拒绝时，会默默地丢弃该任务。
         * <li>
         *     4: DiscardOldestPolicy 当任务被拒绝时，会丢弃最老的一个任务，然后尝试重新提交被拒绝的任务。
         */
        private int rejectedType;
    }

}
