package com.bright.stats.pojo.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @Author txf
 * @Date 2022/7/5 17:11
 * @Description
 */
@Data
public class TableDataDTO implements Serializable {

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
    private String typeCode;
    private String distNo;
    private String username;


    private String insertData;
    private String updateData;
    private String deleteData;
}
