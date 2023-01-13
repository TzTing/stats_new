package com.bright.stats.pojo.dto;

import com.bright.stats.pojo.po.primary.TableType;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/7 11:00
 * @Description
 */
@Data
public class MqMessagesDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String topicType;
    private Integer years;
    private Integer months;
    private String distNo;
    private String typeCode;
    private String username;

    private List<Integer> ids;

}
