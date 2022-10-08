package com.bright.stats.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Author txf
 * @Date 2022/6/7 11:03
 * @Description
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckVO implements Serializable, Comparable {

    private static final long serialVersionUID = 1L;

    private Integer id;
    private String tableName;
    private String shortDis;
    private String distId;
    private String distName;
    private String lx;
    private String lxName;
    private String tip;
    private BigDecimal value1;
    private BigDecimal value2;
    private BigDecimal value3;

    @Override
    public int compareTo(Object o) {
        CheckVO v = (CheckVO) o;
        return ((this.distId.compareTo(v.getDistId()) == 0) ? this.lx.compareTo(v.getLx()) : this.distId.compareTo(v.getDistId()));
    }
}
