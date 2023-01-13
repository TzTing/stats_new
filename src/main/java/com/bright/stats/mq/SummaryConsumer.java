package com.bright.stats.mq;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bright.stats.constant.RocketConstant;
import com.bright.stats.pojo.dto.CheckDTO;
import com.bright.stats.pojo.dto.SummaryDTO;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.vo.CheckVO;
import com.bright.stats.pojo.vo.SummaryVO;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.service.BaseDataService;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author: Tz
 * @Date: 2022/10/14 15:09
 */
@Component
@RocketMQMessageListener(topic = RocketConstant.TOPIC_SUMMARY, consumerGroup = RocketConstant.TOPIC_SUMMARY + "_group")
public class SummaryConsumer implements RocketMQListener<MqMessage> {

    @Autowired
    private BaseDataService baseDataService;

    @Autowired
    private MqMessageRepository mqMessageRepository;

    @Override
    public void onMessage(MqMessage mqMessage) {

        Map<String, Object> data = (Map<String, Object>) mqMessage.getData();
        SummaryDTO summaryDTO = JSONObject.parseObject(JSON.toJSONString(data), SummaryDTO.class);

        SummaryVO summary = null;

        try{
            synchronized (this) {
                summary = baseDataService.summary(summaryDTO);
            }
            //汇总失败
            if(summary.getRvalue()){
                mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SUCCESS);
                mqMessage.setContent("汇总成功！");
            }else{
                mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_BUSINESS_FAIL);
                mqMessage.setContent(JSON.toJSONString(summary));
            }
        }catch (Exception e){
            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SYSTEM_FAIL);
            mqMessage.setContent(e.getMessage());
        }
        mqMessageRepository.save(mqMessage);
    }
}
