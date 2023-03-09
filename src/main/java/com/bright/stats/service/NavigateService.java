package com.bright.stats.service;

import com.bright.stats.pojo.dto.CreateEmptyTableDTO;
import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.po.primary.Note;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.query.MqMessagesQuery;
import com.bright.stats.pojo.vo.InteractiveVO;
import com.bright.stats.pojo.vo.InteractiveVOEx;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 16:20
 * @Description
 */
public interface NavigateService {

    /**
     * 获取多个TableType
     * @return 多个TableType
     */
    List<TableType> listTableTypes();

    /**
     * 选择模式
     * @param tableTypeId TableTypeId
     */
    void selectMode(Integer tableTypeId);

    /**
     * 查询队列消息记录
     * @param mqMessagesQuery
     * @return
     */
    List<MqMessage> listMqMessages(MqMessagesQuery mqMessagesQuery);

    /**
     * 批量阅读
     * @param mqMessagesQuery
     * @return
     */
    Boolean readMqMessages(MqMessagesDTO mqMessagesQuery);

    /**
     * 撤销消息
     * @param ids
     * @return
     */
    List<MqMessage> revokeMessage(MqMessagesDTO ids);

    /**
     * 校验任务运行的情况
     * @param mqMessagesDTO
     * @return
     */
    List<Integer> validMessageTask(MqMessagesDTO mqMessagesDTO);

    /**
     * 根据条件生成空配置数据
     * @param createEmptyTableDTO
     * @return
     */
    Boolean createEmptyTable(CreateEmptyTableDTO createEmptyTableDTO);


    List<InteractiveVOEx> listNotes(String keyword, String tableType);
}
