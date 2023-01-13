package com.bright.stats.pojo.dto;

import lombok.Data;

/**
 * @author: Tz
 * @Date: 2022/11/14 15:03
 */
@Data
public class SumBaseDataDTO {
    String tableType;
    Integer years;
    Integer months;
    String tableName;
    String distId;
    String columns;
    String sumColumns;
    Integer distIdInt;
    Integer maxDistIdInt;
    Integer distIdLen;
    Integer distIdType;
    String lx;
    Boolean isHzsOtherLx;
    Boolean existsDataType;
    Boolean isExistsLx;
    Boolean linkDist;
    String parentLx;
}
