package com.bright;

import com.alibaba.fastjson2.JSON;
import com.bright.stats.pojo.po.primary.OperationLog;
import com.bright.stats.repository.primary.OperationLogRepository;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/9 9:31
 * @Description
 */
@SpringBootTest
public class StatsApplicationTests {

    @Resource
    @Qualifier("jdbcTemplatePrimary")
    private JdbcTemplate primaryJdbcTemplate;


    @Test
    void contextLoads() throws FileNotFoundException {
//        // 写法1
//        String fileName = ResourceUtils.getFile("classpath:") + "noModelWrite" + System.currentTimeMillis() + ".xlsx";
//        // 这里 需要指定写用哪个class去写，然后写到第一个sheet，名字为模板 然后文件流会自动关闭
//        EasyExcel.write(fileName).head(head()).sheet("模板").doWrite(dataList());
    }
//
//    private List<List<String>> head() {
//        List<List<String>> list = ListUtils.newArrayList();
//        List<String> head0 = ListUtils.newArrayList();
//        head0.add("字符串" + System.currentTimeMillis());
//        List<String> head1 = ListUtils.newArrayList();
//        head1.add("数字" + System.currentTimeMillis());
//        List<String> head2 = ListUtils.newArrayList();
//        head2.add("日期" + System.currentTimeMillis());
//        list.add(head0);
//        list.add(head1);
//        list.add(head2);
//        return list;
//    }
//
//    private List<List<Object>> dataList() {
//        List<List<Object>> list = ListUtils.newArrayList();
//        for (int i = 0; i < 10; i++) {
//            List<Object> data = ListUtils.newArrayList();
//            data.add("字符串" + i);
//            data.add(new Date());
//            data.add(0.56);
//            list.add(data);
//        }
//        return list;
//    }


    @Test
    void contextLoads1() {
        BigDecimal bigDecimal1 = BigDecimal.valueOf(1);
        BigDecimal bigDecimal2 = BigDecimal.valueOf(6);
        BigDecimal bigDecimal3 = BigDecimal.valueOf(100);
        BigDecimal multiply = bigDecimal1.divide(bigDecimal2).multiply(bigDecimal3);
        System.out.println(multiply);
    }

    @Test
    void contextLoads2() {
    }
    
    @Test
    void test2() throws UnsupportedEncodingException {
    }


    @Test
    void test(){

        String tableName = "rep905";
        String excludeColumns = "years,sumflag";

        String columns = "";
        String modifyColumn = "";
        String tableAlias = "z";

        //获取当前表的所有字段
        String tableFieldsSql = "  select column_name from all_tab_columns where table_name = ? " +
                "AND OWNER = (SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID())) " +
                "AND column_name not in (select column_name from all_cons_columns where table_name = ? and owner = (SELECT SF_GET_SCHEMA_NAME_BY_ID(CURRENT_SCHID()))) " +
                "AND column_name not in  " +
                "  (select regexp_substr(?,'[^,]+',1,level,'i') as tempcol from dual connect by level <= LENGTH(TRANSLATE(?,','||?,','))+1)";


        List<Map<String, Object>> tableFieldsMaps
                = primaryJdbcTemplate.queryForList(tableFieldsSql, new Object[]{tableName, tableName, excludeColumns, excludeColumns, excludeColumns});

        List<String> tableFieldsList = tableFieldsMaps.stream().map(e -> e.get("column_name").toString()).collect(Collectors.toList());


        columns = tableFieldsList.stream().collect(Collectors.joining(","));
        modifyColumn = tableFieldsList.stream().map(e -> {
            if (StringUtils.isNotBlank(tableAlias)){
                return tableAlias + "." + e;
            }
            return e;
        }).collect(Collectors.joining(", "));
        System.out.println(tableFieldsList);

        System.out.println(columns);
        System.out.println(modifyColumn);
    }

