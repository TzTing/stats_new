package com.bright.stats.util.tag;

import com.bright.stats.util.ExcelParserPOI;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ForeachTag implements ITag{


	@Override
	public List<Map<String,Object>> parsetCell(String ecx, Map<Object, Object> tags,
			Map<Object, Object> params) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String queryStr(String expr) {
		// TODO Auto-generated method stub
		return null;
	}
	
	 /**
	   * parse the tag
	   * 
	   * @param context data object
	   * @param sheet excel sheet
	   * @param curRow excel row
	   * @param curCell excel cell
	   * @return int[] {skip number, shift number, break flag}
	   */
	  public int[] parseTag(Object collection,String columns, HSSFSheet sheet, int curRow, int curCell){
		  int forstart=curRow;
		  int forend=forstart+1;
		  int shiftNum = forend - forstart;
		  int shift = 0;
		  int old_forend=forend;
		  Iterator iterator = ExcelParserPOI.getIterator(collection);
		  int curExprRow= ExcelParserPOI.getExprRow(sheet, curRow, curCell);
		  if(null!=iterator){
				while(iterator.hasNext()){
					Object obj = iterator.next();
					int slrn = sheet.getLastRowNum();
					if(curExprRow<=forstart){
						
						 sheet.shiftRows(forstart, slrn, shiftNum, true, true); //从下标为forstart的行开始移动,到下标为slrn的行结束移动,shiftNum有多少行需要移动
						 ExcelParserPOI.copyRow(sheet, forstart+1, forstart, shiftNum);
					}
			       
			        shift = ExcelParserPOI.parseRow(obj,columns, sheet, forstart, curCell);//forstart + shiftNum - 1);
			        
			        forstart += shiftNum + shift;
			        forend += shiftNum + shift;
			        
				}
			}
		  return new int[] { ExcelParserPOI.getSkipNum(forstart, forend),
			        ExcelParserPOI.getShiftNum(old_forend, forstart), 1,forstart };
	  }

	public int getExprRow(HSSFSheet sheet, int rownum,int cellnum) {
		return 0;
	}

	public List<Map<Object, Object>> queryExcelTemplateByRow(
			HSSFSheet sheet, int sheetIndex, int rownum) {
		return null;
	}



}
