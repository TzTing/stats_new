package com.bright.stats.mq.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bright.stats.constant.RocketConstant;
import com.bright.stats.pojo.dto.CheckDTO;
import com.bright.stats.pojo.dto.ReportDTO;
import com.bright.stats.pojo.dto.SummaryDTO;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.vo.CheckVO;
import com.bright.stats.pojo.vo.InteractiveVO;
import com.bright.stats.pojo.vo.SummaryVO;
import com.bright.stats.repository.primary.MqMessageRepository;
import com.bright.stats.service.BaseDataService;
import com.bright.stats.util.DateUtil;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 队列消费者
 *
 * <p>RocketMQMessageListener(topic = RocketConstant.TOPIC_CONSUMER, consumerGroup = RocketConstant.TOPIC_CONSUMER + "_group"):
 * <p>监听的主题为：RocketConstant.TOPIC_CONSUMER
 * <p>监听的队列为：RocketConstant.TOPIC_CONSUMER + "_group"
 *
 * @author: Tz
 * @Date: 2022/10/17 10:53
 */
@Component
@RocketMQMessageListener(topic = RocketConstant.TOPIC_CONSUMER, consumerGroup = RocketConstant.TOPIC_CONSUMER + "_group")
public class RocketConsumerServiceImpl implements RocketMQListener<MqMessage> {

    @Autowired
    private BaseDataService baseDataService;

    @Autowired
    private MqMessageRepository mqMessageRepository;

    /**
     * 原子操作
     */
    private static ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();


