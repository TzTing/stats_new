package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 15:27:56 
 * @Description 
 */

@Data
@Entity
@Table(name = "_sRuleOuter")
public class RuleOuter implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "ID") 
	private Integer id;

	@Column(name = "years") 
	private Integer years;

	@Column(name = "tableDec") 
	private String tableDec;

	@Column(name = "decExpress") 
	private String decExpress;

	@Column(name = "sourceExpress") 
	private String sourceExpress;

	@Column(name = "Detail") 
	private String detail;

	@Column(name = "exCond") 
	private String exCond;

	@Column(name = "opt") 
	private String opt;

	@Column(name = "orderId") 
	private Integer orderId;

	@Column(name = "evalFlag") 
	private Boolean evalFlag;

	@Column(name = "sourceCondExpress") 
	private String sourceCondExpress;

	@Column(name = "tableflag") 
	private String tableFlag;

	@Column(name = "lx") 
	private String lx;

	@Column(name = "stype") 
	private Integer sType;

}
