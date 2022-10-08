package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-06-02 10:49:07
 * @Description
 */
@Data
@Entity
@Table(name = "tableType")
public class TableType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "tableType")
    private String tableType;

    @Column(name = "optType")
    private Integer optType;

    @Column(name = "beginYear")
    private Integer beginYear;

    @Column(name = "beginMonth")
    private Integer beginMonth;

    @Column(name = "curYear")
    private Integer curYear;

    @Column(name = "curMonth")
    private Integer curMonth;

    @Column(name = "useFlag")
    private String useFlag;

    @Column(name = "sysDef")
    private String sysDef;

    @Column(name = "visible")
    private Boolean visible;

    @Column(name = "istype")
    private Integer isType;

    @Column(name = "isautoIntoData")
    private Boolean isAutoIntoData;

    @Column(name = "editSumOrder")
    private Integer editSumOrder;

}
