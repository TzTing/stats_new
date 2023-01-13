/*
package com.bright.stats.mq;

import com.bright.stats.constant.RocketConstant;
import com.bright.stats.service.BaseDataService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

*/
/**
 * @author: Tz
 * @Date: 2022/10/14 15:09
 *//*

@Component
@RocketMQMessageListener(topic = RocketConstant.TOPIC_REPORT, consumerGroup = RocketConstant.TOPIC_REPORT + "_group")
public class ReportConsumer implements RocketMQListener<String> {

    @Autowired
    private BaseDataService baseDataService;

    @Override
    public void onMessage(String s) {
        System.out.println(s);
    }
}
*/
