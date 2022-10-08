package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 15:27:48 
 * @Description 
 */

@Data
@Entity
@Table(name = "_sRuleInner")
public class RuleInner implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID") 
	private Integer id;

	@Column(name = "years") 
	private Integer years;

	@Column(name = "tableName") 
	private String tableName;

	@Column(name = "fieldName") 
	private String fieldName;

	@Column(name = "express") 
	private String express;

	@Column(name = "detail") 
	private String detail;

	@Column(name = "opt") 
	private String opt;

	@Column(name = "orderId") 
	private Integer orderId;

	@Column(name = "useFlag") 
	private Boolean useFlag;

	@Column(name = "readOnly") 
	private Boolean readonly;

	@Column(name = "evalFlag") 
	private Boolean evalFlag;

}