   /* @Test
    void dmTest(){
        try
        {
            LogmnrDll.initLogmnr();
            long connid = LogmnrDll.createConnect("localhost", 5236, "SYSDBA",	"SYSDBA");
            LogmnrDll.addLogFile(connid,"D:\\dmdata\\arch\\ARCHIVE_LOCAL1_20160704082303068.log",3);
            LogmnrDll.startLogmnr(connid, -1, null, null);
            LogmnrRecord[] arr = LogmnrDll.getData(connid, 100);
            PrintStream ps = new PrintStream("D:\\dmdata\\result.txt");
            System.setOut(ps);
            System.out.println("日志分析结果打印：");
            for (int i =  0; i < arr.length; i++) {
                System.out.println("-----------------------------" + i + "-----------------------------" + "\n");
                System.out.println("xid:" + arr[i].getXid() + "\n");
                System.out.println("operation:" + arr[i].getOperation() + "\n");
                System.out.println("sqlRedo:" + arr[i].getSqlRedo() + "\n");
                System.out.println("########################" + i + "########################" + "\n");
                System.out.println("scn:" + arr[i].getScn() + "\n");
                System.out.println("startScn:" + arr[i].getStartScn() + "\n");
                System.out.println("commitScn:" + arr[i].getCommitScn() + "\n");
                System.out.println("timestamp:" + arr[i].getTimestamp() + "\n");
                System.out.println("startTimestamp:" + arr[i].getStartTimestamp() + "\n");
                System.out.println("commitTimestamp:" + arr[i].getCommitTimestamp() + "\n");
                System.out.println("operationCode:" + arr[i].getOperationCode() + "\n");
                System.out.println("rollBack:" + arr[i].getRollBack() + "\n");
                System.out.println("segOwner:" + arr[i].getSegOwner() + "\n");
                System.out.println("tableName:" + arr[i].getTableName() + "\n");
                System.out.println("rowId:" + arr[i].getRowId() + "\n");
                System.out.println("rbasqn:" + arr[i].getRbasqn() + "\n");
                System.out.println("rbablk:" + arr[i].getRbablk() + "\n");
                System.out.println("rbabyte:" + arr[i].getRbabyte() + "\n");
                System.out.println("dataObj:" + arr[i].getDataObj() + "\n");
                System.out.println("dataObjv:" + arr[i].getDataObjv() + "\n");
                System.out.println("//dataObjd:" + arr[i].getDataObjd() + "\n");
                System.out.println("rsId:" + arr[i].getRsId() + "\n");
                System.out.println("ssn:" + arr[i].getSsn() + "\n");
                System.out.println("csf:" + arr[i].getCsf() + "\n");
                System.out.println("status:" + arr[i].getStatus() + "\n");
                System.out.println("########################" + i + "########################" + "\n");
            }
            System.out.println("结果打印完毕");
            ps.flush();
            ps.close();
            LogmnrDll.endLogmnr(connid, 1);
            LogmnrDll.deinitLogmnr();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        try
        {
            LogmnrDll.initLogmnr();
            long connid = LogmnrDll.createConnect("localhost", 5236, "SYSDBA",	"SYSDBA");
            LogmnrDll.addLogFile(connid,"D:\\dmdata\\arch\\ARCHIVE_LOCAL1_20160704082303068.log",3);
            LogmnrDll.startLogmnr(connid, -1, null, null);
            LogmnrRecord[] arr = LogmnrDll.getData(connid, 100);
            PrintStream ps = new PrintStream("D:\\dmdata\\result.txt");
            System.setOut(ps);
            System.out.println("日志分析结果打印：");
            for (int i =  0; i < arr.length; i++) {
                System.out.println("-----------------------------" + i + "-----------------------------" + "\n");
                System.out.println("xid:" + arr[i].getXid() + "\n");
                System.out.println("operation:" + arr[i].getOperation() + "\n");
                System.out.println("sqlRedo:" + arr[i].getSqlRedo() + "\n");
                System.out.println("########################" + i + "########################" + "\n");
                System.out.println("scn:" + arr[i].getScn() + "\n");
                System.out.println("startScn:" + arr[i].getStartScn() + "\n");
                System.out.println("commitScn:" + arr[i].getCommitScn() + "\n");
                System.out.println("timestamp:" + arr[i].getTimestamp() + "\n");
                System.out.println("startTimestamp:" + arr[i].getStartTimestamp() + "\n");
                System.out.println("commitTimestamp:" + arr[i].getCommitTimestamp() + "\n");
                System.out.println("operationCode:" + arr[i].getOperationCode() + "\n");
                System.out.println("rollBack:" + arr[i].getRollBack() + "\n");
                System.out.println("segOwner:" + arr[i].getSegOwner() + "\n");
                System.out.println("tableName:" + arr[i].getTableName() + "\n");
                System.out.println("rowId:" + arr[i].getRowId() + "\n");
                System.out.println("rbasqn:" + arr[i].getRbasqn() + "\n");
                System.out.println("rbablk:" + arr[i].getRbablk() + "\n");
                System.out.println("rbabyte:" + arr[i].getRbabyte() + "\n");
                System.out.println("dataObj:" + arr[i].getDataObj() + "\n");
                System.out.println("dataObjv:" + arr[i].getDataObjv() + "\n");
                System.out.println("//dataObjd:" + arr[i].getDataObjd() + "\n");
                System.out.println("rsId:" + arr[i].getRsId() + "\n");
                System.out.println("ssn:" + arr[i].getSsn() + "\n");
                System.out.println("csf:" + arr[i].getCsf() + "\n");
                System.out.println("status:" + arr[i].getStatus() + "\n");
                System.out.println("########################" + i + "########################" + "\n");
            }
            System.out.println("结果打印完毕");
            ps.flush();
            ps.close();
            LogmnrDll.endLogmnr(connid, 1);
            LogmnrDll.deinitLogmnr();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
*/

}
