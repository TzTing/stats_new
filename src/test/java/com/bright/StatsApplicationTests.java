package com.bright;

import com.alibaba.fastjson2.JSON;
import com.bright.stats.pojo.po.primary.OperationLog;
import com.bright.stats.repository.primary.OperationLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/9 9:31
 * @Description
 */
@SpringBootTest
public class StatsApplicationTests {

    @Autowired
    private OperationLogRepository operationLogRepository;


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
        operationLogRepository.deleteById(49842);
    }
    
    @Test
    void test2() throws UnsupportedEncodingException {

        String s = new String("年综表".getBytes("gb2312"), "ISO8859-1");
        System.out.println(s);
    }


}
