package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-06-27 16:53:34 
 * @Description 
 */
@Data
@Entity
@Table ( name ="dataProcess_new")
public class DataProcessNew implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "keyword")
	private String keyword;

 	@Column(name = "tableType")
	private String tableType;

 	@Column(name = "processDesc")
	private String processDesc;

 	@Column(name = "processSql")
	private String processSql;

 	@Column(name = "disId")
	private Integer disId;

 	@Column(name = "isAlert")
	private Boolean isAlert;

 	@Column(name = "alertType")
	private Integer alertType;

 	@Column(name = "alert")
	private String alert;

 	@Column(name = "alertSub")
	private String alertSub;

 	@Column(name = "visible")
	private Boolean visible;

 	@Column(name = "isExistSubTable")
	private Boolean isExistSubTable;

 	@Column(name = "subTableColumnName")
	private String subTableColumnName;

 	@Column(name = "subTableSql")
	private String subTableSql;

 	@Column(name = "subTableDescription")
	private String subTableDescription;

 	@Column(name = "subTableMustCheck")
	private Boolean subTableMustCheck;

 	@Column(name = "keys")
	private String keys;

 	@Column(name = "columnKey")
	private String columnKey;

}
