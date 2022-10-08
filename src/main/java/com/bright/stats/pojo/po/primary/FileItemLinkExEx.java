package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 15:08:41 
 * @Description 
 */

@Data
@Entity
@Table(name = "fileItemLinkExEx")
public class FileItemLinkExEx implements Serializable {

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

	@Column(name = "distType") 
	private String distType;

}
