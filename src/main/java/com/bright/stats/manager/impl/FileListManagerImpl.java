package com.bright.stats.manager.impl;

import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.*;
import com.bright.stats.repository.primary.*;
import com.bright.stats.util.TableHeaderUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
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

    @Override
    public FileList getFileList(String typeCode, String tableType, String tableName, Integer years, Integer months, String userDistNo) {
        List<FileList> fileLists = fileListManager.listFileLists(typeCode, tableType, years, months, userDistNo);
        Optional<FileList> optionalFileList = fileLists.stream().filter(fileList -> Objects.equals(fileList.getTableName(), tableName)).findFirst();
        if(!optionalFileList.isPresent()){
            throw new RuntimeException("未配置基础表！");
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
            fileItemList.forEach(fileItem -> {
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

            fileList.setTableHeaders(collect);

            fileList.setFileItems(fileItems);
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


}
