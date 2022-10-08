package com.bright.stats.manager;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 16:23
 * @Description
 */
public interface ExcelTemplateTagManager {
    List<Map<Object, Object>> queryExcelTemplateByRow(HSSFSheet sheet, int sheetIndex, int rownum);

    int queryExcelTemplateByCell(HSSFSheet sheet, int rownum, int cellnum);

}
