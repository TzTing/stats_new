package com.bright.stats.pojo.po.primary;

import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;


/**
 * @Author txf
 * @Date 2022-04-25 14:57:18 
 * @Description 
 */

@Data
@Entity
@Table(name = "fileItem")
public class FileItem implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
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
	private String defdis;

	@Column(name = "iskey")
	private String isKey;

	@Column(name = "disFormat") 
	private String disFormat;

	@Column(name = "defDisFormat") 
	private String defDisFormat;

	@Column(name = "shortDis") 
	private String shortDis;

	@Column(name = "fileitemvalue") 
	private Integer fileItemValue;

	@Column(name = "dw") 
	private Integer dw;

	@Column(name = "isuse_import") 
	private Boolean isUseImport;

	@Column(name = "isuse_Emptytab") 
	private Boolean isUseEmptyTab;

	@Column(name = "isFrozen") 
	private Boolean isFrozen;

	@Column(name = "isSortable") 
	private Boolean isSortable;

	@Column(name = "align") 
	private Integer align;

	@Column(name = "formatter") 
	private String formatter;

	@Column(name = "styler") 
	private String styler;

	@Column(name = "extdis") 
	private String extDis;

	@Column(name = "ismaykey") 
	private Boolean isMayKey;

	@Column(name = "isSumColumn") 
	private Boolean isSumColumn;

	@Column(name = "ChangeColumn") 
	private String changeColumn;

	@Column(name = "isFX") 
	private Boolean isFx;

	@Column(name = "isyj") 
	private Boolean isYj;

	@Column(name = "importFlag")
	private Boolean importFlag;

	@Column(name = "formatterSelect")
	private String formatterSelect;
}
