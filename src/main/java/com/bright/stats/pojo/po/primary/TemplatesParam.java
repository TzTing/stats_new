package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-08-01 14:14:33 
 * @Description 
 */
@Data
@Entity
@Table ( name ="templatesparam")
public class TemplatesParam implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

 	@Column(name = "templateName")
	private String templateName;

 	@Column(name = "tablename")
	private String tableName;

 	@Column(name = "years")
	private Integer years;

 	@Column(name = "item")
	private Integer item;

 	@Column(name = "stype")
	private String stype;

 	@Column(name = "dec")
	private String dec;

 	@Column(name = "processky")
	private String processKy;

 	@Column(name = "skey")
	private String skey;

 	@Column(name = "defV")
	private String defV;

 	@Column(name = "selSql")
	private String selSql;

 	@Column(name = "afterSql")
	private String afterSql;

 	@Column(name = "extdis")
	private String extDis;

 	@Column(name = "orderbysql")
	private String orderBySql;

 	@Column(name = "c7")
	private String c7;

 	@Column(name = "c8")
	private String c8;

 	@Column(name = "c9")
	private String c9;

 	@Column(name = "c10")
	private String c10;

 	@Column(name = "c11")
	private String c11;

 	@Column(name = "orderid")
	private Integer orderId;

 	@Column(name = "visible")
	private Boolean visible;

}
