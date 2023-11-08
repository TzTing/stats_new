package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.manager.QueryCenterManager;
import com.bright.stats.pojo.dto.ExportExcelNoTemplateDTO;
import com.bright.stats.pojo.dto.ExportExcelQueryCenterDTO;
import com.bright.stats.pojo.model.HtmlFileItem;
import com.bright.stats.pojo.model.HtmlSqlInfoItem;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileItem;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.SqlInfo;
import com.bright.stats.pojo.po.primary.SqlInfoItem;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.query.QueryCenterQuery;
import com.bright.stats.service.QueryCenterService;
import com.bright.stats.util.Common;
import com.bright.stats.util.ExcelUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/3 16:43
 * @Description
 */
@Service
@RequiredArgsConstructor
public class QueryCenterServiceImpl implements QueryCenterService {

    private final QueryCenterManager queryCenterManager;
    private final FileListManager fileListManager;
    private final JdbcTemplate jdbcTemplatePrimary;

    @Override
    public List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months, String userDistNo) {
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS, tableName, years, months, userDistNo);
        return fileList.getTableHeaders();
    }

    @Override
    public PageResult<Map<String, Object>> listTableDataForPage(QueryCenterQuery queryCenterQuery) {
        PageResult<Map<String, Object>> mapPageResult = queryCenterManager.listTableDataForPage(queryCenterQuery);
        return mapPageResult;
    }

    /**
     * 查询分析表列表
     *
     * @param typeCode
     * @param years
     * @param months
     * @return
     */
    @Override
    public List<FileList> listAnalysisTables(String typeCode, Integer years, Integer months) {
        List<FileList> fileLists = queryCenterManager.listAnalysisTables(typeCode, years, months);
        return fileLists;
    }

    @Override
    public void exportExcel(ExportExcelQueryCenterDTO exportExcelQueryCenterDTO, HttpServletResponse response) {
        Integer years = exportExcelQueryCenterDTO.getYears();
        Integer months = exportExcelQueryCenterDTO.getMonths();
        String distNo = exportExcelQueryCenterDTO.getDistNo();
        String userDistNo = exportExcelQueryCenterDTO.getUserDistNo();
        String tableName = exportExcelQueryCenterDTO.getTableName();
        String typeCode = exportExcelQueryCenterDTO.getTypeCode();
        Integer optType = exportExcelQueryCenterDTO.getOptType();
        String lx = exportExcelQueryCenterDTO.getLx();


        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS, tableName, years, months, userDistNo);
        String sheetName = fileList.getTableDis();
        String titleName = fileList.getTableDis();
        String outFileName = years + "年" + (months != 0 ? months + "月" : "") + sheetName;


        QueryCenterQuery queryCenterQuery = new QueryCenterQuery();
        queryCenterQuery.setUserDistNo(userDistNo);
        queryCenterQuery.setTypeCode(typeCode);
        queryCenterQuery.setTableName(tableName);
        queryCenterQuery.setIsExcel(true);
        queryCenterQuery.setYears(years);
        queryCenterQuery.setMonths(months);
        queryCenterQuery.setDistNo(distNo);
        queryCenterQuery.setPageNumber(0);
        queryCenterQuery.setPageSize(10000);
        queryCenterQuery.setOptType(optType);
        queryCenterQuery.setLx(lx);

        PageResult<Map<String, Object>> pageResult = queryCenterManager.listTableDataForPage(queryCenterQuery);

        List<Map<String, Object>> list = pageResult.getData();

        List<FileItem> fileItems=fileList.getFileItems();
        List<Object[]> arrays=new ArrayList<Object[]>();
        int i=0;
        if(null!=list && list.size()>0){
            for (Map<String,Object> maps : list) {
                Object[] array=new Object[fileItems.size()];
                i=0;
                for (FileItem fm : fileItems) {
                    if(maps.containsKey(fm.getFieldName().toLowerCase())){
                        array[i]=maps.get(fm.getFieldName().toLowerCase());

                    }else if(StringUtils.equals(fm.getDisFlag(), "1")){
                        array[i]="";
                    }
                    i++;
                }
                arrays.add(array);
            }
        }



        ExcelUtil.excelExport(response, outFileName, titleName, sheetName, getHTMLFileItems(fileItems), fileItems, arrays, null);
    }


    public List<List<HtmlFileItem>> getHTMLFileItems(List<FileItem> fileItems) {
        List<List<HtmlFileItem>> rvalue = new ArrayList<List<HtmlFileItem>>();

//		List<FileItem> fileItems = getFileItems(years, tableName, fileItemName);

        int titleTrSize = getHTMLFileItemTrSize(fileItems); //共有多少表头行（不含固定列）
        int frozen = getHTMLFileItemFrozen(fileItems); //固定列数

        //固定表头
        List<HtmlFileItem> frozenHTMLFileItem = new ArrayList<HtmlFileItem>();
        int j=0;
        for(FileItem fileItem:fileItems) {
            j++;

            if(j>frozen) continue;

            HtmlFileItem htmlFileItem = covertHTMLFileItem(fileItem);
            htmlFileItem.setRowspan(titleTrSize);
            htmlFileItem.setAlign(getAlign(htmlFileItem, htmlFileItem.getFType()));
            //htmlFileItem.setSortable(true);


            htmlFileItem.setTitle("<span class=\"txtcenter\">" + htmlFileItem.getTitle() + "</span>");
            htmlFileItem.setLast(true);

            frozenHTMLFileItem.add(htmlFileItem);
        }
        rvalue.add(frozenHTMLFileItem);

        //初始化其它表头
        List<int[]> args = new ArrayList<int[]>();
        for(int i=0;i<titleTrSize;i++) {
            List<HtmlFileItem> htmlFileItems = new ArrayList<HtmlFileItem>();
            int[] args0 = new int[fileItems.size()-frozen];
            rvalue.add(htmlFileItems);
            args.add(args0);
        }
        int i=0;
        for(FileItem fileItem:fileItems) {
            i++;
            if(i<=frozen) continue;
            HtmlFileItem htmlFileItem = covertHTMLFileItem(fileItem);

            String[] arrayFieldDis = StringUtils.split(htmlFileItem.getFieldDis(), "|");
            if(null!=arrayFieldDis && arrayFieldDis.length>0){
                for(j=0;j<arrayFieldDis.length;j++) {
                    HtmlFileItem hFileItem = covertHTMLFileItem(fileItem);

                    String title = arrayFieldDis[j];


                    hFileItem.setTitle(title);
                    hFileItem.setCount(arrayFieldDis.length);

                    rvalue.get(j+1).add(hFileItem);
                }
                for(j=arrayFieldDis.length;j<titleTrSize;j++) {
                    rvalue.get(j+1).add(null);
                }
            }

        }

        //设置表头合并行、合并列
        for(i=1;i<rvalue.size();i++) {
            List<HtmlFileItem> htmlFileItems = rvalue.get(i);

            j=0;
            for(HtmlFileItem htmlFileItem:htmlFileItems) {
                if(htmlFileItem == null || htmlFileItem.isNeedRemove()) {
                    j++;
                    continue;
                }


                //当前列与上一列相同，则跳出
                if(j!=0) { //第一列不对比
                    boolean isbreak = true;
                    int groupRows = 0;
                    HtmlFileItem priorColumnHtmlFileItem = null;

                    int m=j-1;
                    if(m>=0) {
                        if (!isColumnSameText(rvalue, i, j, m)) {
                            isbreak = false;
                            //break;
                        } else {
                            groupRows = rvalue.get(i).get(m) == null ? groupRows : rvalue.get(i).get(m).getRowspan();

                            priorColumnHtmlFileItem = rvalue.get(i).get(m);
                        }

                        m--;
                    }

                    if(isbreak) {
                        //rvalue.get(i).set(j, null); //相同列，设置为null
                        rvalue.get(i).get(j).setNeedRemove(true);

                        if(priorColumnHtmlFileItem != null) {
                            args.get(i-1)[j] = priorColumnHtmlFileItem.getRowspan();
                        }

                        if (groupRows > 1) {
                            for(int k=titleTrSize;k>i+groupRows-1;k--) {
                                rvalue.get(k).set(j, rvalue.get(k-groupRows + 1).get(j));
                            }
                            for(int k=i+1;k<i+groupRows;k++) {
                                rvalue.get(k).set(j, null);
                            }
                        }
                        j++;
                        continue;
                    }
                }

                int rowspan = 1, colspan = 1, columnGroupSize = htmlFileItem.getCount(), rowGroupSize = 0;
                boolean isColspan = false; //没有计算列数

                //取得列数
                if (!isColspan) {
                    //获取当前组的最大列数columnGroupSize及colspan
                    for(int k=j-1;k>=0;k--) {
                        HtmlFileItem priorColumnHtmlFileItem = htmlFileItems.get(k);

                        if(isColumnSameText(rvalue, i, j, k)) {
                            if(priorColumnHtmlFileItem.getCount()>columnGroupSize) columnGroupSize = priorColumnHtmlFileItem.getCount();
                        } else {
                            break;
                        }
                    }
                    for(int k=j+1;k<htmlFileItems.size();k++) {
                        HtmlFileItem nextColumnHtmlFileItem = htmlFileItems.get(k);

                        if(isColumnSameText(rvalue, i, j, k)) {
                            colspan++;

                            if(nextColumnHtmlFileItem.getCount()>columnGroupSize) columnGroupSize = nextColumnHtmlFileItem.getCount();
                        } else {
                            break;
                        }
                    }
                }

                //取得当前行数
                int hasRows = 0;
                rowGroupSize = 0;
                for(int k=i-1;k>=1;k--) {
                    if(args.get(k-1)[j] != 0) {
                        hasRows++;
                        rowGroupSize += args.get(k-1)[j];
                    }
                }
                rowspan = titleTrSize - (columnGroupSize - hasRows) - rowGroupSize + 1;

                htmlFileItem.setRowspan(rowspan);
                htmlFileItem.setColspan(colspan);


                args.get(i-1)[j] = rowspan;

                //if(rowGroupSize + rowspan != titleTrSize) {
                //	htmlFileItem.setFieldName("");
                //}

                htmlFileItems.set(j, htmlFileItem);
                rvalue.get(i).set(j, htmlFileItem);

                //当前不是只有一行，需要合并行
                if(colspan>1) {
                    for(int k=j+1;k<j+colspan;k++) {
                        htmlFileItems.get(k).setRowspan(rowspan);
                        rvalue.get(i).get(k).setRowspan(rowspan);
                    }
                }

                if(rowspan>1) {
                    for(int k=titleTrSize;k>rowGroupSize+rowspan;k--) {
                        rvalue.get(k).set(j, rvalue.get(k - rowspan + 1).get(j));
                    }

                    for(int k=rowGroupSize+2;k<rowGroupSize + rowspan + 1;k++) {
                        rvalue.get(k).set(j, null);
                        //rvalue.get(k).remove(j);
                    }

                }

                j++;
            }
        }


        //clean
        int lastColumnIndex = -1;
        for(j=rvalue.get(1).size()-1;j>=0;j--) {
            //isLastColumn = false;
            int a=rvalue.size();
            i=a-1;
            for(i=a-1;i>=1;i--) {
                HtmlFileItem htmlFileItem = rvalue.get(i).get(j);
                if(htmlFileItem == null || htmlFileItem.isNeedRemove()) {
                    rvalue.get(i).remove(j);

                } else {
                    if(lastColumnIndex == -1|| lastColumnIndex<i) {
                        lastColumnIndex = i;
                        //isLastColumn = false;
                    }

                    //排序
                    if(i==lastColumnIndex) {
                        rvalue.get(i).get(j).setAlign(getAlign(htmlFileItem, htmlFileItem.getFType())); //对齐方式（表头由js全部转为居中）
                        rvalue.get(i).get(j).setLast(true);

                        //if(StringUtils.equalsIgnoreCase(htmlFileItem.getfType(), "N") || StringUtils.equalsIgnoreCase(htmlFileItem.getfType(), "D")) {
                        rvalue.get(i).get(j).setSortable(htmlFileItem.getIsSortable());
                    }

                    //超过5个字符换行
                    if(rvalue.get(i).size() >= j+1 && htmlFileItem != null && htmlFileItem.getColspan()==1) {
                        StringBuffer sb = new StringBuffer();
                        String title = htmlFileItem.getTitle();


                        int k=0;
                        int fontRowSize = Math.abs((htmlFileItem.getFLen() - 4*2) / 12);
                        //fontRowSize = 0;

                        while (fontRowSize != 0 && StringUtils.trimToEmpty(title).length()>fontRowSize) {
                            sb.append(title.substring(0, fontRowSize)+"<br>");

                            title = title.substring(fontRowSize);

                            k++;
                        }
                        sb.append(title);// + htmlFileItem.getFieldDis() + "::::" + htmlFileItem.getTitle());

//						if(i==lastColumnIndex) {
//							rvalue.get(i).get(j).setTitle("<span class=\"txtcenter\">" + sb.toString() + "</span>");
//						} else {
                        rvalue.get(i).get(j).setTitle(sb.toString());
//						}
                    }

                    if(i!=lastColumnIndex &&  htmlFileItem != null && htmlFileItem.getRowspan()!=rvalue.size()-1) {
                        rvalue.get(i).get(j).setFieldName("");
                        //if(htmlFileItem.getColspan() > 1)
                        rvalue.get(i).get(j).setFLen(0);
                        //rvalue.get(i).get(j).setAlign(2);

                    }
                }
            }
        }

        int years=0;
        String tableName = null;
        if(fileItems.size()>0) {
            years = fileItems.get(0).getYears();
            tableName = fileItems.get(0).getTableName();
        }

        //不存在fileItem里面的需要加入
        List<HtmlFileItem> notExistsFileItems = getHTMLFileItemsByNotExists(titleTrSize, years, tableName);
        for(HtmlFileItem htmlFileItem:notExistsFileItems) {
            rvalue.get(1).add(htmlFileItem);
        }


        //空行处理
        for(i=rvalue.size()-1;i>=1;i--) {
            if(rvalue.get(i).size() == 0) {
                rvalue.remove(i);
            }
        }

        return rvalue;
    }


    public List<HtmlFileItem> getHTMLFileItemsByNotExists(int titleTrSize, int years, String tableName) {
        List<HtmlFileItem> rvalue = new ArrayList<HtmlFileItem>();

//		String sql = "select c.name,c.xtype from syscolumns c inner join sysobjects o on c.id=o.id and o.name=?" +
//				" and not exists(select * from fileItem where years=? and tablename=o.name and disflag=1 and fieldname=c.name)" +
//				" and not (c.name like 'c%' and (c.xtype=56 or c.xtype=60))";
        String sql="${sycolumn}";
        List<Map<String, Object>> raData=this.funcontrast(2);
        sql= Common.replaceFun1(sql,raData);
        List<Map<String, Object>> mapList = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, tableName});

        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            List<Object> values = (List<Object>) map.values();
            data.add(values);
        }

        for(List<Object> dataSub:data) {
            HtmlFileItem fileItem = new HtmlFileItem();
            fileItem.setTableName(tableName);
            fileItem.setFieldName(StringUtils.upperCase((String)dataSub.get(0)));
            fileItem.setFieldDis(fileItem.getFieldName());
            fileItem.setTitle(fileItem.getFieldName());
            fileItem.setAlign(2);
            fileItem.setFLen(50);
            fileItem.setColspan(1);
            fileItem.setRowspan(titleTrSize);
            fileItem.setSortable(false);
            fileItem.setNoShow(true);

            rvalue.add(fileItem);
        }

        return rvalue;
    }


    public List<HtmlFileItem> queryHTMLFileItemsByNoStr(List<FileItem> fileItems,String tableName,String strColumn){
        List<HtmlFileItem> rvalue = new ArrayList<HtmlFileItem>();
        int titleTrSize = getHTMLFileItemTrSize(fileItems); //共有多少表头行（不含固定列）
        String[] strs=strColumn.split(",");
        for (int i = 0; i < strs.length; i++) {
            HtmlFileItem fileItem = new HtmlFileItem();
            fileItem.setTableName(tableName);
            fileItem.setFieldName(strs[i]);
            fileItem.setFieldDis(fileItem.getFieldName());
            fileItem.setTitle(fileItem.getFieldName());
            fileItem.setAlign(2);
            fileItem.setFLen(50);
            fileItem.setColspan(1);
            fileItem.setRowspan(titleTrSize);
            fileItem.setSortable(false);
            fileItem.setNoShow(true);
            rvalue.add(fileItem);
        }


        return rvalue;
    }

    public int getHTMLFileItemTrSize(List<FileItem> fileItems) {
        //表头总共几行
        int titleTrSize = 0;
        for(FileItem fileItem:fileItems) {
            String fieldDis = fileItem.getFieldDis();

            int size = StringUtils.countMatches(fieldDis, "|");
            if(size>titleTrSize) titleTrSize = size;
        }
        titleTrSize ++;

        return titleTrSize;
    }

    private int getHTMLFileItemFrozen(List<FileItem> fileItems) {
        int rvalue = 0;

        for(FileItem fileItem:fileItems) {
            //if(StringUtils.equalsIgnoreCase(fileItem.getfType(), "N")) {
            if(!fileItem.getIsFrozen()) {
                break;

            }

            rvalue++;
        }

        return rvalue;
    }

    private HtmlFileItem covertHTMLFileItem(FileItem fileItem) {
        HtmlFileItem rvalue = (fileItem == null ? null : new HtmlFileItem());

        if(rvalue == null) return rvalue;

        rvalue.setId(fileItem.getId());
        rvalue.setYears(fileItem.getYears());
        rvalue.setTableName(fileItem.getTableName());
        rvalue.setFieldName(fileItem.getFieldName());
        rvalue.setFieldDis(fileItem.getFieldDis());
        rvalue.setDisFlag(fileItem.getDisFlag());
        rvalue.setDisId(fileItem.getDisId());
        rvalue.setFLen(fileItem.getFLen());
        rvalue.setFDec(fileItem.getFDec());
        rvalue.setFType(fileItem.getFType());
        rvalue.setDefLen(fileItem.getDefLen());
        rvalue.setDefId(fileItem.getDefId());
        rvalue.setDefdis(fileItem.getDefdis());
        rvalue.setIsKey(fileItem.getIsKey());
        rvalue.setDisFormat(fileItem.getDisFormat());
        rvalue.setDefDisFormat(fileItem.getDefDisFormat());
        rvalue.setShortDis(fileItem.getShortDis());
        rvalue.setFileItemValue(fileItem.getFileItemValue());
        rvalue.setDw(fileItem.getDw());
        rvalue.setIsFrozen(fileItem.getIsFrozen());
        rvalue.setIsSortable(fileItem.getIsSortable());
        rvalue.setAlign(fileItem.getAlign());
        rvalue.setFormatter(fileItem.getFormatter());
        rvalue.setStyler(fileItem.getStyler());
//        rvalue.setZd(fileItem.getZd());

        //设置HtmlFileItem默认值
        rvalue.setRowspan(1);
        rvalue.setColspan(1);
        rvalue.setTitle(rvalue.getFieldDis());

        return rvalue;
    }

    private int getAlign(HtmlFileItem htmlFileItem, String fType) {
        int rvalue = 1;

		/*if(StringUtils.equalsIgnoreCase(fType, "N")) {
			rvalue = 3;
		}
		if(StringUtils.equalsIgnoreCase(fType, "D")) {
			rvalue = 2;
		}*/

        rvalue = htmlFileItem.getAlign();

        return rvalue;
    }


    private boolean isColumnSameText(List<List<HtmlFileItem>> titles, int row, int column, int threeColumn) {
        boolean rvalue = true;

        for(int i=1;i<=row;i++) {
            HtmlFileItem htmlFileItem = titles.get(i).get(column);
            HtmlFileItem nextHtmlFileItem = titles.get(i).get(threeColumn);

			/*HtmlFileItem priorHtmlFileItem = (i-1>=1) ? titles.get(i-1).get(column) : null;
			int priorHtmlFileItemRowspan

			int k = column - 1;
			while (htmlFileItem == null && k>=0) {
				htmlFileItem = titles.get(i)
				k--;
			}*/

            String title = htmlFileItem == null ? null : htmlFileItem.getTitle();
            String nextTitle = nextHtmlFileItem == null ? null : nextHtmlFileItem.getTitle();

            if(!StringUtils.equals(title, nextTitle)) {
                rvalue = false;
                break;
            }
        }
        return rvalue;
    }

    public List<Map<String, Object>> funcontrast(int ttype) {
        List<Map<String, Object>> rvalue = null;
        String sql = "select mysql_fun, sql_fun, valuedec, sqlstr, kingbase_sqlstr from fun_contrast where visible=1 and ttype=? order by disid";
        rvalue = jdbcTemplatePrimary.queryForList(sql, new Object[]{ttype});
        return rvalue;
    }

}
