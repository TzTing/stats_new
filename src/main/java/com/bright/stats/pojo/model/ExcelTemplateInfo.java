package com.bright.stats.pojo.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/7/13 14:58
 * @Description
 */
@Data
public class ExcelTemplateInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String tableName;
    private Integer beginRow;
    private Map<String, Object> columns;
    private String[] tabArray;
    private int[] distPosition;
    private int[] ztNamePosition;
    private int[] yearsPosition;
    private int[] monthsPosition;

    private int distPositionIndex;
    private int ztNamePositionIndex;
    private int yearsIndex;
    private int monthsIndex;
    private int sheetIndex;
    private int years;
    private int months;
    private String cellStr;

    private List<Map<String, Object>> cellTags;
}
