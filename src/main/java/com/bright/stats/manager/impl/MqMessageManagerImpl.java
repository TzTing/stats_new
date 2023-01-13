package com.bright.stats.manager.impl;

import com.bright.stats.manager.MqMessageManager;
import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.repository.primary.MqMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @author: Tz
 * @Date: 2022/10/18 10:14
 */
@Component
public class MqMessageManagerImpl implements MqMessageManager {

    @Autowired
    private MqMessageRepository mqMessageRepository;

    @Override
    public Boolean checkRunning(MqMessagesDTO mqMessagesDTO) {

        List<MqMessage> mqMessages = mqMessageRepository.findMqMessage(mqMessagesDTO.getTopicType()
                , mqMessagesDTO.getYears()
                , mqMessagesDTO.getMonths()
                , mqMessagesDTO.getDistNo()
                , mqMessagesDTO.getTypeCode()
                , mqMessagesDTO.getUsername());
        if(CollectionUtils.isEmpty(mqMessages)){
            return true;
        }

        //不区分用户名，区分地区， 根据地区来判断是否存在操作 即一个地区只能一个稽核操作在执行
        return false;
    }

    @Override
    public List<MqMessage> findRunningMessage(MqMessagesDTO mqMessagesDTO) {
        List<MqMessage> mqMessages = mqMessageRepository.findRunningMessage(mqMessagesDTO.getYears()
                , mqMessagesDTO.getMonths()
                , mqMessagesDTO.getDistNo()
                , mqMessagesDTO.getTypeCode()
                , mqMessagesDTO.getUsername());
        return mqMessages;
    }

    @Override
    public List<MqMessage> listAvailableMqMessagesByIds(MqMessagesDTO mqMessagesDTO) {
        return mqMessageRepository.findAvailableMqMessageByIds(mqMessagesDTO.getIds());
    }


    @Override
    public List<MqMessage> findTakeMessage(MqMessagesDTO mqMessagesDTO) {
        List<MqMessage> mqMessages = mqMessageRepository.findTakeMessage(mqMessagesDTO.getYears()
                , mqMessagesDTO.getMonths()
                , mqMessagesDTO.getDistNo()
                , mqMessagesDTO.getTypeCode()
                , mqMessagesDTO.getUsername());
        return mqMessages;
    }

    @Override
    public List<MqMessage> listAvailableMqMessagesByParam(MqMessagesDTO mqMessagesDTO) {
        return mqMessageRepository.findTakeMessage(mqMessagesDTO.getYears()
                , mqMessagesDTO.getMonths()
                , mqMessagesDTO.getDistNo()
                , mqMessagesDTO.getTypeCode()
                , mqMessagesDTO.getUsername());
    }

    @Override
    public List<MqMessage> checkRunningAll(MqMessagesDTO mqMessagesDTO) {
        List<MqMessage> mqMessages = mqMessageRepository.findMqMessage(mqMessagesDTO.getYears()
                , mqMessagesDTO.getMonths()
                , mqMessagesDTO.getTypeCode());
        return mqMessages;
    }


}
