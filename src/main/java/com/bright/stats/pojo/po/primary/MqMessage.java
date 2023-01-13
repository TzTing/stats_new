package com.bright.stats.pojo.po.primary;

import lombok.Builder;
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
@Table ( name ="mq_message")
public class MqMessage<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id")
	private Integer id;

	/** 关键词 */
	@Column(name = "keyword")
	private String keyword;

	/** 主题名称 */
	@Column(name = "topic_name")
	private String topicName;

	/** 主题类型 */
	@Column(name = "topic_type")
	private String topicType;

	/** 用户名 */
	@Column(name = "username")
	private String username;

	/** 年份 */
	@Column(name = "years")
	private Integer years;

	/** 月份 */
	@Column(name = "months")
	private Integer months;

	/** 地区编号 */
	@Column(name = "dist_no")
	private String distNo;

	/** 地区名称 */
	@Column(name = "dist_name")
	private String distName;

	/** 模式类型 */
	@Column(name = "type_code")
	private String typeCode;

	/** 内容类型 */
	@Column(name = "content_type")
	private Integer contentType;

	/** 内容 */
	@Column(name = "content")
	private String content;

	/** 消费状态 */
	@Column(name = "consumer_flag")
	private Integer consumerFlag;

	/** 消息是否可用 */
	@Column(name = "run_flag")
	private Boolean runFlag;

	/** 是否消费 */
	@Column(name = "ok_flag")
	private Boolean okFlag;

	/** 是否已读消息 */
	@Column(name = "read_flag")
	private Boolean readFlag;

	/** 创建人 */
	@Column(name = "created_by")
	private String createdBy;

	/** 创建时间 */
	@Column(name = "created_time")
	private Date createdTime;

	/** 更新人 */
	@Column(name = "updated_by")
	private String updatedBy;

	/** 更新时间 */
	@Column(name = "updated_time")
	private Date updatedTime;

	/** 执行耗时时间 */
	@Column(name = "exec_time")
	private Long execTime;


	@Transient
	private T data;
}
