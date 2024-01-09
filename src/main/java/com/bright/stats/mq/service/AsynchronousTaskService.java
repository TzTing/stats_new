package com.bright.stats.mq.service;

import com.bright.stats.pojo.po.primary.MqMessage;

/**
 * <p>Project: stats - AsynchronousTaskService </p>
 *
 * @author: Tz
 * @Date: 2023/12/22 15:38
 * @Description: 异步处理任务的service
 * @version: 1.0.0
 */
public interface AsynchronousTaskService {

    /**
     * 处理主题任务
     * @param topicName 主题名称
     * @param mqMessage 处理任务内容
     */
    void handTopicTask(String topicName, MqMessage mqMessage);
}
