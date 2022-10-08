package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-03 11:13:00 
 * @Description 
 */
@Data
@Entity
@Table ( name ="distEx")
public class DistEx implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "distId")
	private String distId;

 	@Column(name = "distName")
	private String distName;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "tableName")
	private String tableName;

 	@Column(name = "lx")
	private String lx;

 	@Column(name = "lxname")
	private String lxName;

 	@Column(name = "lxid")
	private Integer lxId;

 	@Column(name = "ztId")
	private String ztId;

 	@Column(name = "ztName")
	private String ztName;

 	@Column(name = "acc_modal")
	private Integer accModal;

 	@Column(name = "zth_import")
	private String zthImport;

 	@Column(name = "acc_set")
	private Integer accSet;

 	@Column(name = "disid")
	private Integer disId;

 	@Column(name = "ztTypeId")
	private Integer ztTypeId;

}
