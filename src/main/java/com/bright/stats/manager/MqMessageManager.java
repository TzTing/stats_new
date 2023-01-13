package com.bright.stats.manager;

import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.MqMessage;

import java.util.List;

/**
 * @author: Tz
 * @Date: 2022/10/18 10:14
 */
public interface MqMessageManager {

    /**
     * 校验正在运行的稽核操作
     * @param mqMessagesDTO
     * @return
     */
    Boolean checkRunning(MqMessagesDTO mqMessagesDTO);


    List<MqMessage> findRunningMessage(MqMessagesDTO mqMessagesDTO);


    List<MqMessage> listAvailableMqMessagesByIds(MqMessagesDTO mqMessagesDTO);


    List<MqMessage> findTakeMessage(MqMessagesDTO mqMessagesDTO);


    List<MqMessage> listAvailableMqMessagesByParam(MqMessagesDTO mqMessagesDTO);

    /**
     * 检查所有正在运行的任务
     * @param mqMessagesDTO
     * @return
     */
    List<MqMessage> checkRunningAll(MqMessagesDTO mqMessagesDTO);
}
