package com.bright.stats.manager.impl;

import com.bright.stats.manager.SqlInfoManager;
import com.bright.stats.pojo.model.HtmlSqlInfoItem;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.SqlInfo;
import com.bright.stats.pojo.po.primary.SqlInfoItem;
import com.bright.stats.repository.primary.SqlInfoItemRepository;
import com.bright.stats.repository.primary.SqlInfoRepository;
import com.bright.stats.util.Common;
import com.bright.stats.util.TableHeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/8/4 14:09
 * @Description
 */
@Component
@RequiredArgsConstructor
public class SqlInfoManagerImpl implements SqlInfoManager {

    @Autowired
    private SqlInfoManager sqlInfoManager;

    private final JdbcTemplate jdbcTemplatePrimary;
    private final SqlInfoRepository sqlInfoRepository;
    private final SqlInfoItemRepository sqlInfoItemRepository;

    @Override
    public SqlInfo getSqlInfo(Integer years, String typeCode, String sqlNo) {
        List<SqlInfo> sqlInfos = sqlInfoManager.listSqlInfos(years, typeCode);
        Optional<SqlInfo> optional = sqlInfos.stream().filter(sqlInfo -> Objects.equals(sqlNo, sqlInfo.getSqlNo())).findFirst();
        return optional.get();
    }

    @Override
    @Cacheable(value = "SqlInfo", key = "#root.methodName + #years + '_' + #typeCode")
    public List<SqlInfo> listSqlInfos(Integer years, String typeCode) {
        List<SqlInfo> sqlInfos = sqlInfoRepository.findSqlInfo(years, typeCode);
        for (SqlInfo sqlInfo : sqlInfos) {
            List<SqlInfoItem> sqlInfoItems = sqlInfoItemRepository.findSqlInfoItem(sqlInfo.getYears(), sqlInfo.getModalName());
            sqlInfo.setSqlInfoItems(sqlInfoItems);
            List<TableHeader> tableHeaders = new ArrayList<>();

            sqlInfoItems.forEach(fileItem -> {
                String[] split = fileItem.getFieldDis().split("\\|");
                for (int i = 0; i < split.length; i++) {
                    TableHeader tableHeader = new TableHeader();
                    tableHeader.setField(fileItem.getFieldName());
                    tableHeader.setTitle(split[i]);
                    tableHeader.setFieldType(fileItem.getFType());
                    tableHeader.setFieldFormat(fileItem.getDisFormat());
                    tableHeader.setSort(fileItem.getDefId());
                    tableHeader.setWidth(fileItem.getFLen());
                    if (null != fileItem.getIsFrozen() && fileItem.getIsFrozen()) {
                        tableHeader.setFixed("left");
                    }
                    if ("N".equalsIgnoreCase(fileItem.getFType())) {
                        tableHeader.setAlign("right");
                    } else {
                        tableHeader.setAlign("left");
                    }
                    String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                    tableHeader.setId(Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i);
                    String[] strings = Arrays.copyOfRange(split, 0, i);
                    tableHeader.setPid(Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1)));
                    if (!StringUtils.isEmpty(tableHeader.getPid())) {
                        tableHeader.setWidth(null);
                    }
                    tableHeaders.add(tableHeader);
                }
            });
            List<TableHeader> buildTree = TableHeaderUtil.buildTree(tableHeaders);

            Set<TableHeader> tableHeaderSet = new TreeSet<>(Comparator.comparing(TableHeader::getId));
            tableHeaderSet.addAll(buildTree);
            List<TableHeader> collect = tableHeaderSet.stream().collect(Collectors.toList());
            Collections.sort(collect, Comparator.comparing(TableHeader::getSort));

            sqlInfo.setTableHeaders(collect);

