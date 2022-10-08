package com.bright.stats.util.tdlz_ht;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.*;
import jxl.write.biff.RowsExceededException;
import org.apache.commons.lang.StringUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.List;

public class ExcelUtil {

	/**
	 * 通过传入的数据生成excel文档
	 * 
	 * @param response
	 *            HttpServletResponse
	 * @param outFileName
	 *            用户保存时的文件名，英文
	 * @param columnName
	 *            列头数组
	 * @param contents
	 *            内容的list，List中的每个元素都是一个数组，长度和列头数组的长度一致
	 * @param sheetName
	 *            页的名称
	 * @return 是否生成文件成功
	 * @throws UndefinedException
	 */
	public static boolean excelExport(HttpServletResponse response,
			String outFileName, String[] columnName, Integer[] widths,
			Integer[] aligns, List contents, String sheetName) {
		String value = null;
		WritableWorkbook wwb = null;
		
		System.out.println("columns:" + columnName.length + ",contents:" + contents);

		OutputStream os = null;
		boolean isSuccess = false;
		try {
			os = response.getOutputStream();
			response.reset();
			outFileName = URLEncoder.encode(outFileName, "UTF-8");
			response.setHeader("Content-disposition", "attachment; filename="
					+ outFileName + ".xls");// 设定输出文件头
			response.setContentType("application/msexcel");// 定义输出类型
			wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			/*
			 * 设置列头jxl.write.Label.Label(int arg0,int arg1,String arg2) arg0
			 * 列数，从0开始 arg1 行数，从0开始 arg2 内容
			 */
			sheet.setRowView(0, 450, false); // 设置高度
			for (int i = 0; i < columnName.length; i++) {
				WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
						WritableFont.BOLD, false,
						jxl.format.UnderlineStyle.NO_UNDERLINE,
						Colour.WHITE);

				WritableCellFormat cellFormat = new WritableCellFormat(font);
				cellFormat.setAlignment(Alignment.CENTRE);
				cellFormat.setBackground(Colour.AQUA);
				cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

				// 设置顶部边框线为实线(默认是黑色－－也可以设置其他颜色)
				cellFormat.setBorder(jxl.format.Border.TOP,
						BorderLineStyle.THIN);
				// 设置右边框线为实线
				cellFormat.setBorder(jxl.format.Border.RIGHT,
						BorderLineStyle.THIN);
				// 设置顶部框线为实线
				cellFormat.setBorder(jxl.format.Border.BOTTOM,
						BorderLineStyle.THIN);

				// cellFormat.setBackground(Colour.DARK_BLUE2);
				Label label = new Label(i, 0+2, columnName[i], cellFormat);
				sheet.addCell(label);
				
				sheet.setColumnView(i, widths[i]);
				font = null;
				cellFormat = null;
				label = null;
			}
			// 写内容
			for (int i = 0; i < contents.size(); i++) {
				Object[] content = (Object[]) contents.get(i);
				for (int j = 0; j < columnName.length; j++) {
					WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
							WritableFont.NO_BOLD, false,
							jxl.format.UnderlineStyle.NO_UNDERLINE,
							Colour.BLACK);
					WritableCellFormat cellFormat = new WritableCellFormat(font);

					if (aligns[j] == 1) {
						cellFormat.setAlignment(Alignment.LEFT);
					} else if (aligns[j] == 3) {
						cellFormat.setAlignment(Alignment.RIGHT);
					} else {
						cellFormat.setAlignment(Alignment.CENTRE);
					}

					// 设置顶部边框线为实线(默认是黑色－－也可以设置其他颜色)
					cellFormat.setBorder(jxl.format.Border.TOP, BorderLineStyle.THIN);
					// 设置右边框线为实线
					cellFormat.setBorder(jxl.format.Border.RIGHT, BorderLineStyle.THIN);
					// 设置顶部框线为实线
					cellFormat.setBorder(jxl.format.Border.BOTTOM, BorderLineStyle.THIN);

					Label label = null;
					cellFormat.setBackground(Colour.WHITE);
					
					if (content[j] == null) {
						value = "";
					} else {
						value = content[j].toString();
					}
					if (StringUtils.isEmpty(value)) {
						value = "";
					}
					label = new Label(j, i + 1 + 2, value, cellFormat);

					sheet.addCell(label);
					font = null;
					cellFormat = null;
					label = null;
					value = null;
				}
				
				content = null;
			}
			
