package com.bright.stats.util.tag;

import com.bright.stats.manager.BaseDataTagManager;
import com.bright.stats.manager.ExcelTemplateTagManager;
import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class TransferTag implements ITag{
	
	private final BaseDataTagManager baseDataTagManager;
	private final ExcelTemplateTagManager excelTemplateTagManager;

	
	public List<Map<String,Object>> parsetCell(String ecx,Map<Object,Object> tags,Map<Object,Object> params){
		return baseDataTagManager.queryDataBytemplate(ecx,tags,params);
	}
	
	public String queryStr(String expr){
		return baseDataTagManager.queryStr(expr);
	}
	
	public String modifyTemplate(String file){
		return null;//excelTemplateManager.modifyTemplate(file, excelTemplateInfo, params);
	}

	public int[] parseTag(Object context,String columns, HSSFSheet sheet, int curRow,	int curCell) {
		return null;
	}
	

	public List<Map<Object,Object>> queryExcelTemplateByRow(HSSFSheet sheet,int sheetIndex,int rownum){
	  return excelTemplateTagManager.queryExcelTemplateByRow(sheet, sheetIndex, rownum);
	}
	
	public int getExprRow(HSSFSheet sheet,int rownum,int cellnum){
		  return excelTemplateTagManager.queryExcelTemplateByCell(sheet, rownum, cellnum);
		}

}
