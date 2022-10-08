package com.bright.stats.pojo.po.primary;

import com.bright.stats.pojo.model.TableHeader;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Map;


/**
 * @Author txf
 * @Date 2022-04-25 14:58:36 
 * @Description 
 */

@Data
@Entity
@Table(name = "fileList")
public class FileList implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name = "id") 
	private Integer id;

	@Column(name = "years") 
	private Integer years;

	@Column(name = "tableName") 
	private String tableName;

	@Column(name = "tableDis") 
	private String tableDis;

	@Column(name = "useFlag") 
	private String useFlag;

	@Column(name = "dataFlag") 
	private String dataFlag;

	@Column(name = "espFlag") 
	private String espFlag;

	@Column(name = "typeCode") 
	private String typeCode;

	@Column(name = "gradeFlag") 
	private String gradeFlag;

	@Column(name = "shortDis") 
	private String shortDis;

	@Column(name = "deptflag") 
	private String deptFlag;

	@Column(name = "memo") 
	private String memo;

	@Column(name = "singleFlag") 
	private String singleFlag;

	@Column(name = "tableType") 
	private String tableType;

	@Column(name = "issys") 
	private String isSys;

	@Column(name = "orderId") 
	private Integer orderId;

	@Column(name = "orderStr") 
	private String orderStr;

	@Column(name = "ansNo") 
	private Integer ansNo;

	@Column(name = "upUserGrade") 
	private String upUserGrade;

	@Column(name = "inputrow") 
	private Integer inputRow;

	@Column(name = "roleGrade") 
	private String roleGrade;

	@Column(name = "updateFlag") 
	private Boolean updateFlag;

	@Column(name = "beforeStr") 
	private String beforeStr;

	@Column(name = "priorWtr") 
	private String priorWtr;

	@Column(name = "typediscription") 
	private String typeDescription;

	@Column(name = "isImportDataByFileList") 
	private Boolean isImportDataByFileList;

	@Column(name = "printPage") 
	private Integer printPage;

	@Column(name = "isWanShow") 
	private Boolean isWanShow;

	@Column(name = "tabNavNo") 
	private Integer tabNavNo;

	@Column(name = "isDeleteTab") 
	private Integer isDeleteTab;

	@Column(name = "isNotedit") 
	private Boolean isNotEdit;

	@Column(name = "isImportData") 
	private Boolean isImportData;

	@Column(name = "sqlstr") 
	private String sqlStr;

	@Column(name = "iswan_sum") 
	private Boolean isWanSum;

	@Column(name = "presavaStr") 
	private String preSavaStr;

	@Column(name = "belongDistNo")
	private String belongDistNo;

    @Transient
    private List<FileItem> fileItems;
	@Transient
	List<TableHeader> tableHeaders;
	@Transient
	private FileItemLink fileItemLink;
	@Transient
	private List<FileItemLinkEx> fileItemLinkExs;
	@Transient
	private Map<String, List<FileItemLinkExEx>> fileItemLinkExExsMap;
	@Transient
	private List<RuleInner> ruleInners;
	@Transient
	private List<RuleOuter> ruleOuters;
	@Transient
	private List<AnsTable> ansTables;
}
