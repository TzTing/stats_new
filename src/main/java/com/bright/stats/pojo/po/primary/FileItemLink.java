package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 16:06:12 
 * @Description 
 */

@Data
@Entity
@Table(name = "fileItemLink")
public class FileItemLink implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id") 
	private Integer id;

	@Column(name = "years") 
	private Integer years;

	@Column(name = "tableName") 
	private String tableName;

	@Column(name = "fieldName") 
	private String fieldName;

	@Column(name = "fieldNameId") 
	private String fieldNameId;

	@Column(name = "PrjType") 
	private String PrjType;

	@Column(name = "PrjItem") 
	private String PrjItem;

}
