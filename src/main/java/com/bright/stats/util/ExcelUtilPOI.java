package com.bright.stats.util;

import net.sf.excelutils.ExcelException;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

public class ExcelUtilPOI {
	
	
	public static HSSFWorkbook openWorkbook(ServletContext ctx, String config) throws ExcelException {

		InputStream in = null;
		HSSFWorkbook wb = null;
		try {
			in = ctx.getResourceAsStream(config);
			wb = new HSSFWorkbook(in);
		} catch (Exception e) {
			throw new ExcelException("File" + config + "not found," + e.getMessage());
		} finally {
			try {
				in.close();
			} catch (Exception e) {
			}
		}
		return wb;
	}

	public static void parse(ServletContext ctx,OutputStream out,String file,String ecx,Map<Object,Object> params) throws ExcelException{
		HSSFWorkbook wb=null;
		try {
			wb=openWorkbook(ctx, file);
			parseWorkBook(wb,ecx,params);
			wb.write(out);
		} catch (IOException e) {
			new ExcelException(e.getMessage());
		}
	}
	
	public static void parseWorkBook(HSSFWorkbook wb,String ecx,Map<Object,Object> params){
		int sheetCount=wb.getNumberOfSheets();
		for (int sheetIndex = 0; sheetIndex < sheetCount; sheetIndex++) {
			HSSFSheet sheet=wb.getSheetAt(sheetIndex);
			parseSheet(sheet,sheetIndex,ecx,params);
			sheet.setForceFormulaRecalculation(true);
		}
	}
	
	public static void parseSheet(HSSFSheet sheet,int sheetIndex,String ecx,Map<Object,Object> params){
		ExcelParserPOI.parse(sheet,sheet.getFirstRowNum(),sheet.getLastRowNum(),sheetIndex,ecx,params);
	}
}
