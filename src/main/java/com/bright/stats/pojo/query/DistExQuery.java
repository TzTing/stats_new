package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.PageQuery;
import lombok.Data;

/**
 * @author: Tz
 * @Date: 2022/10/24 10:49
 */
@Data
public class DistExQuery extends PageQuery {

    private Integer years;
    private Integer months;
    private String tableName;
    private Integer grade;
    private String distNo;
    private String typeCode;
    private String userDistNo;
}
