package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.constant.FileListConstant;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.StatisticsCenterQuery;
import com.bright.stats.service.StatisticsCenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/4 11:18
 * @Description
 */
@Service
@RequiredArgsConstructor
public class StatisticsCenterServiceImpl implements StatisticsCenterService {

    private final FileListManager fileListManager;

    @Override
    public List<TableHeader> listTableHeaders(String typeCode, String tableName, Integer years, Integer months) {
        User loginUser = SecurityUtil.getLoginUser();
        String userDistNo = loginUser.getTjDistNo();
        FileList fileList = fileListManager.getFileList(typeCode, FileListConstant.FILE_LIST_TABLE_TYPE_BASE, tableName, years, months, userDistNo);
        return fileList.getTableHeaders();
    }

    @Override
    public PageResult<Map<String, Object>> listTableDataForPage(StatisticsCenterQuery statisticsCenterQuery) {
        return null;
    }
}

