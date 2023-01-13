package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * @Author txf
 * @Date 2022-07-25 15:26:03 
 * @Description 
 */
@Data
@Entity
@Table ( name ="operation_log")
public class OperationLog implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

	/** 操作人 */
	@Column(name = "ope_user")
	private String opeUser;

	/** ip地址 */
	@Column(name = "ope_ip")
	private String opeIp;

	/** 请求地址 */
	@Column(name = "req_uri")
	private String reqUri;

	/** 操作的类 */
	@Column(name = "ope_class")
	private String opeClass;

	/** 请求方式 */
	@Column(name = "req_type")
	private String reqType;

	/** 操作说明 */
	@Column(name = "ope_description")
	private String opeDescription;

	/** 传入的参数 */
	@Column(name = "ope_param")
	private String opeParam;

	/** 操作状态 */
	@Column(name = "ope_stats")
	private Boolean opeStats;

	/** 操作结果 */
	@Column(name = "ope_result")
	private String opeResult;

	/** 操作错误的日志 */
	@Column(name = "ope_error_message")
	private String opeErrorMessage;

	/** 操作的模块 */
	@Column(name = "ope_mode")
	private String opeMode;

	/** 执行的语句 */
	@Column(name = "ope_sql")
	private String opeSql;

	/** 操作类型 */
	@Column(name = "ope_type")
	private String opeType;

	/** 操作开始时间 */
	@Column(name = "ope_begin_date")
	private Date opeBeginDate;

	/** 操作结束时间 */
	@Column(name = "ope_end_date")
	private Date opeEndDate;

	/** 操作的数据库信息 */
	@Column(name = "ope_databases")
	private String opeDatabases;

	/** 操作执行时间（毫秒） */
	@Column(name = "ope_exec_time")
	private Long opeExecTime;

}