			//first
			WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 20,
					WritableFont.NO_BOLD, false,
					jxl.format.UnderlineStyle.NO_UNDERLINE,
					Colour.BLACK);

			WritableCellFormat cellFormat = new WritableCellFormat(font);
			cellFormat.setAlignment(Alignment.CENTRE);
			cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

			Label label = new Label(0, 0, sheetName, cellFormat);
			sheet.mergeCells(0, 0, columnName.length-1, 0);
			sheet.setRowView(0, 800); //行高
			sheet.addCell(label);
			
			wwb.write();
		} catch (RowsExceededException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (WriteException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				wwb.close();
				os.close();
				isSuccess = true;
			} catch (WriteException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
		return isSuccess;
	}
	
	/**
	 * 通过传入的数据生成excel文档
	 * 
	 * @param response
	 *            HttpServletResponse
	 * @param outFileName
	 *            用户保存时的文件名，英文
	 * @param columnName
	 *            列头数组
	 * @param contents
	 *            内容的list，List中的每个元素都是一个数组，长度和列头数组的长度一致
	 * @param sheetName
	 *            页的名称
	 * @return 是否生成文件成功
	 * @throws UndefinedException
	 */
	public static boolean excelExport(HttpServletResponse response,
			String outFileName, String[] columnName, Integer[] widths,
			Integer[] aligns, Object[] contents, String sheetName) {
		String value = null;
		WritableWorkbook wwb = null;

		OutputStream os = null;
		boolean isSuccess = false;
		try {
			os = response.getOutputStream();
			response.reset();
			outFileName = URLEncoder.encode(outFileName, "UTF-8");
			response.setHeader("Content-disposition", "attachment; filename="
					+ outFileName + ".xls");// 设定输出文件头
			response.setContentType("application/msexcel");// 定义输出类型
			wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			/*
			 * 设置列头jxl.write.Label.Label(int arg0,int arg1,String arg2) arg0
			 * 列数，从0开始 arg1 行数，从0开始 arg2 内容
			 */
			sheet.setRowView(0, 450, false); // 设置高度
			for (int i = 0; i < columnName.length; i++) {
				WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
						WritableFont.BOLD, false,
						jxl.format.UnderlineStyle.NO_UNDERLINE,
						Colour.WHITE);

				WritableCellFormat cellFormat = new WritableCellFormat(font);
				cellFormat.setAlignment(Alignment.CENTRE);
				cellFormat.setBackground(Colour.AQUA);
				cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

				// 设置顶部边框线为实线(默认是黑色－－也可以设置其他颜色)
				cellFormat.setBorder(jxl.format.Border.TOP,
						BorderLineStyle.THIN);
				// 设置右边框线为实线
				cellFormat.setBorder(jxl.format.Border.RIGHT,
						BorderLineStyle.THIN);
				// 设置顶部框线为实线
				cellFormat.setBorder(jxl.format.Border.BOTTOM,
						BorderLineStyle.THIN);

				// cellFormat.setBackground(Colour.DARK_BLUE2);
				Label label = new Label(i, 0, columnName[i], cellFormat);
				sheet.addCell(label);
				
				sheet.setColumnView(i, widths[i]);
			}
			// 写内容
			for (int i = 0; i < contents.length; i++) {
				Object[] content = (Object[]) contents[i];
				for (int j = 0; j < content.length; j++) {
					WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 10,
							WritableFont.NO_BOLD, false,
							jxl.format.UnderlineStyle.NO_UNDERLINE,
							Colour.BLACK);
					WritableCellFormat cellFormat = new WritableCellFormat(font);

					if (aligns[j] == 1) {
						cellFormat.setAlignment(Alignment.LEFT);
					} else if (aligns[j] == 3) {
						cellFormat.setAlignment(Alignment.RIGHT);
					} else {
						cellFormat.setAlignment(Alignment.CENTRE);
					}

					// 设置顶部边框线为实线(默认是黑色－－也可以设置其他颜色)
					cellFormat.setBorder(jxl.format.Border.TOP,
							BorderLineStyle.THIN);
					// 设置右边框线为实线
					cellFormat.setBorder(jxl.format.Border.RIGHT,
							BorderLineStyle.THIN);
					// 设置顶部框线为实线
					cellFormat.setBorder(jxl.format.Border.BOTTOM,
							BorderLineStyle.THIN);

					Label label = null;
					cellFormat.setBackground(Colour.WHITE);
					
					if (content[j] == null) {
						value = "";
					} else {
						value = content[j].toString();
					}
					if (StringUtils.isEmpty(value)) {
						value = "";
					}
					label = new Label(j, i + 1, value, cellFormat);

					sheet.addCell(label);
				}
			}
			wwb.write();
		} catch (RowsExceededException e) {
			System.out.println(e.getMessage());
		} catch (WriteException e) {
			System.out.println(e.getMessage());
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				wwb.close();
				os.close();
				isSuccess = true;
			} catch (WriteException e) {
			} catch (IOException e) {
			}
		}
		return isSuccess;
	}

