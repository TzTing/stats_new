package com.bright.stats.manager.impl;

import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.pojo.model.HtmlFileItem;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.repository.primary.*;
import com.bright.stats.util.Common;
import com.bright.stats.util.TableHeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/6/30 16:27
 * @Description
 */
@Component
@RequiredArgsConstructor
public class FileListManagerImpl implements FileListManager {

    @Autowired
    private FileListManager fileListManager;
    private final FileListRepository fileListRepository;
    private final FileItemRepository fileItemRepository;
    private final FileItemLinkRepository fileItemLinkRepository;
    private final FileItemLinkExRepository fileItemLinkExRepository;
    private final FileItemLinkExExRepository fileItemLinkExExRepository;
    private final RuleInnerRepository ruleInnerRepository;
    private final RuleOuterRepository ruleOuterRepository;
    private final AnsTableRepository ansTableRepository;
    private final JdbcTemplate jdbcTemplatePrimary;

    @Override
    public FileList getFileList(String typeCode, String tableType, String tableName, Integer years, Integer months, String userDistNo) {
        List<FileList> fileLists = fileListManager.listFileLists(typeCode, tableType, years, months, userDistNo);
        Optional<FileList> optionalFileList = fileLists.stream().filter(fileList -> fileList.getTableName().equalsIgnoreCase(tableName)).findFirst();
        if(!optionalFileList.isPresent()){
//            throw new RuntimeException("未配置基础表！");
            return null;
        }
        return optionalFileList.get();
    }

