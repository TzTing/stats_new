package com.bright.stats.util.tag;

import org.apache.poi.hssf.usermodel.HSSFSheet;

import java.util.List;
import java.util.Map;
public interface ITag {
	
	public List<Map<String,Object>> parsetCell(String ecx,Map<Object,Object> tags,Map<Object,Object> params);
	public String queryStr(String expr);
		
	 /**
	   * parse the tag
	   * 
	   * @param context data object
	   * @param sheet excel sheet
	   * @param curRow excel row
	   * @param curCell excel cell
	   * @return int[] {skip number, shift number, break flag}
	   */
	  public int[] parseTag(Object context,String columns, HSSFSheet sheet, int curRow, int curCell);

	  public  List<Map<Object,Object>> queryExcelTemplateByRow(HSSFSheet sheet,int sheetIndex,int rownum);
	  public  int getExprRow(HSSFSheet sheet,int rownum,int cellnum);
}
