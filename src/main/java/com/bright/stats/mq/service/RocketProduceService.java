package com.bright.stats.mq.service;

import com.bright.stats.pojo.po.primary.MqMessage;

/**
 * 消息生产者 Server
 * @author: Tz
 * @Date: 2022/10/14 16:19
 */
public interface RocketProduceService {

    /**
     * 发送消息到队列中
     * @param mqMessage 消息
     */
    void sendMessage(MqMessage mqMessage);

}
