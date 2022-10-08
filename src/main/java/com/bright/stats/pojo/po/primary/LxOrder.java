package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-03 10:41:43 
 * @Description 
 */
@Data
@Entity
@Table ( name ="lxorder")
public class LxOrder implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "typeCode")
	private String typeCode;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "lx")
	private String lx;

 	@Column(name = "lxid")
	private Integer lxId;

 	@Column(name = "disid")
	private Integer disId;

 	@Column(name = "parentLx")
	private String parentLx;

 	@Column(name = "pulldownShow")
	private Boolean pullDownShow;

}
