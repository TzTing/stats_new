package com.bright.stats.pojo.po.primary;

import com.bright.stats.pojo.model.HtmlSqlInfoItem;
import com.bright.stats.pojo.model.TableHeader;
import lombok.Data;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * @Author txf
 * @Date 2022-08-04 11:48:46
 * @Description
 */
@Data
@Entity
@Table(name = "_sqlInfo")
public class SqlInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "years")
    private Integer years;

    @Column(name = "modalName")
    private String modalName;

    @Column(name = "tableName")
    private String tableName;

    @Column(name = "sqlstr")
    private String sqlStr;

    @Column(name = "selectCountSql")
    private String selectCountSql;

    @Column(name = "paras")
    private String paras;

    @Column(name = "orderSql")
    private String orderSql;

    @Column(name = "calculSql")
    private String calCulSql;

    @Column(name = "lookUpSql")
    private String lookUpSql;

    @Column(name = "fieldSql")
    private String fieldSql;

    @Column(name = "whereSql")
    private String whereSql;

    @Column(name = "disFieldSql")
    private String disFieldSql;

    @Column(name = "tableType")
    private String tableType;

    @Column(name = "tableDis")
    private String tableDis;

    @Column(name = "txtFlag")
    private Boolean txtFlag;

    @Column(name = "pubFlag")
    private Boolean pubFlag;

    @Column(name = "userName")
    private String userName;

    @Column(name = "inSqlFlag")
    private Boolean inSqlFlag;

    @Column(name = "orderid")
    private Integer orderId;

    @Column(name = "memo")
    private String memo;

    @Column(name = "cols")
    private Integer cols;

    @Column(name = "processSql")
    private String processSql;

    @Column(name = "fieldname")
    private String fieldName;

    @Column(name = "sqlNo")
    private String sqlNo;

    @Column(name = "groupName")
    private String groupName;

    @Transient
    private List<SqlInfoItem> sqlInfoItems;

    @Transient
    private List<TableHeader> tableHeaders;

    @Transient
    private List<List<HtmlSqlInfoItem>> htmlSqlInfoItems;
}
