package com.bright.stats.pojo.query;

import com.bright.common.pojo.query.PageQuery;
import lombok.Data;

/**
 * @Author txf
 * @Date 2022/7/5 16:31
 * @Description
 */
@Data
public class UploadBaseQuery extends PageQuery {

    private static final long serialVersionUID = 1L;

    private Integer years;
    private Integer months;
    private String distNo;
    private String typeCode;

    private String userDistNo;

}
