package com.bright.stats.pojo.po.primary;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @Author txf
 * @Date 2022-06-28 10:12:57 
 * @Description 
 */
@Data
@Entity
@Table ( name ="uploadBase")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE)
public class UploadBase implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "sno")
	private Integer sno;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "months")
	private Integer months;

 	@Column(name = "tableType")
	private String tableType;

 	@Column(name = "type")
	private String type;

 	@Column(name = "name")
	private String name;

 	@Column(name = "bdate")
	private Date bDate;

 	@Column(name = "edate")
	private Date eDate;

 	@Column(name = "okflag")
	private Boolean okFlag;

 	@Column(name = "runflag")
	private Boolean runFlag;

 	@Column(name = "distno")
	private String distNo;

 	@Column(name = "distName")
	private String distName;

 	@Column(name = "sbflag")
	private Boolean sbFlag;

 	@Column(name = "writer")
	private String writer;

}
