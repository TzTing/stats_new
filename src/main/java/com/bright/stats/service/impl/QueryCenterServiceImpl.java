package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.manager.QueryCenterManager;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.query.QueryCenterQuery;
import com.bright.stats.service.QueryCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
