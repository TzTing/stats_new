package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-07-28 17:28:21 
 * @Description 
 */
@Data
@Entity
@Table ( name ="navigate")
public class Navigate implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "navNo")
	private String navNo;

 	@Column(name = "navName")
	private String navName;

 	@Column(name = "navImage")
	private String navImage;

 	@Column(name = "navUrl")
	private String navUrl;

 	@Column(name = "navScript")
	private String navScript;

 	@Column(name = "leftUrl")
	private String leftUrl;

 	@Column(name = "rightUrl")
	private String rightUrl;

 	@Column(name = "visible")
	private Boolean visible;

 	@Column(name = "disId")
	private Integer disId;

 	@Column(name = "image1")
	private String image1;

 	@Column(name = "image2")
	private String image2;

 	@Column(name = "groupName")
	private String groupName;

}
