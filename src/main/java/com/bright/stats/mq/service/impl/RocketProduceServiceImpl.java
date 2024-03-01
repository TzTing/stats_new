package com.bright.stats.mq.service.impl;

import com.bright.stats.constant.RocketConstant;
import com.bright.stats.manager.DistManager;
import com.bright.stats.mq.service.AsynchronousTaskService;
import com.bright.stats.mq.service.RocketProduceService;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.util.DateUtil;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 消息生产者 Server 实现
 * @author: Tz
 * @Date: 2022/10/14 16:19
 */
@Service
@AllArgsConstructor
public class RocketProduceServiceImpl implements RocketProduceService {


//    private final RocketMQTemplate rocketMQTemplate;
    private final MqMessageRepository mqMessageRepository;
    private final DistManager distManager;
    private final AsynchronousTaskService asynchronousTaskService;


    @Override
    public void sendMessage(MqMessage mqMessage) {

        String distFullName = distManager.getDistFullName(mqMessage.getDistNo(), mqMessage.getYears());

        mqMessage.setDistName(distFullName);
        //根据不同类型设置不同的主题名称
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
//        rocketMQTemplate.convertAndSend(RocketConstant.TOPIC_CONSUMER, result);

        //异步处理 不使用消息队列 用异步任务处理
        asynchronousTaskService.handTopicTask(RocketConstant.TOPIC_CONSUMER, result);
    }
}
