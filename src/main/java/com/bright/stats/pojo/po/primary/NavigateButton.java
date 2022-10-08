package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-07-29 09:22:17 
 * @Description 
 */
@Data
@Entity
@Table ( name ="navigate_button")
public class NavigateButton implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "navName")
	private String navName;

 	@Column(name = "buttonId")
	private String buttonId;

 	@Column(name = "buttonDes")
	private String buttonDes;

 	@Column(name = "iconCls")
	private String iconCls;

 	@Column(name = "buttonImage")
	private String buttonImage;

 	@Column(name = "buttonScript")
	private String buttonScript;

 	@Column(name = "isPop")
	private Boolean isPop;

 	@Column(name = "popList")
	private String popList;

 	@Column(name = "disId")
	private Integer disId;

 	@Column(name = "subNavNo")
	private String subNavNo;

 	@Column(name = "ishref")
	private Boolean isHref;

 	@Column(name = "visible")
	private Boolean visible;

 	@Column(name = "enable")
	private Boolean enable;

 	@Column(name = "tabNavNo")
	private Integer tabNavNo;

}
