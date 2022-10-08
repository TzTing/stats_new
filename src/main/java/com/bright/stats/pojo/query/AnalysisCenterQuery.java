package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.Condition;
import com.bright.common.pojo.query.PageQuery;
import lombok.Data;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/5 14:47
 * @Description
 */
@Data
public class AnalysisCenterQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private Integer grade;
    private Boolean isGrade;
    private String sqlNo;
    private String unitDataType;
    private String typeCode;

    private List<Condition> conditions;
}
