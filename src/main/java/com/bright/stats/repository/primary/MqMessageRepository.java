package com.bright.stats.repository.primary;

import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.BsConfig;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.query.MqMessagesQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

/**
 * @Author txf
 * @Date 2022-07-25 15:26:03
 * @Description
 */
public interface MqMessageRepository extends JpaRepository<MqMessage, Integer>, JpaSpecificationExecutor<MqMessage> {

    @Query("from MqMessage where username=:username and consumerFlag in (1, -1) order by createdTime")
    List<MqMessage> findMqMessage(@Param("username") String username);


    @Query("from MqMessage where topicType=:topicType and years=:years and months=:months and distNo=:distNo and typeCode=:typeCode and username=:username and runFlag=true and consumerFlag=0")
    List<MqMessage> findMqMessage(@Param("topicType") String topicType, @Param("years") Integer years, @Param("months") Integer months, @Param("distNo") String distNo, @Param("typeCode") String typeCode, @Param("username") String username);

    @Query("from MqMessage where topicType=:topicType and years=:years and months=:months and typeCode=:typeCode and runFlag=true and consumerFlag=0")
    List<MqMessage> findMqMessage(@Param("topicType") String topicType, @Param("years") Integer years, @Param("months") Integer months, @Param("typeCode") String typeCode);

    @Query("from MqMessage where years=:years and months=:months and typeCode=:typeCode and runFlag=true and consumerFlag=0")
    List<MqMessage> findMqMessage(@Param("years") Integer years, @Param("months") Integer months, @Param("typeCode") String typeCode);


    @Modifying
    @Transactional(rollbackFor = Throwable.class)
    @Query("update MqMessage set readFlag = true where id in (:ids) and username = :username")
    int readMqMessages(@Param("ids") List<Integer> ids, @Param("username") String username);

    @Modifying
    @Transactional(rollbackFor = Throwable.class)
    @Query("update MqMessage set runFlag = false where id in (:ids)")
    Integer revokeMessageByIds(@Param("ids") List<Integer> ids);

    @Query("from MqMessage where runFlag = true and okFlag = false and id in (:ids)")
    List<MqMessage> findAvailableMqMessageByIds(@Param("ids") List<Integer> ids);


    @Query("from MqMessage where years=:years and months=:months and distNo=:distNo and typeCode=:typeCode and username=:username and runFlag=true and okFlag=true and consumerFlag = 0")
    List<MqMessage> findRunningMessage(@Param("years") Integer years
            , @Param("months") Integer months
            , @Param("distNo") String distNo
            , @Param("typeCode") String typeCode
            , @Param("username") String username);


    @Query("from MqMessage where years=:years and months=:months and distNo=:distNo and typeCode=:typeCode and username=:username and runFlag=true and okFlag=false and consumerFlag = 0")
    List<MqMessage> findTakeMessage(@Param("years") Integer years
            , @Param("months") Integer months
            , @Param("distNo") String distNo
            , @Param("typeCode") String typeCode
            , @Param("username") String username);

    @Modifying
    @Transactional(rollbackFor = Throwable.class)
    @Query("update MqMessage set runFlag = false, consumerFlag = -3 where years=:years and months=:months and distNo=:distNo and typeCode=:typeCode and username=:username and runFlag=true and okFlag=false")
    Integer revokeMessageByParam(@Param("years") Integer years
            , @Param("months") Integer months
            , @Param("distNo") String distNo
            , @Param("typeCode") String typeCode
            , @Param("username") String username);


    @Query(value = "select * from Mq", nativeQuery = true)
    void test();
}