    @Override
    public void onMessage(MqMessage mqMessage) {

        //消费稽核、汇总、上报 需要控制地区 当前地区只能有一个任务在执行, 比如我执行地区0103 则 010301地区会等待， 0104 则可以同时执行
        //1、校验任务是否取消
        //2、获取地区的锁
        //3、锁住当前地区进行业务处理
        //4、释放当前执行地区持有的锁


        Long startTime = System.currentTimeMillis();

        Map<String, Object> data = (Map<String, Object>) mqMessage.getData();
        mqMessage.setContentType(1);

        //锁对象
        Object object = null;


//        synchronized (this) {


        //1、校验任务是否取消
        //需要二次判断，处理锁
        synchronized (this){
            //查看这条消息是否是未消费且可用的
            MqMessage resMqMessage = mqMessageRepository.findById(mqMessage.getId()).get();

            //如果不可用 代表该消息在没被消费前被取消了
            if(!resMqMessage.getRunFlag()){
                resMqMessage.setContent("任务被取消！");
                resMqMessage.setConsumerFlag(-3);
                resMqMessage.setUpdatedBy(resMqMessage.getUsername());
                resMqMessage.setUpdatedTime(DateUtil.getCurrDate());
                mqMessageRepository.save(resMqMessage);
                return;
            }


            //2、获取地区的锁
            //如果根据key查询到了对象锁
            Enumeration enumeration = concurrentHashMap.keys();
            while(enumeration.hasMoreElements()){
                String key = (String) enumeration.nextElement();
                if(key.startsWith(mqMessage.getDistNo()) || mqMessage.getDistNo().startsWith(key)){
                    object = concurrentHashMap.get(key);
                }
            }

            if(object == null){
                object = new Object();
            }

            /*MqMessagesDTO mqMessagesDTO = new MqMessagesDTO();
            mqMessagesDTO.setTopicType(mqMessage.getTopicType());
            mqMessagesDTO.setYears(mqMessage.getYears());
            mqMessagesDTO.setMonths(mqMessage.getMonths());
            mqMessagesDTO.setDistNo(mqMessage.getDistNo());
            mqMessagesDTO.setTypeCode(mqMessage.getTypeCode());

            //查询所有正在执行的操作
            List<MqMessage> mqMessageList = mqMessageRepository.findMqMessage(mqMessage.getYears()
                    , mqMessage.getMonths()
                    , mqMessage.getTypeCode());

            //如果当前操作的地区上级或下级存在操作 则停止操作
            mqMessageList = mqMessageList.stream().filter(e -> {
                if(e.getDistNo().startsWith(mqMessage.getDistNo())
                        || mqMessage.getDistNo().startsWith(e.getDistNo())){
                    return true;
                } else {
                    return false;
                }
            }).collect(Collectors.toList());

            //这里大于1是因为当前消息处理的时候就是一条记录
            if(mqMessageList.size() > 1){
                //如果有相同地区的在执行
                concurrentHashMap.put(mqMessage.getDistNo(), object);
                resMqMessage.setContent("任务被强制取消，存在冲突的任务！");
                resMqMessage.setConsumerFlag(-3);
                resMqMessage.setRunFlag(false);
                resMqMessage.setUpdatedBy(resMqMessage.getUsername());
                resMqMessage.setUpdatedTime(DateUtil.getCurrDate());
                mqMessageRepository.save(resMqMessage);
            } else {

            }*/

        }



        //进入消费 表示该消息已经开始消费了

        //3、锁住当前地区进行业务处理
        //如果上一次执行的地区的锁没有释放, 现在有需要没释放的锁，就会进行等待
        synchronized(object) {
            try {
                startTime = System.currentTimeMillis();

                //查看这条消息是否是未消费且可用的
                MqMessage resMqMessage = mqMessageRepository.findById(mqMessage.getId()).get();
                //如果不可用 代表该消息在没被消费前被取消了
                if(!resMqMessage.getRunFlag()){
                    resMqMessage.setContent("任务被取消！");
                    resMqMessage.setConsumerFlag(-3);
                    resMqMessage.setUpdatedBy(resMqMessage.getUsername());
                    resMqMessage.setUpdatedTime(DateUtil.getCurrDate());
                    mqMessageRepository.save(resMqMessage);
                    return;
                }

                concurrentHashMap.put(mqMessage.getDistNo(), object);
                mqMessage.setOkFlag(true);
                MqMessage updResMessage = mqMessageRepository.save(mqMessage);

                if (Objects.isNull(updResMessage)) {
                    throw new RuntimeException("更新是否消费状态异常!");
                }

                if (mqMessage.getTopicType().equals(RocketConstant.TOPIC_CHECK)) {

                    CheckDTO checkDTO = JSONObject.parseObject(JSON.toJSONString(data), CheckDTO.class);

                    try {
                        List<CheckVO> check = baseDataService.check(checkDTO);
                        if (CollectionUtils.isEmpty(check)) {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SUCCESS);
                            mqMessage.setContent("稽核成功！");
                        } else {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_BUSINESS_FAIL);
                            mqMessage.setContent(JSON.toJSONString(check));
                            mqMessage.setContentType(3);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SYSTEM_FAIL);
                        mqMessage.setContent(e.getMessage());
                    }

                } else if (mqMessage.getTopicType().equals(RocketConstant.TOPIC_SUMMARY)) {

                    SummaryDTO summaryDTO = JSONObject.parseObject(JSON.toJSONString(data), SummaryDTO.class);
                    SummaryVO summary = null;
                    try {

                        summary = baseDataService.summaryCodingRun(summaryDTO);

                        //汇总失败
                        if (summary.getRvalue()) {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SUCCESS);
                            mqMessage.setContent("汇总成功！");
                        } else {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_BUSINESS_FAIL);
                            mqMessage.setContent(JSON.toJSONString(summary));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SYSTEM_FAIL);
                        mqMessage.setContent(e.getMessage());
                    }
                } else if (mqMessage.getTopicType().equals(RocketConstant.TOPIC_REPORT)
                        || mqMessage.getTopicType().equals(RocketConstant.TOPIC_WITHDRAW)) {

                    ReportDTO reportDTO = JSONObject.parseObject(JSON.toJSONString(data), ReportDTO.class);
                    try {

                        List<InteractiveVO> result = baseDataService.reportOrWithdraw(reportDTO.getDpName(), reportDTO.getKeyword(), reportDTO.getUser());

                        boolean successFlag = false;

                        for (InteractiveVO interactiveVO : result) {
                            if (interactiveVO.getWindowInfo().contains("您已完成当前的数据上报操作")) {
                                successFlag = true;
                            }
                            if (interactiveVO.getWindowInfo().contains("数据操作完成")) {
                                successFlag = true;
                            }
                            mqMessage.setContentType(interactiveVO.getWindowType());
                        }

                        //上报成功
                        if (successFlag) {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SUCCESS);
                            mqMessage.setContent(result.get(result.size() - 1).getWindowInfo());
                        } else {
                            mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_BUSINESS_FAIL);
                            mqMessage.setContent(result.get(result.size() - 1).getWindowInfo());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        mqMessage.setConsumerFlag(RocketConstant.CONSUMER_FLAG_SYSTEM_FAIL);
                        mqMessage.setContent(e.getMessage());
                    }

                } else {

                }
            } catch (Exception e){
                e.getStackTrace();
            } finally {
                //4、释放当前执行地区持有的锁
                concurrentHashMap.remove(mqMessage.getDistNo());
            }
        }
//        }

        Long endTime = System.currentTimeMillis();
        mqMessage.setExecTime(endTime - startTime);
        mqMessage.setUpdatedBy(mqMessage.getUsername());
        mqMessage.setUpdatedTime(DateUtil.getCurrDate());
        mqMessageRepository.save(mqMessage);

    }

}
