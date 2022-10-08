package com.bright.stats.util;

import com.bright.stats.pojo.model.HtmlFileItem;
import com.bright.stats.pojo.model.HtmlSqlInfoItem;
import com.bright.stats.pojo.po.primary.FileItem;
import com.bright.stats.pojo.po.primary.SqlInfoItem;
import jxl.CellView;
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
import java.net.URLEncoder;
import java.util.*;

public class ExcelUtil extends com.bright.stats.util.tdlz_ht.ExcelUtil {
	private static int getBeginColumn(List<Integer> list) {
        Collections.sort(list);

		int beginColumn = list.size()>0 ? list.get(list.size()-1)+1 : 1;
		for(int i=0;i<list.size();i++){
			if(i==list.size()-1) break;
			
			if(list.get(i)+1 != list.get(i+1)) {
				beginColumn = list.get(i)+1;
				break;
			}
		};

		beginColumn = list.size()>0 && list.get(0) != 1 ? 1 : beginColumn;
		return beginColumn;
	}
	
	private static WritableCellFormat getWritableCellFormat(WritableFont font, boolean isNumber, String numberFormat, int align) {
		WritableCellFormat rvalue = null;
		
		if(!isNumber) {
			rvalue = new WritableCellFormat(font);
		} else {
			if(StringUtils.isNotEmpty(numberFormat)) {
				NumberFormat nf = new NumberFormat(numberFormat);    //设置数字格式
				rvalue = new WritableCellFormat(nf);
			} else {
				rvalue = new WritableCellFormat();
			}
		}
		
		try {
			rvalue.setAlignment(Alignment.LEFT);
			if(align == 2) rvalue.setAlignment(Alignment.CENTRE);
			if(align == 3) rvalue.setAlignment(Alignment.RIGHT);
			
			//rvalue(Colour.AQUA);//AQUA
			rvalue.setVerticalAlignment(VerticalAlignment.CENTRE);
			rvalue.setWrap(true);
	
			// 设置顶部边框线为实线(默认是黑色－－也可以设置其他颜色)
			rvalue.setBorder(jxl.format.Border.ALL, BorderLineStyle.THIN);
			/*// 设置右边框线为实线
			rvalue.setBorder(jxl.format.Border.RIGHT, BorderLineStyle.THIN);
			// 设置顶部框线为实线
			rvalue.setBorder(jxl.format.Border.BOTTOM, BorderLineStyle.THIN);*/
		} catch (WriteException e) {
			System.out.println(e.getMessage());
		}
		
		return rvalue;
	}
	public static boolean excelExport_s(HttpServletResponse response,
			String outFileName, String titleName, String sheetName, List<List<HtmlSqlInfoItem>> htmlSqlInfoItems, List<SqlInfoItem> sqlInfoItems,
			List<Object[]> contents) {
		String value = null;
		WritableWorkbook wwb = null;
		
		//logger.debug("columns:" + columnName.length + ",contents:" + contents);
		int firstColumnRow = 1;
		
		
		

		//存储位置数组
		List<List<Integer>> arrays = new ArrayList<List<Integer>>();  
		for (int i = 1; i < htmlSqlInfoItems.size(); i++) {
			List<Integer> array = new ArrayList<Integer>();
			arrays.add(array);
		}
		
		//excel表头格式
		WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			firstHeaderFont = new WritableFont(WritableFont.createFont("宋体"), 16,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			dataFont = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.NO_BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);

		WritableCellFormat firstHeaderCellFormat = getWritableCellFormat(firstHeaderFont, false, null, 2),
			headerCellFormat = getWritableCellFormat(font, false, null, 2),
			footerCellFormat = getWritableCellFormat(dataFont, false, null, 1);
		/*try {
			headerCellFormat.setBackground(Colour.TEAL);
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//excel数据格式
		WritableCellFormat stringLeftCellFormat = getWritableCellFormat(dataFont, false, null, 1),
			stringCenterCellFormat = getWritableCellFormat(dataFont, false, null, 2),
			stringRightCellFormat = getWritableCellFormat(dataFont, false, null, 3),
			numberDataCell = getWritableCellFormat(dataFont, true, null, 3),
			number0DataCell = getWritableCellFormat(dataFont, true, "0", 3),
			number2DataCell = getWritableCellFormat(dataFont, true, "0.00", 3),
			number4DataCell = getWritableCellFormat(dataFont, true, "0.0000", 3);
		
		
		

		OutputStream os = null;
		boolean isSuccess = false;
		try {
			os = response.getOutputStream();
			response.reset();
			//outFileName = URLEncoder.encode(outFileName, "UTF-8"); ISO8859_1
			
			//q
//			response.setHeader("Content-disposition", "attachment; filename="
//					+ outFileName + ".xls");// 设定输出文件头
		//	outFileName = URLEncoder.encode(outFileName, "GB2312");
			outFileName=outFileName.replaceFirst(" ", "");
//			outFileName=new String(StringUtils.trimToEmpty(outFileName).getBytes("utf-8"),"ISO8859_1");
			response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode( outFileName +".xls", "utf-8"));
			response.setContentType("application/vnd.ms-excel;charset=gb2312");

//			response.setHeader("Content-disposition", "attachment; filename="
//					 +outFileName+".xls");
			// 设定输出文件头
			
			
			
			wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			
			
			//首行表头
			Label headerLabel = new Label(0, 0, titleName, firstHeaderCellFormat);
			sheet.addCell(headerLabel);
			sheet.mergeCells(0, 0, sqlInfoItems.size()-1, 0);
			sheet.setRowView(0, 35*20);
			
			
			/*
			 * 设置列头jxl.write.Label.Label(int arg0,int arg1,String arg2) arg0
			 * 列数，从0开始 arg1 行数，从0开始 arg2 内容
			 */			
			for (int i = 0; i < htmlSqlInfoItems.size(); i++) {	
				for(int j=0; j< htmlSqlInfoItems.get(i).size(); j++) {
					HtmlSqlInfoItem htmlSqlInfoItem = htmlSqlInfoItems.get(i).get(j);
					if(htmlSqlInfoItem.isNoShow()) continue;
					// cellFormat.setBackground(Colour.DARK_BLUE2);
					//1.得到开始列数   (i==0 ? "固定列" : )
					int beginColumn = i>0 ? getBeginColumn(arrays.get(i-1)) : getBeginColumn(arrays.get(i));
					
					//2.列数放入数组
					for(int m=0;m<htmlSqlInfoItem.getRowspan();m++) {
						for(int n=0;n<htmlSqlInfoItem.getColspan();n++) {
							if(i>0) {
								arrays.get(i-1+m).add(beginColumn + n);
							} else {
								arrays.get(i+m).add(beginColumn + n);
							}
						}
					}
					
					Label label = null;
					String title = htmlSqlInfoItem.getTitle();
					title = StringUtils.replace(title, "<span class=\"txtcenter\">", "");
					title = StringUtils.replace(title, "</span>", "");
					title = StringUtils.replace(title, "<br>", "");
					
					if(i>0) {
						label = new Label(beginColumn-1, i-1+firstColumnRow, title, headerCellFormat);
					} else { //固定列
						label = new Label(beginColumn-1, i+firstColumnRow, title, headerCellFormat);						
					}
					
					if(htmlSqlInfoItem.getColspan()>1 || htmlSqlInfoItem.getRowspan()>1) {
						if(i>0) {
							sheet.mergeCells(beginColumn-1, i-1+firstColumnRow, beginColumn-1+htmlSqlInfoItem.getColspan()-1, i-1+firstColumnRow+htmlSqlInfoItem.getRowspan()-1);
						} else { //固定列
							sheet.mergeCells(beginColumn-1, i+firstColumnRow, beginColumn-1+htmlSqlInfoItem.getColspan()-1, i+firstColumnRow+htmlSqlInfoItem.getRowspan()-1);							
						}
					}
					sheet.addCell(label);

					if(htmlSqlInfoItem.getFLen() != 0) {
						CellView cellView = new CellView();
						cellView.setAutosize(true);
//						cellView.setSize(htmlFileItem.getFLen());
						sheet.setColumnView(beginColumn-1, (int)(htmlSqlInfoItem.getFLen()/6)); //htmlFileItem.getFLen()
					}
					
					
					font = null;
					label = null;
				}
				if(i>0)
				sheet.setRowView(i, 24*20);
			}
			
			firstColumnRow += htmlSqlInfoItems.size()-2;
			
			// 写内容
			for (int i = 0; i < contents.size(); i++) {
				Object[] content = (Object[]) contents.get(i);
				for (int j = 0; j < content.length; j++) {
					Label label = null;
					
					if (content[j] == null) {
						value = "";
					} else {
						value = content[j].toString();
					}
					if (StringUtils.isEmpty(value)) {
						value = "";
					}
					
					if(StringUtils.equalsIgnoreCase(sqlInfoItems.get(j).getFType(), "N")) { //数字格式
						String disFormat = sqlInfoItems.get(j).getDisFormat(); //#.####
						WritableCellFormat cellFormat = numberDataCell;
						if(StringUtils.equalsIgnoreCase(disFormat, "0,")) {
							cellFormat = number0DataCell;
						}
						if(StringUtils.equalsIgnoreCase(disFormat, "0.00,")) {
							cellFormat = number2DataCell;
						}
						if(StringUtils.equalsIgnoreCase(disFormat, "0.0000,")) {
							cellFormat = number4DataCell;
						}
						
						if(StringUtils.isEmpty(value)) value="0";
						jxl.write.Number labelNF = new jxl.write.Number(j, i + 1 + firstColumnRow, Double.valueOf(value), cellFormat); //格式化数值
						sheet.addCell(labelNF);   //在表单中添加格式化的数字
						
						cellFormat = null;
					} else {
						WritableCellFormat stringCellFormat = stringLeftCellFormat;
						if(sqlInfoItems.get(j).getAlign() == 2) {
							stringCellFormat = stringCenterCellFormat;
						} else if(sqlInfoItems.get(j).getAlign() == 3) {
							stringCellFormat = stringRightCellFormat;
						}
					
						String formatter=sqlInfoItems.get(j).getFormatter();
						
						label = new Label(j, i + 1 + firstColumnRow, value, stringCellFormat);

						sheet.addCell(label);
						
						stringCellFormat = null;
					}
					
					

//					CellView cellView = new CellView();
//					cellView.setAutosize(true);
//					sheet.setColumnView(j, fileItems.get(j).getFLen()); //htmlFileItem.getFLen()
					
					font = null;
					label = null;
					value = null;
				}

				sheet.setRowView(i + 1 + firstColumnRow, 20*20);
				content = null;
			}
			

			//行尾
			String footerName = "【贝佳软件】" + DateUtil.getDate(DateUtil.getCurrDate());
			Label footerLabel = new Label(0, contents.size() + firstColumnRow + 1, footerName, footerCellFormat);
			sheet.addCell(footerLabel);
			
			//q
			//sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  fileItems.size()-1, 0);
			sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  sqlInfoItems.size()-1,contents.size() + firstColumnRow + 1);
			sheet.setRowView(contents.size() + firstColumnRow + 1, 15*20);
			
			
			/*//first
			WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 20,
					WritableFont.NO_BOLD, false,
					jxl.format.UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);

			WritableCellFormat cellFormat = new WritableCellFormat(font);
			cellFormat.setAlignment(Alignment.CENTRE);
			cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

			Label label = new Label(0, 0, sheetName, cellFormat);
			sheet.mergeCells(0, 0, columnName.length-1, 0);
			sheet.setRowView(0, 800); //行高
			sheet.addCell(label);*/
			
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
	
