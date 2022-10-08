package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-05 11:30:33 
 * @Description 
 */
@Data
@Entity
@Table ( name ="_sqlInfoItem")
public class SqlInfoItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "tablename")
	private String tableName;

 	@Column(name = "fieldname")
	private String fieldName;

 	@Column(name = "fielddis")
	private String fieldDis;

 	@Column(name = "disflag")
	private String disFlag;

 	@Column(name = "disId")
	private Integer disId;

 	@Column(name = "fLen")
	private Integer fLen;

 	@Column(name = "fDec")
	private Integer fDec;

 	@Column(name = "fType")
	private String fType;

 	@Column(name = "defLen")
	private Integer defLen;

 	@Column(name = "defId")
	private Integer defId;

 	@Column(name = "defDis")
	private String defDis;

 	@Column(name = "iskey")
	private String isKey;

 	@Column(name = "disFormat")
	private String disFormat;

 	@Column(name = "defDisFormat")
	private String defDisFormat;

 	@Column(name = "shortDis")
	private String shortDis;

 	@Column(name = "readOnly")
	private Boolean readOnly;

 	@Column(name = "align")
	private Integer align;

 	@Column(name = "formatter")
	private String formatter;

 	@Column(name = "isFrozen")
	private Boolean isFrozen;

 	@Column(name = "updateSql")
	private String updateSql;

 	@Column(name = "isSortable")
	private Boolean isSortable;

 	@Column(name = "ishidden")
	private Boolean isHidden;

 	@Column(name = "isgroup")
	private Boolean isGroup;

}
