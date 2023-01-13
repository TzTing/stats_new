package com.bright.stats.service;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.dto.ExportExcelQueryCenterDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.query.QueryCenterQuery;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/3 16:43
 * @Description
 */
public interface QueryCenterService {

    /**
     * 获取表头
     * @param typeCode 模式
     * @param tableName 表名
     * @param years 年份
     * @param months 月份
     * @param userDistNo 用户关联地区编号
     * @return
     */
    List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months, String userDistNo);


    /**
     * 分页查询
     * @param queryCenterQuery
     * @return
     */
    PageResult<Map<String, Object>> listTableDataForPage(QueryCenterQuery queryCenterQuery);

    /**
     * 查询分析表列表
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    List<FileList> listAnalysisTables(String typeCode, Integer years, Integer months);

    /**
     *
     * @param exportExcelQueryCenterDTO
     * @param response
     */
    void exportExcel(ExportExcelQueryCenterDTO exportExcelQueryCenterDTO, HttpServletResponse response);
}
