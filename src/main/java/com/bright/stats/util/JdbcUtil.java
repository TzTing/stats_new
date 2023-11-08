package com.bright.stats.util;

import org.springframework.jdbc.core.CallableStatementCallback;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JdbcUtil {


    /**
     * 人大金仓执行语句块报错Multiple ResultSets were returned by the query 用此方法返回第一个结果集
     * hxj
     * 2023/6/6
     *
     */
    public static List<Map<String, Object>> queryForMapListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql) {
        return queryForMapListGetFirstResultSet(jdbcTemplate, sql, Object.class);
    }


    public static <T> List<Map<String, T>> queryForMapListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql, Class<T> elementType) {

        return jdbcTemplate.execute(sql, (CallableStatementCallback<List<Map<String, T>>>) cs -> {
            List<Map<String, T>> list = new ArrayList<>();
            boolean execute = cs.execute();

            if (execute) {
                ResultSet resultSet = cs.getResultSet();
                while (resultSet.next()) {
                    ResultSetMetaData meta = resultSet.getMetaData();
                    int colcount = meta.getColumnCount();
                    Map<String, T> map = new HashMap<>();
                    for (int i = 1; i <= colcount; i++) {
                        String name = meta.getColumnLabel(i);
                        if (resultSet.getObject(i) instanceof Integer) {
                            map.put(name, (T) new Integer(resultSet.getInt(i)));
                        } else {
                            map.put(name, resultSet.getObject(i, elementType));
                        }
                    }
                    list.add(map);
                }
//                execute = cs.getMoreResults();
            }
            return list;
        });
    }

//    public static List<List<Object>> queryForListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql) {
//        return jdbcTemplate.execute(sql, (CallableStatementCallback<List<List<Object>>>) cs -> {
//            List<List<Object>> list = new ArrayList<>();
//            boolean execute = cs.execute();
//
//            if (execute) {
//                ResultSet resultSet = cs.getResultSet();
//                while (resultSet.next()) {
//                    ResultSetMetaData meta = resultSet.getMetaData();
//                    int colcount = meta.getColumnCount();
//                    List<Object> subList = new ArrayList<>();
//                    for (int i = 1; i <= colcount; i++) {
//                        subList.add(resultSet.getObject(i));
//                    }
//                    list.add(subList);
//                }
////                execute = cs.getMoreResults();
//            }
//            return list;
//        });
//    }

    public static List<List<Object>> queryForListListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql) {
        return queryForListListGetFirstResultSet(jdbcTemplate, sql, Object.class);
    }

    public static <T> List<List<T>> queryForListListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql, Class<T> elementType) {
        return jdbcTemplate.execute(sql, (CallableStatementCallback<List<List<T>>>) cs -> {
            List<List<T>> list = new ArrayList<>();
            boolean execute = cs.execute();

            if (execute) {
                ResultSet resultSet = cs.getResultSet();
                while (resultSet.next()) {
                    ResultSetMetaData meta = resultSet.getMetaData();
                    int colcount = meta.getColumnCount();
                    List<T> subList = new ArrayList<>();
                    for (int i = 1; i <= colcount; i++) {
                        subList.add(resultSet.getObject(i, elementType));
                        if (resultSet.getObject(i) instanceof Integer) {
                            subList.add((T) new Integer(resultSet.getInt(i)));
                        } else {
                            subList.add(resultSet.getObject(i, elementType));
                        }
                    }
                    list.add(subList);
                }
//                execute = cs.getMoreResults();
            }
            return list;
        });
    }

    public static List<Object> queryForListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql) {
        return queryForListGetFirstResultSet(jdbcTemplate, sql, Object.class);
    }

    public static <T> List<T> queryForListGetFirstResultSet(JdbcTemplate jdbcTemplate, String sql, Class<T> elementType) {
        List<List<T>> lists = queryForListListGetFirstResultSet(jdbcTemplate, sql, elementType);
        return lists.size() > 0 ? lists.get(0) : new ArrayList<>();
    }

}
