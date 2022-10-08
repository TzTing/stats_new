package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-07-25 15:26:03 
 * @Description 
 */
@Data
@Entity
@Table ( name ="bs_config")
public class BsConfig implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "type")
	private String type;

 	@Column(name = "valuename")
	private String valueName;

 	@Column(name = "express")
	private String express;

 	@Column(name = "detail")
	private String detail;

 	@Column(name = "datatype")
	private String datatype;

 	@Column(name = "chkflag")
	private Boolean chkFlag;

}