    @Override
    @Cacheable(value = "FileList", key = "#root.methodName + '_' + #typeCode + '_' + #tableType + '_' + #years + '_' + #months")
    public List<FileList> listFileListsByCache(String typeCode, String tableType, Integer years, Integer months) {
        List<FileList> fileListList = fileListRepository.findFileList(typeCode, tableType, years);
        List<FileList> fileLists = fileListList.stream().filter(fileList -> {
            String numberOfMonthString = fileList.getTypeDescription();
            String[] monthsArray = (Objects.isNull(numberOfMonthString) || StringUtils.isEmpty(numberOfMonthString)) ? null : numberOfMonthString.split(",");
            if (Objects.nonNull(monthsArray)) {
                for (String monthsString : monthsArray) {
                    if (monthsString.equals(String.valueOf(months))) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }

        }).collect(Collectors.toList());

        for (FileList fileList : fileLists) {
            Integer fileListYears = fileList.getYears();
            String fileListTableName = fileList.getTableName();
            List<FileItem> fileItems = null;
            List<FileItem> fileItemList = null;
            if (FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS.equals(tableType)) {
                fileItems = fileItemRepository.findFileItem(fileListYears, "ans_" + fileList.getAnsNo());
                fileItemList = fileItemRepository.findFileItemByDisFlag(fileListYears, "ans_" + fileList.getAnsNo());
            } else {
                fileItems = fileItemRepository.findFileItem(fileListYears, fileListTableName);
                fileItemList = fileItemRepository.findFileItemByDisFlag(fileListYears, fileListTableName);
            }

            List<TableHeader> tableHeaders = new ArrayList<>();

            List<List<HtmlFileItem>> htmlFileItems = getHTMLFileItems(fileItemList);

            List<HtmlFileItem> htmlFileItemList = htmlFileItems.stream().flatMap(Collection::stream).collect(Collectors.toList());

            /*fileItemList.forEach(fileItem -> {
                //上一个
                FileItem lastFileIte = null;
                String[] split = fileItem.getFieldDis().split("\\|");
                for (int i = 0; i < split.length; i++) {
                    TableHeader tableHeader = new TableHeader();
                    tableHeader.setField(fileItem.getFieldName());
                    tableHeader.setTitle(split[i]);
                    tableHeader.setFieldType(fileItem.getFType());
                    tableHeader.setFieldFormat(fileItem.getDisFormat());
//                    tableHeader.setSort(fileItem.getDefId());
                    tableHeader.setSort(fileItem.getDisId());
                    tableHeader.setWidth(fileItem.getFLen());
                    if (null != fileItem.getIsFrozen() && fileItem.getIsFrozen()) {
                        tableHeader.setFixed("left");
                    }
                    if ("N".equalsIgnoreCase(fileItem.getFType())) {
                        tableHeader.setAlign("right");
                    } else {
                        tableHeader.setAlign("left");
                    }

                    if(lastFileIte == null){
                        String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                        tableHeader.setId(Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i);
                        String[] strings = Arrays.copyOfRange(split, 0, i);
                        tableHeader.setPid(Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1)));
                    } else {

                        String[] lastFieldDis = lastFileIte.getFieldDis().split("\\|");

                        String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                        String[] strings = Arrays.copyOfRange(split, 0, i);

                        String id = Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i;
                        String pid = Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1));

                        if(lastFieldDis.length <= split.length){
                            String temp = lastFieldDis[i];

                            //如果当前行的当前列和上一列一样 则合并
                            if(temp.equalsIgnoreCase(split[i])){
                                tableHeader.setId(id);
                            } else {
                                tableHeader.setId(id + "_" + fileItem.getId());
                            }
                        } else {
                            tableHeader.setId(id);
                        }


                        if(lastFieldDis.length <= split.length){
                            if(i - 1 == -1){
                                tableHeader.setPid(pid);
                            } else {
                                String temp = lastFieldDis[i];
                                if(temp.equalsIgnoreCase(split[i])){
                                    tableHeader.setPid(pid);
                                } else {
                                    tableHeader.setPid(pid + "_" + fileItem.getId());
                                }
                            }
                        } else {
                            tableHeader.setPid(pid);
                        }
                    }

                    if (!StringUtils.isEmpty(tableHeader.getPid())) {
                        tableHeader.setWidth(null);
                    }
                    tableHeaders.add(tableHeader);
                }
                lastFileIte = fileItem;
            });*/

            //上一个
            FileItem lastFileIte = null;

            StringBuilder lastDisFields = new StringBuilder();

            int maxRow = getHTMLFileItemTrSize(fileItems);

            //上一个填充后的表头列
            String[] lastDisFieldArrays = new String[maxRow];

            //当前填充后的表头列
            String[] cureDisFieldArrays = new String[maxRow];

            //根据表头最大行数初始化
            String[] lastItemId = new String[maxRow];
            for(int i = 0; i < lastItemId.length - 1; i++){
                lastItemId[i] = "";
                lastDisFieldArrays[i] = "";
                cureDisFieldArrays[i] = "";
            }

            for(int j = 0; j < fileItemList.size(); j++){

                String[] split = fileItemList.get(j).getFieldDis().split("\\|");

                int rowIndex = 1;

                if(split.length <= maxRow - 1 && !fileItemList.get(j).getIsFrozen()){
                    String[] temp = new String[maxRow];
                    int tempIndex = 0;

                    for(int i = 0; i < split.length; i++){

                        List<HtmlFileItem> tempRowHtml = htmlFileItems.get(rowIndex);
                        for(int k = 0; k < tempRowHtml.size(); k++){
                            HtmlFileItem tempHtmlFileItem = tempRowHtml.get(k);

                            if(!fileItemList.get(j).getTableName().equalsIgnoreCase(tempHtmlFileItem.getTableName())){
                                continue;
                            }

                            if(!tempHtmlFileItem.getTitle().equalsIgnoreCase(split[i])){
                                continue;
                            }

                            if(tempHtmlFileItem.getTitle().equalsIgnoreCase(split[i])
                                    && tempHtmlFileItem.getColspan() != null
                                    && tempHtmlFileItem.getColspan() > 1){

                            } else {
                                if(!fileItemList.get(j).getFieldDis().equalsIgnoreCase(tempHtmlFileItem.getFieldDis())){
                                    continue;
                                }
                            }


                            if(tempHtmlFileItem.getRowspan() != null
                                    && tempHtmlFileItem.getRowspan() > 1){
                                temp[tempIndex] = split[i];
                                tempIndex++;
                                rowIndex++;
                                lastDisFields.append(split[i]).append("|");

                                htmlFileItemList.get(k).setColspan(htmlFileItemList.get(k).getColspan());
                                for(int l = 0; l < tempHtmlFileItem.getRowspan() - 1; l++){
                                    temp[tempIndex] = "";
                                    tempIndex++;
                                    rowIndex++;
                                    lastDisFields.append("").append("|");
                                }
                            } else {
                                temp[tempIndex] = split[i];
                                tempIndex++;
                                rowIndex++;
                                lastDisFields.append(split[i]).append("|");
                            }
//                            break;
                            /*if(tempHtmlFileItem.getColspan() != null
                                    && tempHtmlFileItem.getColspan() > 1) {
                                colspan = tempHtmlFileItem.getColspan() - colspan - 1;
                            } else if (tempHtmlFileItem.getColspan() != null){
                                colspan = tempHtmlFileItem.getColspan();
                            } else {
                                colspan = 1;
                            }*/
                        }
                    }



                    /*for(int i = 0; i < temp.length; i++){
                        if(i > 0 && (i + 1) % 2 == 0 && needSize > 0){
                            temp[i] = "";
                            needSize--;
                        } else {
                            temp[i] = split[splitIndex];
                            splitIndex++;
                        }
                    }*/
//                    System.out.println(lastDisFields.substring(0, lastDisFields.length() - 1));
                    cureDisFieldArrays = lastDisFields.toString().split("\\|");
                    lastDisFields.setLength(0);
                    split = temp;
                } else {
                    cureDisFieldArrays = split;
                }

                for (int i = 0; i < split.length; i++) {
                    TableHeader tableHeader = new TableHeader();
                    tableHeader.setField(fileItemList.get(j).getFieldName());
                    tableHeader.setTitle(split[i]);
                    tableHeader.setFieldType(fileItemList.get(j).getFType());
                    tableHeader.setFieldFormat(fileItemList.get(j).getDisFormat());
                    tableHeader.setSort(fileItemList.get(j).getDisId());
                    tableHeader.setWidth(fileItemList.get(j).getFLen());
                    tableHeader.setLevel(i + 1);
                    tableHeader.setFormatterSelect(fileItemList.get(j).getFormatterSelect());
                    if (null != fileItemList.get(j).getIsFrozen() && fileItemList.get(j).getIsFrozen()) {
                        tableHeader.setFixed("left");
                    }

                    if(fileItemList.get(j).getAlign() != null) {
                        if(fileItemList.get(j).getAlign() == 1){
                            tableHeader.setAlign("left");
                        }
                        if(fileItemList.get(j).getAlign() == 2){
                            tableHeader.setAlign("center");
                        }
                        if(fileItemList.get(j).getAlign() == 3){
                            tableHeader.setAlign("right");
                        }
                    } else {
                        if ("N".equalsIgnoreCase(fileItemList.get(j).getFType())) {
                            tableHeader.setAlign("right");
                        } else {
                            tableHeader.setAlign("left");
                        }
                    }

                    if(lastFileIte == null){
                        String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                        tableHeader.setId(Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i);
                        String[] strings = Arrays.copyOfRange(split, 0, i);
                        tableHeader.setPid(Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1)));
                    } else {

//                        四、集体所有农用地总面积|其中：未承包到户面积|万亩|18
                          //id = aaa pid = _null
//                        四、集体所有农用地总面积|3.林地|万亩|19
                        //

//                        String[] lastFieldDis = lastFileIte.getFieldDis().split("\\|");
                        String[] lastFieldDis = lastDisFieldArrays;

                        String[] stringArray = Arrays.copyOfRange(split, 0, i + 1);
                        String[] strings = Arrays.copyOfRange(split, 0, i);

                        String id = Arrays.toString(stringArray).replace("[", "").replace("]", "") + "_" + i;
                        String pid = Arrays.toString(strings).replace("[", "").replace("]", "") + "_" + ((i - 1) == -1 ? null : (i - 1));


                        String temp = Arrays.toString(Arrays.copyOfRange(lastFieldDis, 0, i + 1))
                                .replace("[", "").replace("]", "");


                        //如果当前行的当前列和上一列一样 则合并
//                        if(temp.equalsIgnoreCase(Arrays.toString(stringArray).replace("[", "").replace("]", ""))){
//                            tableHeader.setId(id);
//                        } else {
//                            tableHeader.setId(id + "_" + fileItemList.get(j).getId());
//                        }

                        //如果当前行和当前列是相同的 就从记录中取上一行的上一列的id
                        if(temp.equalsIgnoreCase(Arrays.toString(stringArray).replace("[", "").replace("]", ""))){
                            tableHeader.setId(lastItemId[i]);
                        } else {
                            //不一样就设置新的id 并保存当前列
                            tableHeader.setId(id + "_" + fileItemList.get(j).getId());
                            lastItemId[i] = id + "_" + fileItemList.get(j).getId();
                        }



                        String temp2 = Arrays.toString(Arrays.copyOfRange(lastFieldDis, 0, i))
                                .replace("[", "").replace("]", "");

                        //如果当前行列的上一级等于上行列的上一级
                        if(temp2.equalsIgnoreCase(Arrays.toString(strings).replace("[", "").replace("]", ""))){
                            //并且当前是第一季
                            if (i - 1 == -1) {
                                tableHeader.setPid(pid);
                            } else {
                                //如果不是则设置为上行列的上一级
                                tableHeader.setPid(lastItemId[i - 1]);
                            }
                        } else {
                            //不等于则设置新值
                            tableHeader.setPid(pid + "_" + fileItemList.get(j).getId());
                        }

//                        if(temp2.equalsIgnoreCase(Arrays.toString(strings).replace("[", "").replace("]", ""))){
//                            tableHeader.setPid(pid);
//                        } else {
//                            tableHeader.setPid(pid + "_" + fileItemList.get(j).getId());
//                        }

                    }

                    if (!StringUtils.isEmpty(tableHeader.getPid())
                            && (tableHeader.getFixed() != null && !tableHeader.getFixed().equalsIgnoreCase("left"))) {
                        tableHeader.setWidth(null);
                    }
                    tableHeaders.add(tableHeader);
                }

                //记录当前的循环 下一次循环使用
                lastFileIte = fileItemList.get(j);

                lastDisFieldArrays = cureDisFieldArrays;

//                if(!fileItemList.get(j).getIsFrozen()){
//
//                }
            }


            for(TableHeader tableHeader : tableHeaders){
//                int level = tableHeader.getId().split(",").length;
                List<HtmlFileItem> tempHtmlList = htmlFileItems.get(tableHeader.getLevel());
                for(HtmlFileItem htmlFileItem : tempHtmlList){
                    if(tableHeader.getTitle().equalsIgnoreCase(htmlFileItem.getTitle())){
                        tableHeader.setRowspan(htmlFileItem.getRowspan());
                        tableHeader.setColspan(htmlFileItem.getColspan());
                        tableHeader.setWidth(htmlFileItem.getFLen());
                    }
                }
            }

            List<TableHeader> buildTree = TableHeaderUtil.buildTree(tableHeaders);

            if (FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS.equals(tableType)) {
//                Set<TableHeader> tableHeaderSet = new TreeSet<>(Comparator.comparing(TableHeader::getId));
//                tableHeaderSet.addAll(buildTree);
//                List<TableHeader> collect = tableHeaderSet.stream().collect(Collectors.toList());
                Collections.sort(buildTree, Comparator.comparing(TableHeader::getSort));
                fileList.setTableHeaders(buildTree);
            } else {
                Set<TableHeader> tableHeaderSet = new TreeSet<>(Comparator.comparing(TableHeader::getId));
                tableHeaderSet.addAll(buildTree);
                List<TableHeader> collect = tableHeaderSet.stream().collect(Collectors.toList());
                Collections.sort(collect, Comparator.comparing(TableHeader::getSort));
                fileList.setTableHeaders(collect);
            }

//            Set<TableHeader> tableHeaderSet = new TreeSet<>(Comparator.comparing(TableHeader::getId));
//            tableHeaderSet.addAll(buildTree);
//            List<TableHeader> collect = tableHeaderSet.stream().collect(Collectors.toList());
//            Collections.sort(collect, Comparator.comparing(TableHeader::getSort));
//
//            fileList.setTableHeaders(collect);

            fileList.setFileItems(fileItems);
            List<FileItemLink> fileItemLinks = fileItemLinkRepository.findFileItemLink(fileListYears, fileListTableName);
            if (!CollectionUtils.isEmpty(fileItemLinks)) {
                fileList.setFileItemLink(fileItemLinks.get(0));
                String prjType = fileItemLinks.get(0).getPrjType();
                List<FileItemLinkEx> fileItemLinkExs = fileItemLinkExRepository.findFileItemLinkEx(fileListYears, fileListTableName, prjType);
                fileList.setFileItemLinkExs(fileItemLinkExs);
                List<FileItemLinkExEx> fileItemLinkExExs = fileItemLinkExExRepository.findFileItemLinkExEx(fileListYears, fileListTableName, prjType);
                Map<String, List<FileItemLinkExEx>> fileItemLinkExExsMap = fileItemLinkExExs.stream().collect(Collectors.groupingBy(FileItemLinkExEx::getDistType));
                fileList.setFileItemLinkExExsMap(fileItemLinkExExsMap);
            }
            List<RuleInner> ruleInners = ruleInnerRepository.findRuleInner(fileListYears, fileListTableName);
            fileList.setRuleInners(ruleInners);
            List<RuleOuter> ruleOuters = ruleOuterRepository.findRuleOuter(fileListYears, fileListTableName);
            fileList.setRuleOuters(ruleOuters);
            if (FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS.equals(tableType)) {
                List<AnsTable> ansTables = ansTableRepository.findAnsTable(fileListYears, months, fileList.getAnsNo());
                fileList.setAnsTables(ansTables);
            }

        }
        return fileLists;
    }

    @Override
    public List<FileList> listFileLists(String typeCode, String tableType, Integer years, Integer months, String userDistNo) {
        List<FileList> fileLists = fileListManager.listFileListsByCache(typeCode, tableType, years, months);
        if(!org.apache.commons.lang3.StringUtils.isBlank(userDistNo) && "0".equals(userDistNo)){
            return fileLists;
        }
        List<FileList> collects = fileLists.stream().filter(fileList -> {
            if (org.apache.commons.lang3.StringUtils.isBlank(fileList.getBelongDistNo()) ||
                    (fileList.getBelongDistNo().startsWith(userDistNo) || userDistNo.startsWith(fileList.getBelongDistNo()))) {
                return true;
            } else {
                return false;
            }
        }).collect(Collectors.toList());

        if(CollectionUtils.isEmpty(collects)){
            throw new RuntimeException("未配置基础表！");
        }
        return collects;
    }

    /**
     * 获取多个FileList(只有单独的filelist表 不需要关联其他的内容)
     *
     * @param typeCode  模式
     * @param tableType 表类型 基本表/分析表
     * @param years     年份
     * @param months    月份
     * @return 多个FileList
     */
    @Override
    public List<FileList> listFileListsOnly(String typeCode, String tableType, Integer years, Integer months) {
        List<FileList> fileListList = fileListRepository.findFileList(typeCode, tableType, years);
        List<FileList> fileLists = fileListList.stream().filter(fileList -> {
            String numberOfMonthString = fileList.getTypeDescription();
            String[] monthsArray = (Objects.isNull(numberOfMonthString) || StringUtils.isEmpty(numberOfMonthString)) ? null : numberOfMonthString.split(",");
            if (Objects.nonNull(monthsArray)) {
                for (String monthsString : monthsArray) {
                    if (monthsString.equals(String.valueOf(months))) {
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }

        }).collect(Collectors.toList());


        //查询对应的单位类型
        for (FileList fileList : fileLists) {
            Integer fileListYears = fileList.getYears();
            String fileListTableName = fileList.getTableName();

            List<FileItemLink> fileItemLinks = fileItemLinkRepository.findFileItemLink(fileListYears, fileListTableName);
            if (!CollectionUtils.isEmpty(fileItemLinks)) {
                fileList.setFileItemLink(fileItemLinks.get(0));
                String prjType = fileItemLinks.get(0).getPrjType();
                List<FileItemLinkEx> fileItemLinkExs = fileItemLinkExRepository.findFileItemLinkEx(fileListYears, fileListTableName, prjType);
                fileList.setFileItemLinkExs(fileItemLinkExs);
                List<FileItemLinkExEx> fileItemLinkExExs = fileItemLinkExExRepository.findFileItemLinkExEx(fileListYears, fileListTableName, prjType);
                Map<String, List<FileItemLinkExEx>> fileItemLinkExExsMap = fileItemLinkExExs.stream().collect(Collectors.groupingBy(FileItemLinkExEx::getPrjType));
                fileList.setFileItemLinkExExsMap(fileItemLinkExExsMap);
            }
        }

        return fileLists;
    }



    /**
     * 获取多个FileList(不区分表类型TableType, 并且查所有)
     *
     * @param typeCode 模式
     * @param years    年份
     * @param months   月份
     * @return 多个FileList
     */
    @Override
    public List<FileList> listFileLists(String typeCode, Integer years, Integer months) {

        //查询出所有filelist关联的配置内容
        List<FileList> fileLists = fileListRepository.findFileList(typeCode, years);
        for (int i = 0; i < fileLists.size(); i++) {
            FileList fileList = fileLists.get(i);
            Integer fileListYears = fileList.getYears();
            String fileListTableName = fileList.getTableName();
            String tableType = fileList.getTableType();

            //设置fileList 所有的fileItem
            if (FileListConstant.FILE_LIST_TABLE_TYPE_ANALYSIS.equals(tableType)) {
                fileList.setFileItems(fileItemRepository.findFileItem(fileListYears, "ans_" + fileList.getAnsNo()));
            } else {
                fileList.setFileItems(fileItemRepository.findFileItem(fileListYears, fileListTableName));
            }

        }
        
        return fileLists;
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

            if(j>frozen){
                continue;
            }

            HtmlFileItem htmlFileItem = covertHTMLFileItem(fileItem);
            htmlFileItem.setRowspan(titleTrSize);
            htmlFileItem.setAlign(getAlign(htmlFileItem, htmlFileItem.getFType()));
            //htmlFileItem.setSortable(true);


            htmlFileItem.setTitle(htmlFileItem.getTitle());
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
            if(i<=frozen){
                continue;
            }
            HtmlFileItem htmlFileItem = covertHTMLFileItem(fileItem);

            String[] arrayFieldDis = org.apache.commons.lang.StringUtils.split(htmlFileItem.getFieldDis(), "|");
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
                            if(priorColumnHtmlFileItem.getCount()>columnGroupSize) {
                                columnGroupSize = priorColumnHtmlFileItem.getCount();
                            }
                        } else {
                            break;
                        }
                    }
                    for(int k=j+1;k<htmlFileItems.size();k++) {
                        HtmlFileItem nextColumnHtmlFileItem = htmlFileItems.get(k);

                        if(isColumnSameText(rvalue, i, j, k)) {
                            colspan++;

                            if(nextColumnHtmlFileItem.getCount()>columnGroupSize){
                                columnGroupSize = nextColumnHtmlFileItem.getCount();
                            }
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

                        while (fontRowSize != 0 && org.apache.commons.lang.StringUtils.trimToEmpty(title).length()>fontRowSize) {
                            sb.append(title.substring(0, fontRowSize));

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
//        List<HtmlFileItem> notExistsFileItems = getHTMLFileItemsByNotExists(titleTrSize, years, tableName);
//        for(HtmlFileItem htmlFileItem:notExistsFileItems) {
//            rvalue.get(1).add(htmlFileItem);
//        }


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
        List<Map<String, Object>> raData = this.funcontrast(2);
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
            fileItem.setFieldName(org.apache.commons.lang3.StringUtils.upperCase((String)dataSub.get(0)));
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

            int size = org.apache.commons.lang3.StringUtils.countMatches(fieldDis, "|");
            if(size>titleTrSize){
                titleTrSize = size;
            }
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

            if(!org.apache.commons.lang3.StringUtils.equals(title, nextTitle)) {
                rvalue = false;
                break;
            }
        }
        return rvalue;
    }


    public List<Map<String, Object>> funcontrast(int ttype) {
        List<Map<String, Object>> rvalue = null;
        String sql = "select mysql_fun, sql_fun, valuedec, sqlstr from fun_contrast where visible=1 and ttype=? order by disid";
        rvalue = jdbcTemplatePrimary.queryForList(sql, new Object[]{ttype});
        return rvalue;
    }


}
