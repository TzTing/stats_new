package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-03 09:55:24 
 * @Description 
 */
@Data
@Entity
@Table ( name ="tabinLimit_lx")
public class TabInLimitLx implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "tableName")
	private String tableName;

 	@Column(name = "prjType")
	private String prjType;

 	@Column(name = "prjItem")
	private String prjItem;

 	@Column(name = "distType")
	private String distType;

 	@Column(name = "isAdd")
	private Boolean isAdd;

 	@Column(name = "isDelete")
	private Boolean isDelete;

 	@Column(name = "isNotedit")
	private Boolean isNotEdit;

 	@Column(name = "sbisDelete")
	private Boolean sbIsDelete;

 	@Column(name = "isszqc")
	private Boolean isSzQc;

 	@Column(name = "isenable")
	private Boolean isEnable;

 	@Column(name = "tabNavNo")
	private Integer tabNavNo;

 	@Column(name = "disid")
	private Integer disId;

 	@Column(name = "visible")
	private Boolean visible;

}
