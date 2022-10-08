package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/7 15:50
 * @Description
 */
@Data
public class ImportExcelVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String keyword;
    private Integer row;
    private List<Object> excelData;
    private Boolean isSuccess;
    private String info;
    private String successSql;
}