            if(sqlInfo.getSqlNo().length() == 4){
                List<List<HtmlSqlInfoItem>> htmlSqlInfoItems = this.getHTMLSqlInfoItems(sqlInfoItems);
                sqlInfo.setHtmlSqlInfoItems(htmlSqlInfoItems);
            }

        }
        return sqlInfos;
    }

    public List<List<HtmlSqlInfoItem>> getHTMLSqlInfoItems(List<SqlInfoItem> sqlInfoItems) {

        List<List<HtmlSqlInfoItem>> rvalue = new ArrayList<List<HtmlSqlInfoItem>>();

        int titleTrSize = getHTMLSqlInfoItemTrSize(sqlInfoItems); //共有多少表头行（不含固定列）
        int frozen = getHTMLSqlInfoItemFrozen(sqlInfoItems); //固定列数
        //固定表头
        List<HtmlSqlInfoItem> frozenHTMLSqlInfoItem = new ArrayList<HtmlSqlInfoItem>();
        int j = 0;
        for (SqlInfoItem sqlInfoItem : sqlInfoItems) {
            j++;
            if (j > frozen) break;

            HtmlSqlInfoItem htmlSqlInfoItem = covertHTMLSqlInfoItem(sqlInfoItem);
            htmlSqlInfoItem.setRowspan(titleTrSize);
            htmlSqlInfoItem.setAlign(getAlign(htmlSqlInfoItem, htmlSqlInfoItem.getFType()));
            //htmlFileItem.setSortable(true);

            htmlSqlInfoItem.setTitle("<span class=\"txtcenter\">" + htmlSqlInfoItem.getTitle() + "</span>");
            htmlSqlInfoItem.setLast(true);

            frozenHTMLSqlInfoItem.add(htmlSqlInfoItem);
        }
        rvalue.add(frozenHTMLSqlInfoItem);

        //初始化其它表头
        List<int[]> args = new ArrayList<int[]>();
        for (int i = 0; i < titleTrSize; i++) {
            List<HtmlSqlInfoItem> htmlSqlInfoItems = new ArrayList<HtmlSqlInfoItem>();
            int[] args0 = new int[sqlInfoItems.size() - frozen];
            rvalue.add(htmlSqlInfoItems);
            args.add(args0);
        }
        int i = 0;
        for (SqlInfoItem sqlInfoItem : sqlInfoItems) {
            i++;
            if (i <= frozen) continue;
            HtmlSqlInfoItem htmlSqlInfoItem = covertHTMLSqlInfoItem(sqlInfoItem);

            String[] arrayFieldDis = org.apache.commons.lang.StringUtils.split(htmlSqlInfoItem.getFieldDis(), "|");
            for (j = 0; j < arrayFieldDis.length; j++) {
                HtmlSqlInfoItem hSqlInfoItem = covertHTMLSqlInfoItem(sqlInfoItem);
                String title = arrayFieldDis[j];
                hSqlInfoItem.setTitle(title);
                hSqlInfoItem.setCount(arrayFieldDis.length);
                if (j == arrayFieldDis.length - 1) hSqlInfoItem.setLastColumn(true);
                rvalue.get(j + 1).add(hSqlInfoItem);
            }
            for (j = arrayFieldDis.length; j < titleTrSize; j++) {
                rvalue.get(j + 1).add(null);
            }
        }
        //设置表头合并行、合并列
        for (i = 1; i < rvalue.size(); i++) {
            List<HtmlSqlInfoItem> htmlSqlInfoItems = rvalue.get(i);

            j = 0;
            for (HtmlSqlInfoItem htmlSqlInfoItem : htmlSqlInfoItems) {
                if (htmlSqlInfoItem == null || htmlSqlInfoItem.isNeedRemove()) {
                    j++;
                    continue;
                }


                //当前列与上一列相同，则跳出
                if (j != 0) { //第一列不对比
                    boolean isbreak = true;
                    int groupRows = 0;
                    HtmlSqlInfoItem priorColumnHtmlSqlInfoItem = null;

                    int m = j - 1;
                    if (m >= 0) {
                        if (!isColumnSameText(rvalue, i, j, m)) {
                            isbreak = false;
                            //break;
                        } else {
                            groupRows = rvalue.get(i).get(m) == null ? groupRows : rvalue.get(i).get(m).getRowspan();

                            priorColumnHtmlSqlInfoItem = rvalue.get(i).get(m);
                        }

                        m--;
                    }

                    if (isbreak) {
                        //rvalue.get(i).set(j, null); //相同列，设置为null
                        rvalue.get(i).get(j).setNeedRemove(true);

                        if (priorColumnHtmlSqlInfoItem != null) {
                            args.get(i - 1)[j] = priorColumnHtmlSqlInfoItem.getRowspan();
                        }

                        if (groupRows > 1) {
                            for (int k = titleTrSize; k > i + groupRows - 1; k--) {
                                rvalue.get(k).set(j, rvalue.get(k - groupRows + 1).get(j));
                            }
                            for (int k = i + 1; k < i + groupRows; k++) {
                                rvalue.get(k).set(j, null);
                            }
                        }
                        j++;
                        continue;
                    }
                }

                int rowspan = 1, colspan = 1, columnGroupSize = htmlSqlInfoItem.getCount(), rowGroupSize = 0;
                boolean isColspan = false; //没有计算列数

                //取得列数
                if (!isColspan) {
                    //获取当前组的最大列数columnGroupSize及colspan
                    for (int k = j - 1; k >= 0; k--) {
                        HtmlSqlInfoItem priorColumnHtmlSqlInfoItem = htmlSqlInfoItems.get(k);

                        if (isColumnSameText(rvalue, i, j, k)) {
                            if (priorColumnHtmlSqlInfoItem.getCount() > columnGroupSize)
                                columnGroupSize = priorColumnHtmlSqlInfoItem.getCount();
                        } else {
                            break;
                        }
                    }
                    for (int k = j + 1; k < htmlSqlInfoItems.size(); k++) {
                        HtmlSqlInfoItem nextColumnHtmlSqlInfoItem = htmlSqlInfoItems.get(k);

                        if (isColumnSameText(rvalue, i, j, k)) {
                            colspan++;

                            if (nextColumnHtmlSqlInfoItem.getCount() > columnGroupSize)
                                columnGroupSize = nextColumnHtmlSqlInfoItem.getCount();
                        } else {
                            break;
                        }
                    }
                }

                //取得当前行数
                int hasRows = 0;
                rowGroupSize = 0;
                for (int k = i - 1; k >= 1; k--) {
                    if (args.get(k - 1)[j] != 0) {
                        hasRows++;
                        rowGroupSize += args.get(k - 1)[j];
                    }
                }
                rowspan = titleTrSize - (columnGroupSize - hasRows) - rowGroupSize + 1;

                htmlSqlInfoItem.setRowspan(rowspan);
                htmlSqlInfoItem.setColspan(colspan);


                args.get(i - 1)[j] = rowspan;

                //if(rowGroupSize + rowspan != titleTrSize) {
                //	htmlFileItem.setFieldName("");
                //}

                htmlSqlInfoItems.set(j, htmlSqlInfoItem);
                rvalue.get(i).set(j, htmlSqlInfoItem);

                //当前不是只有一行，需要合并行
                if (colspan > 1) {
                    for (int k = j + 1; k < j + colspan; k++) {
                        htmlSqlInfoItems.get(k).setRowspan(rowspan);
                        rvalue.get(i).get(k).setRowspan(rowspan);
                    }
                }

                if (rowspan > 1) {
                    for (int k = titleTrSize; k > rowGroupSize + rowspan; k--) {
                        rvalue.get(k).set(j, rvalue.get(k - rowspan + 1).get(j));
                    }

                    for (int k = rowGroupSize + 2; k < rowGroupSize + rowspan + 1; k++) {
                        rvalue.get(k).set(j, null);
                        //rvalue.get(k).remove(j);
                    }

                }

                j++;
            }
        }


        //clean
        int lastColumnIndex = -1;
        for (j = rvalue.get(1).size() - 1; j >= 0; j--) {
            //isLastColumn = false;
            int a = rvalue.size();
            i = a - 1;
            for (i = a - 1; i >= 1; i--) {
                HtmlSqlInfoItem htmlSqlInfoItem = rvalue.get(i).get(j);
                if (htmlSqlInfoItem == null || htmlSqlInfoItem.isNeedRemove()) {
                    rvalue.get(i).remove(j);

                } else {
                    if (lastColumnIndex == -1 || lastColumnIndex < i) {
                        lastColumnIndex = i;
                        //isLastColumn = false;
                    }

                    //排序
                    if (i == lastColumnIndex) {
                        rvalue.get(i).get(j).setAlign(getAlign(htmlSqlInfoItem, htmlSqlInfoItem.getFType())); //对齐方式（表头由js全部转为居中）
                        rvalue.get(i).get(j).setLast(true);

                        //if(StringUtils.equalsIgnoreCase(htmlFileItem.getfType(), "N") || StringUtils.equalsIgnoreCase(htmlFileItem.getfType(), "D")) {
                        //rvalue.get(i).get(j).setSortable(htmlSqlInfoItem.getIsSortable());
                    }
                    rvalue.get(i).get(j).setSortable(htmlSqlInfoItem.getIsSortable());
                    //超过5个字符换行
                    if (rvalue.get(i).size() >= j + 1 && htmlSqlInfoItem != null && htmlSqlInfoItem.getColspan() == 1) {
                        StringBuffer sb = new StringBuffer();
                        String title = htmlSqlInfoItem.getTitle();


                        int k = 0;
                        int fontRowSize = Math.abs((htmlSqlInfoItem.getFLen() - 4 * 2) / 12);

                        while (fontRowSize != 0 && org.apache.commons.lang.StringUtils.trimToEmpty(title).length() > fontRowSize) {
                            sb.append(title.substring(0, fontRowSize) + "<br>");

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

                    if (i != lastColumnIndex && htmlSqlInfoItem != null && htmlSqlInfoItem.getRowspan() != rvalue.size() - 1 && !htmlSqlInfoItem.isLastColumn()) {
                        rvalue.get(i).get(j).setFieldName("");

                        //if(htmlFileItem.getColspan() > 1)
                        rvalue.get(i).get(j).setFLen(0);
                    }
                }
            }
        }

        int years = 0;
        String tableName = null;
        if (sqlInfoItems.size() > 0) {
            years = sqlInfoItems.get(0).getYears();
            tableName = sqlInfoItems.get(0).getTableName();
        }

        //不存在fileItem里面的需要加入
        List<HtmlSqlInfoItem> notExistsSqlInfoItems = getHTMLSqlInfoItemsByNotExists(titleTrSize, years, tableName);
        for (HtmlSqlInfoItem htmlSqlInfoItem : notExistsSqlInfoItems) {
            rvalue.get(1).add(htmlSqlInfoItem);
        }


        //空行处理
        for (i = rvalue.size() - 1; i >= 1; i--) {
            if (rvalue.get(i).size() == 0) {
                rvalue.remove(i);
            }
        }

        return rvalue;
    }

    public int getHTMLSqlInfoItemTrSize(List<SqlInfoItem> sqlInfoItems) {
        //表头总共几行
        int titleTrSize = 0;
        for (SqlInfoItem sqlInfoItem : sqlInfoItems) {
            String fieldDis = sqlInfoItem.getFieldDis();

            int size = org.apache.commons.lang.StringUtils.countMatches(fieldDis, "|");
            if (size > titleTrSize) titleTrSize = size;
        }
        titleTrSize++;

        return titleTrSize;
    }

    private int getHTMLSqlInfoItemFrozen(List<SqlInfoItem> sqlInfoItems) {
        int rvalue = 0;

        for (SqlInfoItem sqlInfoItem : sqlInfoItems) {
            //if(StringUtils.equalsIgnoreCase(fileItem.getfType(), "N")) {
            if (Objects.isNull(sqlInfoItem.getReadOnly()) || !sqlInfoItem.getReadOnly()) {
                break;

            }

            rvalue++;
        }
        return rvalue;
    }

    private HtmlSqlInfoItem covertHTMLSqlInfoItem(SqlInfoItem sqlInfoItem) {
        HtmlSqlInfoItem rvalue = (sqlInfoItem == null ? null : new HtmlSqlInfoItem());

        if (rvalue == null) return rvalue;
        rvalue.setId(sqlInfoItem.getId());
        rvalue.setYears(sqlInfoItem.getYears());
        rvalue.setTableName(sqlInfoItem.getTableName());
        rvalue.setFieldName(sqlInfoItem.getFieldName());
        rvalue.setFieldDis(sqlInfoItem.getFieldDis());
        rvalue.setDisFlag(sqlInfoItem.getDisFlag());
        rvalue.setDisId(sqlInfoItem.getDisId());
        rvalue.setFLen(sqlInfoItem.getFLen());
        rvalue.setFDec(sqlInfoItem.getFDec());
        rvalue.setFType(sqlInfoItem.getFType());
        rvalue.setDefLen(sqlInfoItem.getDefLen());
        rvalue.setDefId(sqlInfoItem.getDefId());
        rvalue.setDefDis(sqlInfoItem.getDefDis());
        rvalue.setIsKey(sqlInfoItem.getIsKey());
        rvalue.setDisFormat(sqlInfoItem.getDisFormat());
        rvalue.setDefDisFormat(sqlInfoItem.getDefDisFormat());
        rvalue.setShortDis(sqlInfoItem.getShortDis());
        rvalue.setReadOnly(sqlInfoItem.getReadOnly());
        rvalue.setAlign(sqlInfoItem.getAlign());
        rvalue.setFormatter(sqlInfoItem.getFormatter());
        rvalue.setIsFrozen(sqlInfoItem.getIsFrozen());
        rvalue.setIsSortable(sqlInfoItem.getIsSortable());
        rvalue.setNoShow(Objects.isNull(sqlInfoItem.getIsHidden())?false:sqlInfoItem.getIsHidden());
        //设置HtmlFileItem默认值
        rvalue.setRowspan(1);
        rvalue.setColspan(1);
        rvalue.setTitle(rvalue.getFieldDis());
        rvalue.setIsGroup(sqlInfoItem.getIsGroup());
        return rvalue;
    }


    private int getAlign(HtmlSqlInfoItem htmlSqlInfoItem, String fType) {
        int rvalue = 1;
        rvalue = htmlSqlInfoItem.getAlign();
        return rvalue;
    }

    private boolean isColumnSameText(List<List<HtmlSqlInfoItem>> titles, int row, int column, int threeColumn) {
        boolean rvalue = true;

        for (int i = 1; i <= row; i++) {
            HtmlSqlInfoItem htmlSqlInfoItem = titles.get(i).get(column);
            HtmlSqlInfoItem nextHtmlSqlInfoItem = titles.get(i).get(threeColumn);

			/*HtmlFileItem priorHtmlFileItem = (i-1>=1) ? titles.get(i-1).get(column) : null;
			int priorHtmlFileItemRowspan

			int k = column - 1;
			while (htmlFileItem == null && k>=0) {
				htmlFileItem = titles.get(i)
				k--;
			}*/

            String title = htmlSqlInfoItem == null ? null : htmlSqlInfoItem.getTitle();
            String nextTitle = nextHtmlSqlInfoItem == null ? null : nextHtmlSqlInfoItem.getTitle();

            if (!org.apache.commons.lang.StringUtils.equals(title, nextTitle)) {
                rvalue = false;
                break;
            }
        }
        return rvalue;
    }

    public List<HtmlSqlInfoItem> getHTMLSqlInfoItemsByNotExists(int titleTrSize, int years, String tableName) {
        List<HtmlSqlInfoItem> rvalue = new ArrayList<HtmlSqlInfoItem>();

//		String sql = "select c.name,c.xtype from syscolumns c inner join sysobjects o on c.id=o.id and o.name=?" +
//				" and not exists(select * from fileItem where years=? and tablename=o.name and disflag=1 and fieldname=c.name)" +
//				" and not (c.name like 'c%' and (c.xtype=56 or c.xtype=60))";
        String sql = "${sycolumn}";

        List<Map<String, Object>> rvalue1 = null;
        String sql1 = "select mysql_fun, sql_fun, valuedec, sqlstr from fun_contrast where visible=1 and ttype=? order by disid";
        rvalue1 = jdbcTemplatePrimary.queryForList(sql1, new Object[]{2});

        List<Map<Object, Object>> mapList = new ArrayList<>();
        for (Map<String, Object> map : rvalue1) {
            Map<Object, Object> objectObjectMap = new HashMap<>(16);
            for (Map.Entry<String, Object> objectObjectEntry : map.entrySet()) {
                objectObjectMap.put(objectObjectEntry.getKey(), objectObjectEntry.getValue());
            }
            mapList.add(objectObjectMap);
        }

        sql = Common.replaceFun(sql, mapList);
        List<Map<String, Object>> mapList1 = jdbcTemplatePrimary.queryForList(sql, new Object[]{years, tableName});
        List<List<Object>> data = new ArrayList<>();
        for (Map<String, Object> map : mapList1) {
            List<Object> objectList = new ArrayList<>();
            for (String s : map.keySet()) {
                objectList.add(map.get(s));
            }
            data.add(objectList);
        }

        for (List<Object> dataSub : data) {
            HtmlSqlInfoItem sqlInfoItem = new HtmlSqlInfoItem();
            sqlInfoItem.setTableName(tableName);
            sqlInfoItem.setFieldName(org.apache.commons.lang.StringUtils.upperCase((String) dataSub.get(0)));
            sqlInfoItem.setFieldDis(sqlInfoItem.getFieldName());
            sqlInfoItem.setTitle(sqlInfoItem.getFieldName());
            sqlInfoItem.setAlign(2);
            sqlInfoItem.setFLen(50);
            sqlInfoItem.setColspan(1);
            sqlInfoItem.setRowspan(titleTrSize);
            sqlInfoItem.setSortable(false);
            sqlInfoItem.setNoShow(true);

            rvalue.add(sqlInfoItem);
        }

        return rvalue;
    }
}
