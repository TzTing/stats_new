package com.bright.stats.pojo.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/27 9:27
 * @Description
 */
@Data
public class TableHeader implements Serializable {

    private static final long serialVersionUID = 1L;

    private String field;
    private String title;
    private String id;
    private String pid;
    private String fieldType;
    private String fieldFormat;
    private Integer sort;
    private Integer width;
    private String fixed;
    private String align;
    private List<TableHeader> children;
}
