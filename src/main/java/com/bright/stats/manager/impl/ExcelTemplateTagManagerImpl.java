package com.bright.stats.manager.impl;

import com.bright.stats.manager.ExcelTemplateTagManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 16:23
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExcelTemplateTagManagerImpl implements ExcelTemplateTagManager {

    @Override
    public List<Map<Object, Object>> queryExcelTemplateByRow(HSSFSheet sheet, int sheetIndex, int rownum) {
        //int rownum = sheet.getFirstRowNum();
        int cellNum=0;
        boolean isList=false;
        String bfuhao="${";
        String efuhao="}";
        List<Map<Object,Object>> tags=new ArrayList<Map<Object,Object>>();
        try {
            //for (; rownum <= sheet.getLastRowNum(); rownum++) {
            HSSFRow rows=sheet.getRow(rownum);
            if(rows==null){
                return tags;
            }

            for (cellNum =rows.getFirstCellNum(); cellNum < rows.getLastCellNum(); cellNum++) {
                HSSFCell cell=rows.getCell(cellNum);
                if(cell==null)
                    continue;
                int ii=cell.getCellType();
                if(cell.getCellType()!=HSSFCell.CELL_TYPE_STRING){
                    continue;
                }
                String cellValue= StringUtils.trimToEmpty(cell.getStringCellValue());
                if(cellValue==null || StringUtils.isEmpty(cellValue)){
                    continue;
                }
                Map<Object,Object> tagMap=new HashMap<Object, Object>();
                if(!(cellValue.startsWith("$") || cellValue.startsWith("[") || (cellValue.indexOf("[")>0))){
                    continue;
                }
                int keyTag=cellValue.indexOf("$");
                if(keyTag<0){
                    isList=false;
                    bfuhao="[";
                    efuhao="]";
                    keyTag=cellValue.indexOf("[");
                    if(keyTag<0){
                        continue;
                    }
                }else{
                    isList=true;
                    bfuhao="${";
                    efuhao="}";
                }

                if(keyTag>=cellValue.length()-1){
                    continue;
                }
                int indexValued = cellValue.indexOf(bfuhao);
                int indexValued2 = cellValue.lastIndexOf(efuhao);
                if ((indexValued == 0) && (indexValued2 > 0)) {
                    cellValue = cellValue.substring(indexValued + bfuhao.length(), indexValued2);
                }

                if(indexValued>0 && indexValued2>0){
                    cellValue = cellValue.substring(indexValued + bfuhao.length(), indexValued2);
                }

                Map<Object,Object> map=new HashMap<Object, Object>();//(Map<Object,Object>)obj;
                map.put("distposition", new int[]{rownum,cellNum});
                map.put("expr", cellValue);
                map.put("islist", isList);
                tags.add(map);
            }
            //}
        } catch (Exception e) {
            log.error("sheet:"+sheetIndex+",rownum:"+rownum+",cell:"+cellNum);
            e.printStackTrace();
        }

        return tags;
    }

    @Override
    public int queryExcelTemplateByCell(HSSFSheet sheet, int rownum, int cellnum) {
        int rvalue=-1;
        boolean isList=false;
        String bfuhao="${";
        String efuhao="}";
        List<Map<Object,Object>> tags=new ArrayList<Map<Object,Object>>();
        try {
            for (; rownum <= sheet.getLastRowNum(); rownum++) {
                HSSFRow rows=sheet.getRow(rownum);
                if(rows==null){
                    return rvalue;
                }
                //for (cellNum =rows.getFirstCellNum(); cellNum < rows.getLastCellNum(); cellNum++) {
                HSSFCell cell=rows.getCell(cellnum);
                if(cell==null)
                    continue;

                String cellValue=StringUtils.trimToEmpty(cell.getStringCellValue());
                if(cellValue==null || StringUtils.isEmpty(cellValue)){
                    continue;
                }
                Map<Object,Object> tagMap=new HashMap<Object, Object>();
                if(!(cellValue.startsWith("$") || cellValue.startsWith("["))){
                    continue;
                }
                int keyTag=cellValue.indexOf("$");
                if(keyTag<0){
                    isList=false;
                    bfuhao="[";
                    efuhao="]";
                    keyTag=cellValue.indexOf("[");
                    if(keyTag<0){
                        continue;
                    }
                }else{
                    isList=true;
                    bfuhao="${";
                    efuhao="}";
                }

                if(keyTag>=cellValue.length()-1){
                    continue;
                }
                int indexValued = cellValue.indexOf(bfuhao);
                int indexValued2 = cellValue.lastIndexOf(efuhao);
                if ((indexValued == 0) && (indexValued2 > 0)) {
                    cellValue = cellValue.substring(indexValued + bfuhao.length(), indexValued2);
                }

//					Map<Object,Object> map=new HashMap<Object, Object>();//(Map<Object,Object>)obj;
//					map.put("distposition", new int[]{rownum,cellnum});
//					map.put("expr", cellValue);
//					map.put("islist", isList);
//					tags.add(map);
                rvalue=rownum;
                break;
            }
            //}
        } catch (Exception e) {
            log.error("sheet:"+sheet.getSheetName()+",rownum:"+rownum+",cell:"+cellnum);
            e.printStackTrace();
        }

        return rvalue;
    }
}
