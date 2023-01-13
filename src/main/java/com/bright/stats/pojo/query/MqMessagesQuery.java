package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.Condition;
import com.bright.common.pojo.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/12 9:25
 * @Description
 */
@Data
@ApiModel(description= "消息数据查询对象")
public class MqMessagesQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    /**
     * 主题类型
     */
    private String topicType;

    @ApiModelProperty(value = "年份", required = true)
    private Integer years;

    private Integer months;

    private String distNo;

    private String username;

    private String typeCode;

    private Integer consumerFlag;

    private Boolean readFlag;


}
