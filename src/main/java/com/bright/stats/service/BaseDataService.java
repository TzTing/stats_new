package com.bright.stats.service;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.dto.*;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.BaseDataQuery;
import com.bright.stats.pojo.query.ExistDataQuery;
import com.bright.stats.pojo.query.UploadBaseQuery;
import com.bright.stats.pojo.vo.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 15:14
 * @Description
 */
public interface BaseDataService {

    /**
     * 获取基础数据表内公式
     * @param typeCode 模式
     * @param tableName 模式
     * @param years 年份
     * @param months 月份
     * @param userDistNo 用户关联地区编号
     * @return 表内公式
     */
    List<RuleInnerVO> listRuleInners(String typeCode, String tableName, Integer years, Integer months, String userDistNo);

    /**
     * 获取表头列表
     * @param typeCode 模式
     * @param tableName 表名称
     * @param years 年份
     * @param months 月份
     * @param userDistNo 用户关联地区编号
     * @return 表头
     */
    List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months, String userDistNo);

    /**
     * 分页获取表数据
     * @param baseDataQuery
     * @return 表数据
     */
    PageResult<Map<String, Object>> listTableDataForPage(BaseDataQuery baseDataQuery);

    /**
     * 保存数据
     * @param tableDataDTO 参数
     */
    void saveTableData(TableDataDTO tableDataDTO);

    /**
     * 汇总
     * @param summaryDTO 汇总参数
     * @return 汇总结果
     */
    SummaryVO summary(SummaryDTO summaryDTO);

    /**
     * 汇总
     * @param summaryDTO 汇总参数
     * @return 汇总结果
     */
    SummaryVO summaryCodingRun(SummaryDTO summaryDTO);

    /**
     * 稽核
     * @param checkDTO 稽核参数
     * @return 稽核结果
     */
    List<CheckVO> check(CheckDTO checkDTO);

    /**
     * 分页获取上报单位数据
     * @param uploadBaseQuery 参数
     * @return 上报单位数据
     */
    PageResult<UploadBase> listUploadBaseForPage(UploadBaseQuery uploadBaseQuery);

    /**
     * 上报或者退回数据
     * @param dpName 待办事项_上报/待办事项_退回上报
     * @param keyword id连接地区编号 如：439605_010351001
     * @param user 当前用户
     * @return 是否上报或者退回数据相关的描述
     */
    List<InteractiveVO> reportOrWithdraw(String dpName, String keyword, User user);

    /**
     * 获取excel模板列表
     * @param years 年份
     * @param typeCode 模式/表名称
     * @param username 用户名称
     * @return excel模板列表
     */
    List<ExcelTemplateVO> listExcelTemplates(Integer years, String typeCode, String username);

    /**
     * 获取excel模板列表
     * @param years 年份
     * @param typeCode 模式/表名称
     * @param username 用户名称
     * @return excel模板列表
     */
    List<ExcelTemplateVO> listExcelTemplates(Integer years, String typeCode, String username, String userDistNo);

    /**
     * 按模板导出Excel
     * @param exportExcelDTO 导出Excel参数
     * @return 导出Excel值
     */
    ExportExcelVO exportExcelByTemplate(ExportExcelDTO exportExcelDTO);

    /**
     * 按模板和标签导出Excel
     * @param exportExcelTagDTO
     * @param request
     * @param response
     */
    void exportExcelByTemplateAndTag(ExportExcelTagDTO exportExcelTagDTO, HttpServletRequest request, HttpServletResponse response);

    Map<String, Object> importExcelByTemplate(TableType tableType, Integer excelTemplateId, String rootPath, String filePath, String distNo, Integer years, Integer months);

    Map<String, Object> listLxsAndLxNames(String typeCode, String tableName, String distNo, Integer years, Integer months);

    Map<String, Object> listExistData(ExistDataQuery existDataQuery);

    Map<String, Object> getPreviousYearData(String typeCode, String tableName, Integer years, Integer months, String paramJson);

    /**
     * 获取基本表列表
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    List<FileList> listBaseTables(String typeCode, Integer years, Integer months);

    /**
     * 获取地区所有级别
      * @return
     */
    List<Integer> getDistAllGrade();

    /**
     * 初始化上报数据
     * @param initUploadDataDTO
     * @return
     */
    Boolean initUploadData(InitUploadDataDTO initUploadDataDTO);


    /**
     * 根据主题校验正在运行的操作
     * @param mqMessagesDTO
     * @return
     */
    Boolean checkRunning(MqMessagesDTO mqMessagesDTO);

    /**
     * 根据地区编号检查当前地区上下级是否存在运行的任务
     * @param mqMessagesDTO
     * @return
     */
    Boolean checkRunningByDist(MqMessagesDTO mqMessagesDTO);


    /**
     *
     * @param mqMessagesDTO
     */
    void checkRuningAll(MqMessagesDTO mqMessagesDTO);

    /**
     * 根据条件获取上报情况
     * @param distNo
     * @param years
     * @param months
     * @param tableType
     * @return true:已上报  false:未上报
     */
    Boolean getReportSituation(String distNo, Integer years, Integer months, String tableType);
}
