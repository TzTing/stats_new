package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.FileList;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/30 16:27
 * @Description
 */
public interface FileListManager {

    /**
     * 获取单个FileList
     * @param typeCode 模式
     * @param tableType 表类型 基本表/分析表
     * @param tableName 表名称
     * @param years 年份
     * @param months 月份
     * @param userDistNo 用户关联地区编号
     * @return 单个FileList
     */
    FileList getFileList(String typeCode, String tableType, String tableName, Integer years, Integer months, String userDistNo);

    /**
     * 获取多个FileList
     * @param typeCode 模式
     * @param tableType 表类型 基本表/分析表
     * @param years 年份
     * @param months 月份
     * @return 多个FileList
     */
    List<FileList> listFileListsByCache(String typeCode, String tableType, Integer years, Integer months);

    /**
     * 获取多个FileList
     * @param typeCode 模式
     * @param tableType 表类型 基本表/分析表
     * @param years 年份
     * @param months 月份
     * @return 多个FileList
     */
    List<FileList> listFileLists(String typeCode, String tableType, Integer years, Integer months, String userDistNo);
}
