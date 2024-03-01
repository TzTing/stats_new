package com.bright.common.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * <p> Project: stats - DynamicThreadPoolChangeListener </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/01/19 10:59
 * @since 1.0.0
 */
@Component
public class DynamicThreadPoolChangeListener implements ApplicationListener<DynamicThreadPoolChangeEvent> {


    private final DynamicThreadPoolProperties dynamicThreadPoolProperties;
    private final ApplicationContext context;
    private final DynamicThreadPoolManager dynamicThreadPoolManager;

    public DynamicThreadPoolChangeListener(ApplicationContext context
            , DynamicThreadPoolProperties dynamicThreadPoolProperties
            , DynamicThreadPoolManager dynamicThreadPoolManager) {
        this.context = context;
        this.dynamicThreadPoolProperties = dynamicThreadPoolProperties;
        this.dynamicThreadPoolManager = dynamicThreadPoolManager;
    }

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(DynamicThreadPoolChangeEvent event) {

        // 获取BeanDefinitionRegistry
        BeanDefinitionRegistry beanDefinitionRegistry = (BeanDefinitionRegistry) context.getAutowireCapableBeanFactory();

        // 获取配置文件中的线程池配置
        Map<String, DynamicThreadPoolProperties.PoolConfig> poolConfigMap = dynamicThreadPoolProperties.getPool();
        for (Map.Entry<String, DynamicThreadPoolProperties.PoolConfig> entry : poolConfigMap.entrySet()) {
            DynamicThreadPoolProperties.PoolConfig poolConfig = entry.getValue();

            //获取相关属性
            String poolName = poolConfig.getPoolName();
            int corePoolSize = poolConfig.getCorePoolSize();
            String beanName = poolConfig.getBeanName();
            long keepAliveTime = poolConfig.getKeepAliveTime();
            int blockQueueCapacity = poolConfig.getBlockQueueCapacity();
            int maximumPoolSize = poolConfig.getMaximumPoolSize();
            int rejectedType = poolConfig.getRejectedType();

            //需要注册的线程池
            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(BizThreadPoolExt.class);

            //构建构造器的参数
            ConstructorArgumentValues constructorArgs = new ConstructorArgumentValues();
            constructorArgs.addIndexedArgumentValue(0, corePoolSize);
            constructorArgs.addIndexedArgumentValue(1, maximumPoolSize);
            constructorArgs.addIndexedArgumentValue(2, keepAliveTime);
            constructorArgs.addIndexedArgumentValue(3, TimeUnit.SECONDS);
            constructorArgs.addIndexedArgumentValue(4, new ArrayBlockingQueue<>(blockQueueCapacity, true));

            //处理设置的拒绝策略
            switch (rejectedType) {
                case 2:
                    constructorArgs.addIndexedArgumentValue(5, new ThreadPoolExecutor.CallerRunsPolicy());
                    break;
                case 3:
                    constructorArgs.addIndexedArgumentValue(5, new ThreadPoolExecutor.DiscardPolicy());
                    break;
                case 4:
                    constructorArgs.addIndexedArgumentValue(5, new ThreadPoolExecutor.DiscardOldestPolicy());
                    break;
                default:
                    constructorArgs.addIndexedArgumentValue(5, new ThreadPoolExecutor.AbortPolicy());
                    break;
            }

            constructorArgs.addIndexedArgumentValue(6, poolName);

            //设置bean
            builder.getRawBeanDefinition().setConstructorArgumentValues(constructorArgs);

            //注册bean
            BeanDefinition beanDefinition = builder.getRawBeanDefinition();
            beanDefinitionRegistry.registerBeanDefinition(beanName, beanDefinition);

            // 获取线程池实例
            BizThreadPoolExt bizThreadPoolExt = (BizThreadPoolExt) context.getBean(beanName);
            dynamicThreadPoolManager.registerThreadPool(poolName
                    , bizThreadPoolExt);
        }

    }

}
