package com.bright.stats.util;

import com.bright.stats.util.tag.ITag;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.*;

public class ExcelParserPOI {
    public static String tagPackages;
    private static Map tagMap = new HashMap();

    static {
        tagPackages = ITag.class.getPackage().getName();
    }

    public static void parse(HSSFSheet sheet, int fromRow, int toRow, int sheetIndex, String ecx, Map<Object, Object> params) {
        int rowNum = fromRow;
        int[] shift = new int[]{0, 0, 0, 0}; // {SkipNum, ShiftNum, break flag}

        ITag transfer = getTagClass("transfer");
        while (rowNum <= toRow) {
            rowNum += shift[1] + shift[0];
            toRow += shift[1];
            if (rowNum > toRow)
                break;
            HSSFRow row = sheet.getRow(rowNum);
            if (null == row) {
                rowNum++;
                continue;
            }

            List<Map<Object, Object>> tags = transfer.queryExcelTemplateByRow(sheet, sheetIndex, rowNum);
            Iterator itags = getIterator(tags);
            if (null == itags) return;
            while (itags.hasNext()) {
                Map<Object, Object> mapTag = (Map<Object, Object>) itags.next();
                if (null == mapTag || mapTag.size() == 0) continue;
                int[] distposition = (int[]) mapTag.get("distposition");
                int rownum = distposition[0];
                int cellnum = distposition[1];
                row = sheet.getRow(rownum);
                if (null == row) continue;
                HSSFCell cell = getCell(row, cellnum);
                if (null == cell) continue;
                //info 读取数据
                Object context = transfer.parsetCell(ecx, mapTag, params);
                boolean islist = (Boolean) mapTag.get("islist");
                String expr = (String) mapTag.get("expr");
                String columns = transfer.queryStr(expr);
                if (islist) {
                    ITag tag1 = getTagClass("foreach");
                    shift = tag1.parseTag(context, columns, sheet, rownum, cellnum);
                } else {
                    Iterator its = getIterator(context);
                    if (null != its) {
                        Object obj = its.next();
                        parseRow(obj, columns, sheet, rownum, cellnum);
                        //parseCell(obj,columns,sheet,row,cell);
                    }

                }
            }
            int removeRow = shift[3];
            if (removeRow > 0) {
                int slrn = sheet.getLastRowNum();
                sheet.removeRow(getRow(removeRow, sheet));
                sheet.shiftRows(removeRow + 1, slrn + 1, -1, true, true);
                shift[3] = 0;
            }
            rowNum++;
        }//while

    }

    /**
     * @param collection
     * @param columns
     * @param sheet
     * @param curRow     当前要插入数据的行
     * @param curCell    当前要插入数据的列
     * @author q
     * @Description: TODO
     * @date 2018-8-2下午07:40:50
     */
    public static int parseRow(Object collection, String columns, HSSFSheet sheet, int curRow, int curCell) {
        HSSFRow row = sheet.getRow(curRow);
        if (null == row) return 0;
        Map<Object, Object> bean = (Map<Object, Object>) collection;
        String keyname = "";
        Object value = null;
        StringTokenizer st = new StringTokenizer(columns, ",");
        int colnum = curCell;
        while (st.hasMoreTokens()) {
            keyname = st.nextToken();
            int a = row.getLastCellNum();
            //if(colnum>row.getLastCellNum())break;
            HSSFCell cell = getCell(row, colnum);
            value = bean.get(keyname);
            value = parseStr(value, cell.getStringCellValue(), false);
            parseCell(value, columns, sheet, row, cell);
            colnum++;
        }
        return 0;
    }

