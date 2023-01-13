package com.bright.stats.mq;

import com.bright.stats.pojo.po.primary.MqMessage;

/**
 * @author: Tz
 * @Date: 2022/10/14 16:19
 */
public interface RocketProduceService {

    /**
     *
     * @param mqMessage
     */
    void sendMessage(MqMessage mqMessage);

}
