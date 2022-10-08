package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-07-29 09:36:46 
 * @Description 
 */
@Data
@Entity
@Table ( name ="rolefunction")
public class RoleFunction implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "nname")
	private String nname;

 	@Column(name = "navno")
	private String navNo;

 	@Column(name = "navname")
	private String navName;

 	@Column(name = "parentName")
	private String parentName;

 	@Column(name = "rno")
	private Integer rno;

 	@Column(name = "disid")
	private Integer disId;

 	@Column(name = "visible")
	private Boolean visible;

}