    public static void parseCell(Object value, String columns, HSSFSheet sheet, HSSFRow row, HSSFCell cell) {
        int shiftCount = 0;
        //StringTokenizer st = new StringTokenizer(columns, ",");
        boolean bJustExpr = true;
        boolean bMerge = true;
        //for (int i = 0; i < datas.size(); i++) {
        //Map<Object,Object> data=datas.get(i);
        //while(st.hasMoreTokens()){
        //String col=st.nextToken();
        //value=data.get(col);
        // replace the cell
        int a = cell.getCellType();

        if (null != value) {
            String n = value.getClass().getName();
            if (bJustExpr && "java.lang.Integer".equals(value.getClass().getName())) {
                cell.setCellValue(Double.parseDouble(value.toString()));
            } else if (bJustExpr && "java.lang.Double".equals(value.getClass().getName())) {
                cell.setCellValue(((Double) value).doubleValue());
            } else if (bJustExpr && "java.util.Date".equals(value.getClass().getName())) {
                cell.setCellValue((Date) value);
            } else if (bJustExpr && "java.lang.Boolean".equals(value.getClass().getName())) {
                cell.setCellValue(((Boolean) value).booleanValue());
            } else if (bJustExpr && "java.math.BigDecimal".equals(value.getClass().getName())) {
                cell.setCellValue(((BigDecimal) value).doubleValue());
            } else {
                cell.setCellValue(value.toString());
            }
        } else {
            switch (cell.getCellType()) {
                case HSSFCell.CELL_TYPE_NUMERIC:
                    cell.setCellValue(0);
                    break;
                default:
                    cell.setCellValue("");
                    break;
            }
        }
        //合并单元格
        // merge the cell that has a "!" character at the expression
//			    if (row.getRowNum() - 1 >= sheet.getFirstRowNum() && bMerge) {
//			      HSSFRow lastRow = WorkbookUtils.getRow(row.getRowNum() - 1, sheet);
//			      HSSFCell lastCell = WorkbookUtils.getCell(lastRow, cell.getColumnIndex());
//			      boolean canMerge = false;
//			      if (lastCell.getCellType() == cell.getCellType()) {
//			        switch (cell.getCellType()) {
//			        case HSSFCell.CELL_TYPE_STRING:
//			          canMerge = lastCell.getStringCellValue().equals(cell.getStringCellValue());
//			          break;
//			        case HSSFCell.CELL_TYPE_BOOLEAN:
//			          canMerge = lastCell.getBooleanCellValue() == cell.getBooleanCellValue();
//			          break;
//			        case HSSFCell.CELL_TYPE_NUMERIC:
//			          canMerge = lastCell.getNumericCellValue() == cell.getNumericCellValue();
//			          break;
//			        }
//			      }
//			      if (canMerge) {
//			    	  sheet.addMergedRegion(new Region(ref))
//			        sheet.addMergedRegion(new Region(lastRow.getRowNum(), lastCell.getCellNum(), row.getRowNum(), cell.getCellNum()));
//			      }
//			    }
        //}
//}

    }

    /**
     * parse complex expression ${${}}aaa${}
     *
     * @param context
     * @param str
     * @param quot    string needs quotation or not
     * @return value of the str
     */
    public static Object parseStr(Object context, String str, boolean quot) {

        str = str.trim();
        int exprCount = 0;
        int valueFrom = -1;
        int valueTo = -1;
        int valueCount = 0;
        int pos = 0;
        Object value = context;
        String delim = "[";
        String delim2 = "]";
        int indexValued = str.indexOf(delim);
        int indexValued2 = str.lastIndexOf(delim2);
        if (indexValued < 0) {
            return value;
        }

        boolean bJustExpr = str.trim().length() == indexValued2 + delim2.length() - indexValued;

        while (pos < str.length()) {
            if (pos + delim.length() <= str.length()) {
                if (delim.equals(str.substring(pos, pos + delim.length()))) {
                    if (valueCount == 0) {
                        valueFrom = pos;
                    }
                    valueCount++;
                    pos = pos + delim.length();
                    continue;
                }
            }

            if (delim2.equals(str.substring(pos, pos + delim2.length()))) {
                valueCount--;
                if (valueCount == 0) {
                    valueTo = pos;
                    String expr = str.substring(valueFrom, valueTo + delim2.length());
                    //value = parseExpr(context, expr);
                    exprCount++;
                    // replace the string
                    StringBuffer sbuf = new StringBuffer(str);
                    if (null != value) {
                        String rep = value.toString();
                        // need quotation
                        if (quot) {
                            rep = "\"" + rep + "\"";
                        }
                        sbuf.replace(valueFrom, valueTo + delim2.length(), rep);
                        pos += delim2.length() + value.toString().length() - expr.length();
                    } else {
                        String rep = "";
                        // need quotation
                        if (quot) {
                            rep = "\"" + rep + "\"";
                        }
                        sbuf.replace(valueFrom, valueTo + delim2.length(), rep);
                        pos += delim2.length() + 0 - expr.length();
                    }
                    str = sbuf.toString();
                    continue;
                } else {
                    pos += delim2.length();
                    continue;
                }
            }
            pos++;
        }

        if (exprCount == 1 && bJustExpr) {
            if (null != value) {
                if (quot && "java.lang.String".equals(value.getClass().getName())) {
                    return "\"" + value.toString() + "\"";
                }
                return value;
            }
            return value;
        } else {
            return str;
        }
    }

