package com.bright.common.config;

import com.sun.management.OperatingSystemMXBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Project: stats - BizThreadPoolExt </p>
 *
 * @author: Tz
 * @Date: 2023/12/25 11:50
 * @Description: 业务线程池扩展
 * @version: 1.0.0
 */
public class BizThreadPoolExt extends ThreadPoolExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(BizThreadPoolExt.class);

    /**
     * Java虚拟机的线程系统的管理接口
     */
    private ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /**
     * 运行Java虚拟机的操作系统的管理接口
     */
    private OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

    /**
     * 默认拒绝策略
     */
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

    /**
     * 线程池名称，一般以业务名称命名，方便区分
     */
    private String poolName;

    /**
     * 最短执行时间
     */
    private Long minCostTime;

    /**
     * 最长执行时间
     */
    private Long maxCostTime;
    /**
     * 总的耗时
     */
    private AtomicLong totalCostTime = new AtomicLong();

    private ThreadLocal<Long> startTimeThreadLocal = new ThreadLocal<>();

    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param poolName        线程池名称
     */
    public BizThreadPoolExt(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             TimeUnit unit, BlockingQueue<Runnable> workQueue, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), poolName);
    }


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param
     * @param poolName        线程池名称
     */
    public BizThreadPoolExt(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler, String poolName) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), handler, poolName);
    }


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param threadFactory   线程工厂
     * @param poolName        线程池名称
     */
    public BizThreadPoolExt(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             TimeUnit unit, BlockingQueue<Runnable> workQueue,
                             ThreadFactory threadFactory, String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
        this.poolName = poolName;
        this.maxCostTime = 0L;
        this.minCostTime = 0L;
    }


    /**
     * 调用父类的构造方法，并初始化HashMap和线程池名称
     *
     * @param corePoolSize    线程池核心线程数
     * @param maximumPoolSize 线程池最大线程数
     * @param keepAliveTime   线程的最大空闲时间
     * @param unit            空闲时间的单位
     * @param workQueue       保存被提交任务的队列
     * @param threadFactory   线程工厂
     * @param handler         拒绝策略
     * @param poolName        线程池名称
     */
    public BizThreadPoolExt(int corePoolSize, int maximumPoolSize, long keepAliveTime,
                             TimeUnit unit, BlockingQueue<Runnable> workQueue,
                             ThreadFactory threadFactory, RejectedExecutionHandler handler, String poolName) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
        this.poolName = poolName;
        this.maxCostTime = 0L;
        this.minCostTime = 0L;
    }

    /**
     * 根据线程id 获取线程执行占用总cpu的百分比
     * @param threadId 线程id
     * @return         占用总cpu的百分比
     */
    private double getThreadCpuUsage(long threadId) {
        long threadCpuTime = threadMXBean.getThreadCpuTime(threadId);
        long systemCpuTime = osBean.getProcessCpuTime();

        double cpuUsage = ((double) threadCpuTime / (double) systemCpuTime) * 100;
        cpuUsage = Math.round(cpuUsage * 100.0) / 100.0;
        return cpuUsage;
    }

    /**
     * 线程池延迟关闭时（等待线程池里的任务都执行完毕），统计线程池情况
     */
    @Override
    public void shutdown() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        LOGGER.info("{} 关闭线程池， 已执行任务: {}, 正在执行任务: {}, 未执行任务数量: {}",
                this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        super.shutdown();
    }

    /**
     * 线程池立即关闭时，统计线程池情况
     */
    @Override
    public List<Runnable> shutdownNow() {
        // 统计已执行任务、正在执行任务、未执行任务数量
        LOGGER.info("{} 立即关闭线程池，已执行任务: {}, 正在执行任务: {}, 未执行任务数量: {}",
                this.poolName, this.getCompletedTaskCount(), this.getActiveCount(), this.getQueue().size());
        return super.shutdownNow();
    }

    /**
     * 任务执行之前，记录任务开始时间
     */
    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        startTimeThreadLocal.set(System.currentTimeMillis());
    }

    /**
     * 任务执行之后，计算任务结束时间
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        long costTime = System.currentTimeMillis() - startTimeThreadLocal.get();
        String threadName = Thread.currentThread().getName();
        startTimeThreadLocal.remove();
        maxCostTime = maxCostTime > costTime ? maxCostTime : costTime;
        if (getCompletedTaskCount() == 0) {
            minCostTime = costTime;
        }
        minCostTime = minCostTime < costTime ? minCostTime : costTime;
        totalCostTime.addAndGet(costTime);
        LOGGER.info("{}-pool-monitor: " +
                        "任务名称: {}, 任务耗时: {} ms, 占用cpu时间: {} ms, 占总cpu: {} %, 初始线程数: {}, 核心线程数: {}, 执行的任务数量: {}, " +
                        "已完成任务数量: {}, 任务总数: {}, 队列里缓存的任务数量: {}, 池中存在的最大线程数: {}, " +
                        "最大允许的线程数: {},  线程空闲时间: {}, 线程池是否关闭: {}, 线程池是否终止: {}",
                this.poolName, threadName,
                costTime, threadMXBean.getThreadCpuTime(Thread.currentThread().getId()) / (1000 * 1000), getThreadCpuUsage(Thread.currentThread().getId()),
                this.getPoolSize(), this.getCorePoolSize(), this.getActiveCount(),
                this.getCompletedTaskCount(), this.getTaskCount(), this.getQueue().size(), this.getLargestPoolSize(),
                this.getMaximumPoolSize(), this.getKeepAliveTime(TimeUnit.MILLISECONDS), this.isShutdown(), this.isTerminated());
    }


    public Long getMinCostTime() {
        return minCostTime;
    }

    public Long getMaxCostTime() {
        return maxCostTime;
    }

    public void setMaxCostTime(Long maxCostTime) {
        this.maxCostTime = maxCostTime;
    }

    public void setMinCostTime(Long mixCostTime) {
        this.minCostTime = mixCostTime;
    }


    public long getAverageCostTime(){
        if(getCompletedTaskCount()==0||totalCostTime.get()==0){
            return 0;
        }
        return totalCostTime.get()/getCompletedTaskCount();
    }

    public String getPoolName() {
        return poolName;
    }

    /**
     * 生成线程池所用的线程，改写了线程池默认的线程工厂，传入线程池名称，便于问题追踪
     */
    static class MonitorThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        /**
         * 初始化线程工厂
         *
         * @param poolName 线程池名称
         */
        MonitorThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = Objects.nonNull(s) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = poolName + "-pool-" + poolNumber.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
