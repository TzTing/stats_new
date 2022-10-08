package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @Author txf
 * @Date 2022-06-28 15:49:01 
 * @Description 
 */
@Data
@Entity
@Table ( name ="excelTemplates")
public class ExcelTemplate implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "tableName")
	private String tableName;

 	@Column(name = "tableDis")
	private String tableDis;

 	@Column(name = "shortDis")
	private String shortDis;

 	@Column(name = "name")
	private String name;

 	@Column(name = "fileName")
	private String fileName;

 	@Column(name = "writer")
	private String writer;

 	@Column(name = "writeDate")
	private Date writeDate;

 	@Column(name = "visible")
	private Boolean visible;

 	@Column(name = "type")
	private String type;

 	@Column(name = "excelType")
	private String excelType;

 	@Column(name = "templateShape")
	private Integer templateShape;

 	@Column(name = "tableType")
	private String tableType;

 	@Column(name = "ECX")
	private String ecx;

 	@Column(name = "typeCode")
	private String typeCode;

 	@Column(name = "jxMode")
	private Integer jxMode;

 	@Column(name = "ybName")
	private String ybName;

 	@Column(name = "exc_describe")
	private String excDescribe;

}
