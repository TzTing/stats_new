package com.bright.common.config;

/**
 * @author: Tz
 * @Date: 2022/10/21 0:55
 */
public class SQLConvertor {

    /**
     * ⾮主键字段名称
     * ⾮主键字段名称
     */
    private String[] columnNames;
    /**
     * 主键字段名称
     */
    private String[] pkColumnNames;
    /**
     * ⾮主键值
     */
    private Object[] columnValues;
    /**
     * 主键值
     */
    private Object[] pkColumnValues;
    /**
     * 表名
     */
    private String tableName;
    /**
     * 数据库中NULL值
     */
    private final static String NULL = "NULL";

    private SQLConvertor() {
    }

    private SQLConvertor(String[] pkColumnNames, Object[] pkColumnValues,
                         String[] columnNames, Object[] columnValues, String tableName) {
        super();
        this.columnNames = columnNames;
        this.pkColumnNames = pkColumnNames;
        this.columnValues = columnValues;
        this.pkColumnValues = pkColumnValues;
        this.tableName = tableName;
    }

    public String[] getColumnNames() {
        return columnNames;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public String[] getPkColumnNames() {
        return pkColumnNames;
    }

    public void setPkColumnNames(String[] pkColumnNames) {
        this.pkColumnNames = pkColumnNames;
    }

    public Object[] getColumnValues() {
        return columnValues;
    }

    public void setColumnValues(Object[] columnValues) {
        this.columnValues = columnValues;
    }

    public Object[] getPkColumnValues() {
        return pkColumnValues;
    }


    public void setPkColumnValues (Object[]pkColumnValues){
        this.pkColumnValues = pkColumnValues;
    }
    public String getTableName (){
        return tableName;
    }
    public void setTableName (String tableName){
        this.tableName = tableName;
    }
    public static SQLConvertor newInstance (String[]pkColumns, Object[]pkValues, String[]columns, Object[]
            values, String table){
        return new SQLConvertor(pkColumns, pkValues, columns, values, table);
    }
    public String toInsertSQL () {
        // 表的列名称
        String[] tableColumns = new String[columnNames.length + pkColumnNames.length];
        System.arraycopy(columnNames, 0, tableColumns, 0, columnNames.length);
        System.arraycopy(pkColumnNames, 0, tableColumns, columnNames.length, pkColumnNames.length);
        // 列值
        Object[] tableColumnValues = new Object[columnValues.length + pkColumnValues.length];
        System.arraycopy(columnValues, 0, tableColumnValues, 0, columnValues.length);
        System.arraycopy(pkColumnValues, 0, tableColumnValues, columnValues.length, pkColumnValues.length);
        // 拼接语句
        StringBuffer buff = new StringBuffer();
        buff.append("insert into ");
        buff.append(tableName);
        buff.append(" (");
        StringBuffer values = new StringBuffer();
        values.append("values(");
        int columnCount = tableColumns.length;
        for (int i = 0; i < columnCount; i++) {
            buff.append(tableColumns[i]);
            if (tableColumnValues[i] != null) {
                values.append("'" + tableColumnValues[i] + "'");
            } else {
                values.append(NULL);
            }
            if (i <= columnCount - 1) {
                buff.append(",");
                values.append(",");
            }
        }
        buff.append(") ");
        values.append(")");
        buff.append(values);
        buff.append(";");
        return buff.toString();
    }
    public String toDeleteSQL () {
        StringBuffer buff = new StringBuffer();
        buff.append("delete from ");
        buff.append(tableName);
        buff.append(" where ");
        int pkCount = pkColumnNames.length;
        for (int i = 0; i < pkCount ;
             i++){
            buff.append(pkColumnNames[i]);
            buff.append("=");
            buff.append("'" + pkColumnValues[i] + "'");
            if ( i < pkCount - 1){
                buff.append(" and ");
            }
        }
        buff.append(";");
        return buff.toString();
    }
    public String toUpdateSQL () {
        StringBuffer buff = new StringBuffer();
        buff.append("update ");
        buff.append(tableName);
        buff.append(" set ");
        int updateColumnCount = columnNames.length;
        for (int i = 0; i < updateColumnCount ;
             i++){
            buff.append(columnNames[i]);
            buff.append("=");
            if (columnValues[i] != null) {
                buff.append("'" + columnValues[i] + "'");
            } else {
                buff.append(NULL);
            }
            if ( i <updateColumnCount - 1){
                buff.append(",");
            }
        }
        buff.append(" where ");
        int pkCount = pkColumnNames.length;
        for (int j = 0; j < pkCount ; j++){
            buff.append(pkColumnValues[j]);
            buff.append("=");
            buff.append("'" + pkColumnValues[j] + "'");
            if ( j < pkCount - 1){
                buff.append(" and ");
            }
        }
        buff.append(";");
        return buff.toString();
    }

}