	public static boolean excelExport(HttpServletResponse response,
			String outFileName, String titleName, String sheetName, List<List<HtmlFileItem>> htmlFileItems, List<FileItem> fileItems,
			List<Object[]> contents,List<Map<Object,Object>> abnormals) {
		String value = null;
		WritableWorkbook wwb = null;
		
		//logger.debug("columns:" + columnName.length + ",contents:" + contents);
		int firstColumnRow = 1;
		
		
//		List<Map<Object,Object>> coles=new ArrayList<Map<Object,Object>>();
//		Map<Object,Object> col=new HashMap<Object, Object>();
//		col.put("c7", 5);
//		col.put("opt", ">");
//		col.put("color", "red");
//		coles.add(col);

		//存储位置数组
		List<List<Integer>> arrays = new ArrayList<List<Integer>>();  
		for (int i = 1; i < htmlFileItems.size(); i++) {
			List<Integer> array = new ArrayList<Integer>();
			arrays.add(array);
		}
		
		//excel表头格式
		WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			firstHeaderFont = new WritableFont(WritableFont.createFont("宋体"), 16,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			dataFont = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.NO_BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);

		WritableCellFormat firstHeaderCellFormat = getWritableCellFormat(firstHeaderFont, false, null, 2),
			headerCellFormat = getWritableCellFormat(font, false, null, 2),
			footerCellFormat = getWritableCellFormat(dataFont, false, null, 1);
		/*try {
			headerCellFormat.setBackground(Colour.TEAL);
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//excel数据格式
		WritableCellFormat stringLeftCellFormat = getWritableCellFormat(dataFont, false, null, 1),
			stringCenterCellFormat = getWritableCellFormat(dataFont, false, null, 2),
			stringRightCellFormat = getWritableCellFormat(dataFont, false, null, 3),
			numberDataCell = getWritableCellFormat(dataFont, true, null, 3),
			number0DataCell = getWritableCellFormat(dataFont, true, "0", 3),
			number2DataCell = getWritableCellFormat(dataFont, true, "0.00", 3),
			number4DataCell = getWritableCellFormat(dataFont, true, "0.0000", 3);
		
		WritableFont dft = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.NO_BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.RED);
		
		WritableCellFormat	numberCell = getWritableCellFormat(dft, false, null, 3),
			number0Cell = getWritableCellFormat(dft, false, "0", 3),
			number2Cell = getWritableCellFormat(dft, false, "0.00", 3),
			number4Cell = getWritableCellFormat(dft, false, "0.0000", 3);
		
		
		

		OutputStream os = null;
		boolean isSuccess = false;
		try {
			os = response.getOutputStream();
			response.reset();
			//outFileName = URLEncoder.encode(outFileName, "UTF-8"); ISO8859_1
			
			//q
//			response.setHeader("Content-disposition", "attachment; filename="
//					+ outFileName + ".xls");// 设定输出文件头
		//	outFileName = URLEncoder.encode(outFileName, "GB2312");
			outFileName=outFileName.replaceFirst(" ", "");
			outFileName=new String(StringUtils.trimToEmpty(outFileName).getBytes("utf-8"),"ISO8859_1");
			response.setContentType("application/msexcel");// 定义输出类型
			response.setHeader("Content-disposition", "attachment; filename="
			 +outFileName +".xls");
		
//			response.setHeader("Content-disposition", "attachment; filename="
//					 +outFileName+".xls");
			// 设定输出文件头
			
			
			
			wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			
			
			//首行表头
			Label headerLabel = new Label(0, 0, titleName, firstHeaderCellFormat);
			sheet.addCell(headerLabel);
			sheet.mergeCells(0, 0, fileItems.size()-1, 0);
			sheet.setRowView(0, 35*20);
			boolean isflag=false;
			
			/*
			 * 设置列头jxl.write.Label.Label(int arg0,int arg1,String arg2) arg0
			 * 列数，从0开始 arg1 行数，从0开始 arg2 内容
			 */			
			for (int i = 0; i < htmlFileItems.size(); i++) {	
				for(int j=0; j< htmlFileItems.get(i).size(); j++) {
					HtmlFileItem htmlFileItem = htmlFileItems.get(i).get(j);
					if(htmlFileItem.isNoShow()) continue;
					// cellFormat.setBackground(Colour.DARK_BLUE2);
					//1.得到开始列数   (i==0 ? "固定列" : )
					int beginColumn = i>0 ? getBeginColumn(arrays.get(i-1)) : getBeginColumn(arrays.get(i));
					
					//2.列数放入数组
					for(int m=0;m<htmlFileItem.getRowspan();m++) {
						for(int n=0;n<htmlFileItem.getColspan();n++) {
							if(i>0) {
								arrays.get(i-1+m).add(beginColumn + n);
							} else {
								arrays.get(i+m).add(beginColumn + n);
							}
						}
					}
					
					Label label = null;
					String title = htmlFileItem.getTitle();
					title = StringUtils.replace(title, "<span class=\"txtcenter\">", "");
					title = StringUtils.replace(title, "</span>", "");
					title = StringUtils.replace(title, "<br>", "");
					
					if(i>0) {
						label = new Label(beginColumn-1, i-1+firstColumnRow, title, headerCellFormat);
					} else { //固定列
						label = new Label(beginColumn-1, i+firstColumnRow, title, headerCellFormat);						
					}
					
					if(htmlFileItem.getColspan()>1 || htmlFileItem.getRowspan()>1) {
						if(i>0) {
							sheet.mergeCells(beginColumn-1, i-1+firstColumnRow, beginColumn-1+htmlFileItem.getColspan()-1, i-1+firstColumnRow+htmlFileItem.getRowspan()-1);
						} else { //固定列
							sheet.mergeCells(beginColumn-1, i+firstColumnRow, beginColumn-1+htmlFileItem.getColspan()-1, i+firstColumnRow+htmlFileItem.getRowspan()-1);							
						}
					}
					sheet.addCell(label);

					if(htmlFileItem.getFLen() != 0) {
						CellView cellView = new CellView();
						cellView.setAutosize(true);
//						cellView.setSize(htmlFileItem.getFLen());
						sheet.setColumnView(beginColumn-1, (int)(htmlFileItem.getFLen()/6)); //htmlFileItem.getFLen()
					}
					
					
					font = null;
					label = null;
				}
				if(i>0)
				sheet.setRowView(i, 24*20);
			}
			
			firstColumnRow += htmlFileItems.size()-2;
			isflag=false;
			// 写内容
			for (int i = 0; i < contents.size(); i++) {
				Object[] content = (Object[]) contents.get(i);
				for (int j = 0; j < content.length; j++) {
					Label label = null;
					isflag=false;
					if (content[j] == null) {
						value = "";
					} else {
						value = content[j].toString();
					}
					if (StringUtils.isEmpty(value)) {
						value = "";
					}

					
					if(StringUtils.equalsIgnoreCase(fileItems.get(j).getFType(), "N")) { //数字格式
						String disFormat = fileItems.get(j).getDisFormat(); //#.####
						//begin
						if(null!=abnormals && abnormals.size()>0){
							for (int a=0; a<abnormals.size(); a++) {
								if(null!=abnormals.get(a).get(fileItems.get(j).getFieldName().toLowerCase()) && StringUtils.isNotEmpty(abnormals.get(a).get(fileItems.get(j).getFieldName().toLowerCase()).toString())){
									Object s=abnormals.get(a).get(fileItems.get(j).getFieldName().toLowerCase());
									Object opt=abnormals.get(a).get("opt");
									if(compareData(value,s.toString(), opt.toString())){
										isflag=true;
									}
								}
							}
						}
						
//						numberDataCell.setFont(dft);
//						number0DataCell.setFont(dft);
//						number2DataCell.setFont(dft);
//						number4DataCell.setFont(dft);
						////////end
						
						WritableCellFormat cellFormat = numberDataCell;
						if(isflag)cellFormat=numberCell;
						if(StringUtils.equalsIgnoreCase(disFormat, "0,")) {
							cellFormat = number0DataCell;
							if(isflag)cellFormat=number0Cell;
						}
						if(StringUtils.equalsIgnoreCase(disFormat, "0.00,")) {
							cellFormat = number2DataCell;
							if(isflag)cellFormat=number2Cell;
						}
						if(StringUtils.equalsIgnoreCase(disFormat, "0.0000,")) {
							cellFormat = number4DataCell;
							if(isflag)cellFormat=number4Cell;
						}
						if(StringUtils.isEmpty(value)) value="0";
						jxl.write.Number labelNF = new jxl.write.Number(j, i + 1 + firstColumnRow, Double.valueOf(value), cellFormat); //格式化数值
						sheet.addCell(labelNF);   //在表单中添加格式化的数字
						
						cellFormat = null;
					} else {
						WritableCellFormat stringCellFormat = stringLeftCellFormat;
						if(fileItems.get(j).getAlign() == 2) {
							stringCellFormat = stringCenterCellFormat;
						} else if(fileItems.get(j).getAlign() == 3) {
							stringCellFormat = stringRightCellFormat;
						}
					
						String formatter=fileItems.get(j).getFormatter();
						
						label = new Label(j, i + 1 + firstColumnRow, value, stringCellFormat);

						sheet.addCell(label);
						
						stringCellFormat = null;
					}
					
					

//					CellView cellView = new CellView();
//					cellView.setAutosize(true);
//					sheet.setColumnView(j, fileItems.get(j).getFLen()); //htmlFileItem.getFLen()
					
					font = null;
					label = null;
					value = null;
				}

				sheet.setRowView(i + 1 + firstColumnRow, 20*20);
				content = null;
			}
			

			//行尾
			String footerName = "【贝佳软件】" + DateUtil.getDate(DateUtil.getCurrDate());
			Label footerLabel = new Label(0, contents.size() + firstColumnRow + 1, footerName, footerCellFormat);
			sheet.addCell(footerLabel);
			
			//q
			//sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  fileItems.size()-1, 0);
			sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  fileItems.size()-1,contents.size() + firstColumnRow + 1);
			sheet.setRowView(contents.size() + firstColumnRow + 1, 15*20);
			
			
			/*//first
			WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 20,
					WritableFont.NO_BOLD, false,
					jxl.format.UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);

			WritableCellFormat cellFormat = new WritableCellFormat(font);
			cellFormat.setAlignment(Alignment.CENTRE);
			cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

			Label label = new Label(0, 0, sheetName, cellFormat);
			sheet.mergeCells(0, 0, columnName.length-1, 0);
			sheet.setRowView(0, 800); //行高
			sheet.addCell(label);*/
			
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
	
	
	public static boolean excelExportByObj(HttpServletResponse response,
			String outFileName, String titleName, String sheetName, List<List<HtmlFileItem>> htmlFileItems, List<FileItem> fileItems,
			List<Map<Object,Object>> contents) {
		String value = null;
		WritableWorkbook wwb = null;
		
		//logger.debug("columns:" + columnName.length + ",contents:" + contents);
		int firstColumnRow = 1;

		//存储位置数组
		List<List<Integer>> arrays = new ArrayList<List<Integer>>();  
		for (int i = 1; i < htmlFileItems.size(); i++) {
			List<Integer> array = new ArrayList<Integer>();
			arrays.add(array);
		}
		
		//excel表头格式
		WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			firstHeaderFont = new WritableFont(WritableFont.createFont("宋体"), 16,
				WritableFont.BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK),
			dataFont = new WritableFont(WritableFont.createFont("宋体"), 9,
				WritableFont.NO_BOLD, false,
				jxl.format.UnderlineStyle.NO_UNDERLINE,
				Colour.BLACK);

		WritableCellFormat firstHeaderCellFormat = getWritableCellFormat(firstHeaderFont, false, null, 2),
			headerCellFormat = getWritableCellFormat(font, false, null, 2),
			footerCellFormat = getWritableCellFormat(dataFont, false, null, 1);
		/*try {
			headerCellFormat.setBackground(Colour.TEAL);
		} catch (WriteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		
		//excel数据格式
		WritableCellFormat stringLeftCellFormat = getWritableCellFormat(dataFont, false, null, 1),
			stringCenterCellFormat = getWritableCellFormat(dataFont, false, null, 2),
			stringRightCellFormat = getWritableCellFormat(dataFont, false, null, 3),
			numberDataCell = getWritableCellFormat(dataFont, true, null, 3),
			number0DataCell = getWritableCellFormat(dataFont, true, "0", 3),
			number2DataCell = getWritableCellFormat(dataFont, true, "0.00", 3),
			number4DataCell = getWritableCellFormat(dataFont, true, "0.0000", 3);

		OutputStream os = null;
		boolean isSuccess = false;
		try {
			os = response.getOutputStream();
			response.reset();
			//outFileName = URLEncoder.encode(outFileName, "UTF-8"); ISO8859_1
			
			//q
//			response.setHeader("Content-disposition", "attachment; filename="
//					+ outFileName + ".xls");// 设定输出文件头
		//	outFileName = URLEncoder.encode(outFileName, "GB2312");
			outFileName=outFileName.replaceFirst(" ", "");
			outFileName=new String(StringUtils.trimToEmpty(outFileName).getBytes("utf-8"),"ISO8859_1");
			response.setContentType("application/msexcel");// 定义输出类型
			response.setHeader("Content-disposition", "attachment; filename="
			 +outFileName +".xls");
		
//			response.setHeader("Content-disposition", "attachment; filename="
//					 +outFileName+".xls");
			// 设定输出文件头
			
			wwb = Workbook.createWorkbook(os);
			WritableSheet sheet = wwb.createSheet(sheetName, 0);
			
			
			//首行表头
			Label headerLabel = new Label(0, 0, titleName, firstHeaderCellFormat);
			sheet.addCell(headerLabel);
			sheet.mergeCells(0, 0, fileItems.size()-1, 0);
			sheet.setRowView(0, 35*20);
			
			
			/*
			 * 设置列头jxl.write.Label.Label(int arg0,int arg1,String arg2) arg0
			 * 列数，从0开始 arg1 行数，从0开始 arg2 内容
			 */			
			for (int i = 0; i < htmlFileItems.size(); i++) {	
				for(int j=0; j< htmlFileItems.get(i).size(); j++) {
					HtmlFileItem htmlFileItem = htmlFileItems.get(i).get(j);
					if(htmlFileItem.isNoShow()) continue;
					// cellFormat.setBackground(Colour.DARK_BLUE2);
					//1.得到开始列数   (i==0 ? "固定列" : )
					int beginColumn = i>0 ? getBeginColumn(arrays.get(i-1)) : getBeginColumn(arrays.get(i));
					
					//2.列数放入数组
					for(int m=0;m<htmlFileItem.getRowspan();m++) {
						for(int n=0;n<htmlFileItem.getColspan();n++) {
							if(i>0) {
								arrays.get(i-1+m).add(beginColumn + n);
							} else {
								arrays.get(i+m).add(beginColumn + n);
							}
						}
					}
					
					Label label = null;
					String title = htmlFileItem.getTitle();
					title = StringUtils.replace(title, "<span class=\"txtcenter\">", "");
					title = StringUtils.replace(title, "</span>", "");
					title = StringUtils.replace(title, "<br>", "");
					
					if(i>0) {
						label = new Label(beginColumn-1, i-1+firstColumnRow, title, headerCellFormat);
					} else { //固定列
						label = new Label(beginColumn-1, i+firstColumnRow, title, headerCellFormat);						
					}
					
					if(htmlFileItem.getColspan()>1 || htmlFileItem.getRowspan()>1) {
						if(i>0) {
							sheet.mergeCells(beginColumn-1, i-1+firstColumnRow, beginColumn-1+htmlFileItem.getColspan()-1, i-1+firstColumnRow+htmlFileItem.getRowspan()-1);
						} else { //固定列
							sheet.mergeCells(beginColumn-1, i+firstColumnRow, beginColumn-1+htmlFileItem.getColspan()-1, i+firstColumnRow+htmlFileItem.getRowspan()-1);							
						}
					}
					sheet.addCell(label);

					if(htmlFileItem.getFLen() != 0) {
						CellView cellView = new CellView();
						cellView.setAutosize(true);
//						cellView.setSize(htmlFileItem.getFLen());
						sheet.setColumnView(beginColumn-1, (int)(htmlFileItem.getFLen()/6)); //htmlFileItem.getFLen()
					}
					
					
					font = null;
					label = null;
				}
				if(i>0)
				sheet.setRowView(i, 24*20);
			}
			
			firstColumnRow += htmlFileItems.size()-2;
			
			// 写内容
			
			
			
			for (int i = 0; i < contents.size(); i++) {
				Map<Object,Object> map=contents.get(i);
				Iterator<Object> it= map.keySet().iterator();
//				while (it.hasNext()) {
//					Object key=it.next();
				for (int j = 0; j < fileItems.size(); j++) {
					Label label = null;
					if(map.containsKey(fileItems.get(j).getFieldName())){
						value=(null==map.get(fileItems.get(j).getFieldName()))?"":map.get(fileItems.get(j).getFieldName()).toString();
						if(StringUtils.equalsIgnoreCase(fileItems.get(j).getFType(), "N")) { //数字格式
							String disFormat = fileItems.get(j).getDisFormat(); //#.####
							WritableCellFormat cellFormat = numberDataCell;
							if(StringUtils.equalsIgnoreCase(disFormat, "0,")) {
								cellFormat = number0DataCell;
							}
							if(StringUtils.equalsIgnoreCase(disFormat, "0.00,")) {
								cellFormat = number2DataCell;
							}
							if(StringUtils.equalsIgnoreCase(disFormat, "0.0000,")) {
								cellFormat = number4DataCell;
							}
							
							if(StringUtils.isEmpty(value)) value="0";
							jxl.write.Number labelNF = new jxl.write.Number(j, i + 1 + firstColumnRow, Double.valueOf(value), cellFormat); //格式化数值
							sheet.addCell(labelNF);   //在表单中添加格式化的数字
							
							cellFormat = null;
						}else {
							WritableCellFormat stringCellFormat = stringLeftCellFormat;
							if(fileItems.get(j).getAlign() == 2) {
								stringCellFormat = stringCenterCellFormat;
							} else if(fileItems.get(j).getAlign() == 3) {
								stringCellFormat = stringRightCellFormat;
							}
						
							String formatter=fileItems.get(j).getFormatter();
							
							label = new Label(j, i + 1 + firstColumnRow, value, stringCellFormat);

							sheet.addCell(label);
							
							stringCellFormat = null;
						}
					
						
					}
					font = null;
					label = null;
					value = null;
				}
				sheet.setRowView(i + 1 + firstColumnRow, 20*20);
				//content = null;
				//}
				
			//	Object[] content = (Object[]) contents.get(i);
//				for (int j = 0; j < content.length; j++) {
//					Label label = null;
//					
//					if (content[j] == null) {
//						value = "";
//					} else {
//						value = content[j].toString();
//					}
//					if (StringUtils.isEmpty(value)) {
//						value = "";
//					}
//					
//					if(StringUtils.equalsIgnoreCase(fileItems.get(j).getfType(), "N")) { //数字格式
//						String disFormat = fileItems.get(j).getDisFormat(); //#.####
//						WritableCellFormat cellFormat = numberDataCell;
//						if(StringUtils.equalsIgnoreCase(disFormat, "0,")) {
//							cellFormat = number0DataCell;
//						}
//						if(StringUtils.equalsIgnoreCase(disFormat, "0.00,")) {
//							cellFormat = number2DataCell;
//						}
//						if(StringUtils.equalsIgnoreCase(disFormat, "0.0000,")) {
//							cellFormat = number4DataCell;
//						}
//						
//						if(StringUtils.isEmpty(value)) value="0";
//						jxl.write.Number labelNF = new jxl.write.Number(j, i + 1 + firstColumnRow, Double.valueOf(value), cellFormat); //格式化数值
//						sheet.addCell(labelNF);   //在表单中添加格式化的数字
//						
//						cellFormat = null;
//					} else {
//						WritableCellFormat stringCellFormat = stringLeftCellFormat;
//						if(fileItems.get(j).getAlignValue() == 2) {
//							stringCellFormat = stringCenterCellFormat;
//						} else if(fileItems.get(j).getAlignValue() == 3) {
//							stringCellFormat = stringRightCellFormat;
//						}
//					
//						String formatter=fileItems.get(j).getFormatter();
//						
//						label = new Label(j, i + 1 + firstColumnRow, value, stringCellFormat);
//
//						sheet.addCell(label);
//						
//						stringCellFormat = null;
//					}
//					
//					
//
////					CellView cellView = new CellView();
////					cellView.setAutosize(true);
////					sheet.setColumnView(j, fileItems.get(j).getFLen()); //htmlFileItem.getFLen()
//					
//					font = null;
//					label = null;
//					value = null;
//				}

//				sheet.setRowView(i + 1 + firstColumnRow, 20*20);
//				content = null;
			}
			

			//行尾
			String footerName = "【贝佳软件】" + DateUtil.getDate(DateUtil.getCurrDate());
			Label footerLabel = new Label(0, contents.size() + firstColumnRow + 1, footerName, footerCellFormat);
			sheet.addCell(footerLabel);
			
			//q
			//sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  fileItems.size()-1, 0);
			sheet.mergeCells(0, contents.size() + firstColumnRow + 1,  fileItems.size()-1,contents.size() + firstColumnRow + 1);
			sheet.setRowView(contents.size() + firstColumnRow + 1, 15*20);
			
			
			/*//first
			WritableFont font = new WritableFont(WritableFont.createFont("宋体"), 20,
					WritableFont.NO_BOLD, false,
					jxl.format.UnderlineStyle.NO_UNDERLINE,
					jxl.format.Colour.BLACK);

			WritableCellFormat cellFormat = new WritableCellFormat(font);
			cellFormat.setAlignment(Alignment.CENTRE);
			cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);

			Label label = new Label(0, 0, sheetName, cellFormat);
			sheet.mergeCells(0, 0, columnName.length-1, 0);
			sheet.setRowView(0, 800); //行高
			sheet.addCell(label);*/
			
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

}
