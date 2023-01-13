package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.Condition;
import com.bright.common.pojo.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/12 9:25
 * @Description
 */
@Data
@ApiModel(description= "基础数据查询对象")
public class BaseDataQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "年份", required = true)
    @NotNull(message = "年份不能为空")
    private Integer years;

    @ApiModelProperty(value = "月份", required = true)
    @NotNull(message = "月份不能为空")
    private Integer months;

    @ApiModelProperty(value = "表名", required = true)
    @NotBlank(message = "表名不能为空")
    private String tableName;


    private String lx;
    private String lxName;
    private Integer grade;
    private Boolean isGrade;
    private Boolean isBalanced;
    private String distNo;
    private String typeCode;
    private String userDistNo;


    @ApiModelProperty(value = "查询条件")
    private List<Condition> conditions;
}
