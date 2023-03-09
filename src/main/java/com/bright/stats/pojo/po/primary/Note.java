package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * @author: Tz
 * @Date: 2023/02/04 16:28
 */
@Data
@Entity
@Table(name = "notes")
public class Note implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "description")
    private String description;

    @Column(name = "sqlstr")
    private String sqlStr;

    @Column(name = "visible")
    private Integer visible;

    @Column(name = "disId")
    private String disId;

    @Column(name = "typecode")
    private String typeCode;

    @Column(name = "keyword")
    private String keyword;

    @Column(name = "alterSql")
    private String alterSql;

    @Column(name = "alterType")
    private String alterType;

    @Column(name = "alterUrl")
    private String alterUrl;

    @Column(name = "alterParam")
    private String alterParam;


}
