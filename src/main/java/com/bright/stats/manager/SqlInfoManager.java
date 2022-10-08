package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.SqlInfo;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/4 14:09
 * @Description
 */
public interface SqlInfoManager {

    /**
     * 获取SqlInfo
     * @param years 年份
     * @param typeCode 模式
     * @param sqlNo SqlInfo编号
     * @return
     */
    SqlInfo getSqlInfo(Integer years, String typeCode, String sqlNo);

    /**
     * 获取SqlInfo集合
     * @param years 年份
     * @param typeCode 模式
     * @return
     */
    List<SqlInfo> listSqlInfos(Integer years, String typeCode);
}
