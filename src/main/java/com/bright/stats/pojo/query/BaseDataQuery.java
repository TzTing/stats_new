package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.Condition;
import com.bright.common.pojo.query.PageQuery;
import lombok.Data;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/12 9:25
 * @Description
 */
@Data
public class BaseDataQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String tableName;
    private String lx;
    private String lxName;
    private Integer grade;
    private Boolean isGrade;
    private Boolean isBalanced;
    private String distNo;
    private String typeCode;
    private String userDistNo;


    private List<Condition> conditions;
}
