package com.bright.stats.service;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.vo.SqlInfoVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/5 9:37
 * @Description
 */
public interface AnalysisCenterService {

    /**
     * 获取分析方案列表
     * @param years 年份
     * @param typeCode 模式
     * @return 分析方案列表
     */
    List<SqlInfoVO> listAnalysisSchemes(Integer years, String typeCode);

    /**
     * 获取表头列表
     * @param years 年份
     * @param typeCode 模式
     * @param sqlNo 方案编号
     * @return 表头
     */
    List<TableHeader> listTableHeaders(Integer years, String typeCode, String sqlNo);

    /**
     * 分页获取表数据
     * @param analysisCenterQuery
     * @return 表数据
     */
    PageResult<Map<String, Object>> listTableDataForPage(AnalysisCenterQuery analysisCenterQuery);

    void exportExcel(ExportExcelNoTemplateDTO exportExcelNoTemplateDTO, HttpServletResponse response);

}