//	public static List<ImportExcel> excelImport(String fileName, Integer beginRow) {
//		List<ImportExcel> rvalue = new ArrayList<ImportExcel>();
//
//		TimeZone tz = TimeZone.getTimeZone("GMT+08:00");
//		TimeZone.setDefault(tz);
//		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//		ImportExcel importExcel=null;
//		try {
//			Workbook book = Workbook.getWorkbook(new File(fileName));
//			//  获得第一个工作表对象
//            Sheet sheet  =  book.getSheet( 0 );
//            if(sheet==null)
//            	System.out.println("["+fileName+"]Excel的第一个工作表对象为空！");
//
//            int rows = sheet.getRows();
//            int cols = sheet.getColumns();
//            if(rows==0 || cols==0 )
//            	System.out.println("["+fileName+"]Excel的rows:"+rows+"  cols:"+cols+" ！");
//
//            for(int i=beginRow-1;i<rows;i++) {
//            	importExcel = new ImportExcel();
//            	List<Object> dataSub = new ArrayList<Object>();
//
//            	for(int j=0;j<cols;j++) {
//            		Cell cell = sheet.getCell(j, i);
//            		if(cell==null)
//            			System.out.println("["+fileName+"]Excel的j:"+j+"  i:"+i+"cell is null ");
//            		String value = cell.getContents();
//
//            		if (cell.getType() == CellType.DATE) {
//            			DateCell dc = (DateCell) cell;
//                		Date date = dc.getDate();
//
//                		value = sdf.format(date);
//
//                		if(value.length() > " 08:00:00".length() && StringUtils.right(value, " 08:00:00".length()).equals(" 08:00:00")) {
//                			value = StringUtils.left(value, value.length() - " 08:00:00".length());
//                		}
//            		}
//            		dataSub.add(value);
//            	}
//
//            	importExcel.setRow(i+1);
//            	importExcel.setExcelData(dataSub);
//            	rvalue.add(importExcel);
//            }
//
//            book.close();
//            //  得到第一列第一行的单元格
//		} catch (Exception e) {
//			System.out.println("获得第一个工作表数据时出错！"+e.getMessage(),e);
//		}
//
//		return rvalue;
//	}
	
	protected static boolean compareData(String v1, String v2, String opt) {
		if(null==v1 || StringUtils.isEmpty(v1)){
			v1="0";
		}
		
		if(null==v2 || StringUtils.isEmpty(v2)){
			v2="0";
		}
		
		BigDecimal value1=new BigDecimal(v1);
		BigDecimal value2=new BigDecimal(v2);
		if(value1.compareTo(BigDecimal.ZERO)==0 && value2.compareTo(BigDecimal.ZERO)==0){
			return true;
		}
		if(StringUtils.equals(opt, "=")) {
			return value1.compareTo(value2) == 0;
		}
		if(StringUtils.equals(opt, ">")) {
			return value1.compareTo(value2) > 0;
		}
		if(StringUtils.equals(opt, ">=")) {
			return value1.compareTo(value2) >= 0;
		}
		if(StringUtils.equals(opt, "<")) {
			return value1.compareTo(value2) < 0;
		}
		if(StringUtils.equals(opt, "<=")) {
			return value1.compareTo(value2) <= 0;
		}
		if(StringUtils.equals(opt, "<>")) {
			return value1.compareTo(value2) != 0;
		}
		return false;
	}
}
