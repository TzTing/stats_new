package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.stats.manager.AnalysisCenterManager;
import com.bright.stats.manager.SqlInfoManager;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.model.HtmlSqlInfoItem;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.SqlInfo;
import com.bright.stats.pojo.po.primary.SqlInfoItem;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.vo.SqlInfoVO;
import com.bright.stats.service.AnalysisCenterService;
import com.bright.stats.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/5 9:37
 * @Description
 */
@Service
@RequiredArgsConstructor
public class AnalysisCenterServiceImpl implements AnalysisCenterService {

    private final AnalysisCenterManager analysisCenterManager;
    private final SqlInfoManager sqlInfoManager;

    @Override
    public List<SqlInfoVO> listAnalysisSchemes(Integer years, String typeCode) {
        List<SqlInfoVO> sqlInfoVOS = analysisCenterManager.listAnalysisSchemes(years, typeCode);
        return sqlInfoVOS;
    }

    @Override
    public List<TableHeader> listTableHeaders(Integer years, String typeCode, String sqlNo) {
        List<TableHeader> tableHeaders = analysisCenterManager.listTableHeaders(years, typeCode, sqlNo);
        return tableHeaders;
    }

    @Override
    public PageResult<Map<String, Object>> listTableDataForPage(AnalysisCenterQuery analysisCenterQuery) {
        PageResult<Map<String, Object>> mapPageResult = analysisCenterManager.listTableData(analysisCenterQuery, true, PageResult.class);
        return mapPageResult;
    }

    @Override
    public void exportExcel(ExportExcelNoTemplateDTO exportExcelNoTemplateDTO, HttpServletResponse response) {
        Integer years = exportExcelNoTemplateDTO.getYears();
        Integer months = exportExcelNoTemplateDTO.getMonths();
        String distNo = exportExcelNoTemplateDTO.getDistNo();
        Integer grade = exportExcelNoTemplateDTO.getGrade();
        Boolean isGrade = exportExcelNoTemplateDTO.getIsGrade();
        String typeCode = exportExcelNoTemplateDTO.getTypeCode();
        String sqlNo = exportExcelNoTemplateDTO.getSqlNo();


        SqlInfo sqlInfo = sqlInfoManager.getSqlInfo(years, typeCode, sqlNo);
        String sheetName = sqlInfo.getModalName();
        String titleName = sqlInfo.getModalName();
        String outFileName = years + "年" + (months != 0 ? months + "月" : "") + sheetName;
        List<List<HtmlSqlInfoItem>> htmlSqlInfoItems = sqlInfo.getHtmlSqlInfoItems();
        List<SqlInfoItem> sqlInfoItems = sqlInfo.getSqlInfoItems();

        AnalysisCenterQuery analysisCenterQuery = new AnalysisCenterQuery();
        analysisCenterQuery.setYears(years);
        analysisCenterQuery.setMonths(months);
        analysisCenterQuery.setDistNo(distNo);
        analysisCenterQuery.setSqlNo(sqlNo);
        analysisCenterQuery.setGrade(grade);
        analysisCenterQuery.setIsGrade(isGrade);
        analysisCenterQuery.setTypeCode(typeCode);


        List<Map<String, Object>> list = analysisCenterManager.listTableData(analysisCenterQuery, false, List.class);
        List<Object[]> listArray = new ArrayList<>();
        int i = 0;
        if (null != list && list.size() > 0) {
            for (Map<String, Object> dm : list) {
                Object[] array = new Object[sqlInfoItems.size()];
                i = 0;
                for (SqlInfoItem sm : sqlInfoItems) {
                    if (dm.containsKey(sm.getFieldName().toLowerCase())) {
                        array[i] = dm.get(sm.getFieldName().toLowerCase());
                    }
                    i++;
                }
                listArray.add(array);
            }
        }

        ExcelUtil.excelExport_s(response, outFileName, titleName, sheetName, htmlSqlInfoItems, sqlInfoItems, listArray);
    }
}
