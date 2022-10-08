package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-07-29 09:42:11
 * @Description
 */
@Data
@Entity
@Table(name = "rolemain")
public class RoleMain implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "rno")
    private Integer rno;

    @Column(name = "rname")
    private String rname;

    @Column(name = "disid")
    private Integer disId;

    @Column(name = "visible")
    private Boolean visible;

}