    public static ITag getTagClass(String str) {
        str = str.substring(0, 1).toUpperCase() + str.substring(1, str.length());
        String tagName = str + "Tag";
        ITag tag = (ITag) tagMap.get(str);
        if (null == tag) {
            try {
                Class clazz = Class.forName(tagPackages + "." + tagName);
                tag = (ITag) clazz.newInstance();
            } catch (Exception e) {
                tag = null;
                e.printStackTrace();
            }
        }
        return tag;
    }

    /**
     * get Iterator from the object
     *
     * @param collection
     * @return Iterator of the object
     */
    public static Iterator getIterator(Object collection) {
        Iterator iterator = null;
        if (collection.getClass().isArray()) {
            try {
                // If we're lucky, it is an array of objects
                // that we can iterate over with no copying
                iterator = Arrays.asList((Object[]) collection).iterator();
            } catch (ClassCastException e) {
                // Rats -- it is an array of primitives
                int length = Array.getLength(collection);
                ArrayList c = new ArrayList(length);
                for (int i = 0; i < length; i++) {
                    c.add(Array.get(collection, i));
                }
                iterator = c.iterator();
            }
        } else if (collection instanceof Collection) {
            iterator = ((Collection) collection).iterator();
        } else if (collection instanceof Iterator) {
            iterator = (Iterator) collection;
        } else if (collection instanceof Map) {
            iterator = ((Map) collection).entrySet().iterator();
        }
        return iterator;
    }

    /**
     * get Row, if not exists, create
     *
     * @param rowCounter int
     * @param sheet      HSSFSheet
     * @return HSSFRow
     */
    public static HSSFRow getRow(int rowCounter, HSSFSheet sheet) {
        HSSFRow row = sheet.getRow((short) rowCounter);
        if (row == null) {
            row = sheet.createRow((short) rowCounter);
        }
        return row;
    }

    /**
     * get Cell, if not exists, create
     *
     * @param row    HSSFRow
     * @param column int
     * @return HSSFCell
     */
    public static HSSFCell getCell(HSSFRow row, int column) {
        HSSFCell cell = row.getCell(column);
        if (cell == null) {
            cell = row.createCell(column);
        }
        return cell;
    }

    /**
     * get Skip Num
     *
     * @param tagstart
     * @param tagend
     * @return skip number
     */
    public static int getSkipNum(int tagstart, int tagend) {
        return tagend - tagstart;
    }

    /**
     * get shift Num
     *
     * @param old_tagend
     * @param tagstart
     * @return shift number
     */
    public static int getShiftNum(int old_tagend, int tagstart) {
        return tagstart - old_tagend - 1;
    }

    public static int getExprRow(HSSFSheet sheet, int rownum, int cellnum) {
        ITag tag = getTagClass("transfer");
        return tag.getExprRow(sheet, rownum, cellnum);
    }

    /**
     * copy row
     *
     * @param sheet
     * @param from  begin of the row
     * @param to    destination fo the row
     * @param count count of copy
     */
    public static void copyRow(HSSFSheet sheet, int from, int to, int count) {

        for (int rownum = from; rownum < from + count; rownum++) {
            HSSFRow fromRow = sheet.getRow(rownum);
            HSSFRow toRow = getRow(to + rownum - from, sheet);
            if (null == fromRow)
                return;
            toRow.setHeight(fromRow.getHeight());
            toRow.setHeightInPoints(fromRow.getHeightInPoints());
            for (int i = fromRow.getFirstCellNum(); i < fromRow.getLastCellNum() && i >= 0; i++) {
                HSSFCell fromCell = getCell(fromRow, i);
                HSSFCell toCell = getCell(toRow, i);
                //toCell.setEncoding(fromCell.getEncoding());
                toCell.setCellStyle(fromCell.getCellStyle());
                toCell.setCellType(fromCell.getCellType());
//					switch (fromCell.getCellType()) {
//					case HSSFCell.CELL_TYPE_BOOLEAN:
//						toCell.setCellValue(fromCell.getBooleanCellValue());
//						break;
//					case HSSFCell.CELL_TYPE_FORMULA:
//						toCell.setCellFormula(fromCell.getCellFormula());
//						break;
//					case HSSFCell.CELL_TYPE_NUMERIC:
//						toCell.setCellValue(fromCell.getNumericCellValue());
//						break;
//					case HSSFCell.CELL_TYPE_STRING:
//						toCell.setCellValue(fromCell.getStringCellValue());
//						break;
//					default:
//					}
            }
        }
    }
}
