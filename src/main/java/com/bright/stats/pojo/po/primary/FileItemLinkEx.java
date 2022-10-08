package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 14:54:40 
 * @Description 
 */

@Data
@Entity
@Table(name = "fileItemLinkEx")
public class FileItemLinkEx implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id") 
	private Integer id;

	@Column(name = "years") 
	private Integer years;

	@Column(name = "tableName") 
	private String tableName;

	@Column(name = "prjType") 
	private String prjType;

	@Column(name = "prjItem") 
	private String prjItem;

}
