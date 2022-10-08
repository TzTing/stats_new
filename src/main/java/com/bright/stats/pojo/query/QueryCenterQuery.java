package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.Condition;
import com.bright.common.pojo.query.PageQuery;
import lombok.Data;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/3 17:32
 * @Description
 */
@Data
public class QueryCenterQuery extends PageQuery {

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
    private Integer optType;
    private Boolean isExcel;
    private String userDistNo;

    private List<Condition> conditions;
}
