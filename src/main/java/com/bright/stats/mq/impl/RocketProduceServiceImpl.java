package com.bright.stats.mq.impl;

import com.bright.stats.constant.RocketConstant;
import com.bright.stats.manager.DistManager;
import com.bright.stats.mq.RocketProduceService;
import com.bright.stats.pojo.po.primary.Dist;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.util.DateUtil;
import lombok.AllArgsConstructor;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author: Tz
 * @Date: 2022/10/14 16:19
 */
@Service
@AllArgsConstructor
public class RocketProduceServiceImpl implements RocketProduceService {


    private final RocketMQTemplate rocketMQTemplate;
    private final MqMessageRepository mqMessageRepository;
    private final DistManager distManager;


    @Override
    public void sendMessage(MqMessage mqMessage) {

        String distFullName = distManager.getDistFullName(mqMessage.getDistNo(), mqMessage.getYears());

        mqMessage.setDistName(distFullName);
        switch (mqMessage.getTopicType()){
            case RocketConstant.TOPIC_CHECK:
                mqMessage.setTopicName(RocketConstant.TOPIC_CHECK_NAME);
                break;
            case RocketConstant.TOPIC_SUMMARY:
                mqMessage.setTopicName(RocketConstant.TOPIC_SUMMARY_NAME);
                break;
            case RocketConstant.TOPIC_REPORT:
                mqMessage.setTopicName(RocketConstant.TOPIC_REPORT_NAME);
                break;
            case RocketConstant.TOPIC_WITHDRAW:
                mqMessage.setTopicName(RocketConstant.TOPIC_WITHDRAW_NAME);
                break;
            default:
        }

        //默认是未消费且可用的消息
        mqMessage.setOkFlag(false);
        mqMessage.setRunFlag(true);
        mqMessage.setCreatedBy(mqMessage.getUsername());
        mqMessage.setCreatedTime(DateUtil.getCurrDate());
        MqMessage result = mqMessageRepository.save(mqMessage);
        if(Objects.isNull(result)){
            throw new RuntimeException("消息保存异常！");
        }
        rocketMQTemplate.convertAndSend(RocketConstant.TOPIC_CONSUMER, result);
    }
}
