package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @Author txf
 * @Date 2022-07-14 09:51:52
 * @Description
 */
@Data
@Entity
@Table(name = "dist")
public class Dist implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "distId")
    private String distId;

    @Column(name = "distName")
    private String distName;

    @Column(name = "years")
    private Integer years;

    @Column(name = "distIdParent")
    private String distIdParent;

    @Column(name = "distType")
    private String distType;

    @Column(name = "memo")
    private String memo;

    @Column(name = "GradeType")
    private String gradeType;

    @Column(name = "server")
    private String server;

    @Column(name = "linkType")
    private String linkType;

    @Column(name = "distPass")
    private String distPass;

    @Column(name = "import_distNo")
    private String importDistNo;

    @Column(name = "sbtime")
    private Date sbTime;

    @Column(name = "import_name")
    private String importName;

    @Column(name = "nblimit")
    private Boolean nbLimit;

    @Column(name = "nbismust")
    private Boolean nbIsMust;

    @Column(name = "orderid")
    private Integer orderId;

    @Column(name = "cjlx")
    private Integer cjLx;

    @Transient
    private Boolean childrenExistFlag;
}
