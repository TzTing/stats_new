package com.bright.stats.manager.impl;

import com.bright.stats.manager.LxSummaryManager;
import com.bright.stats.pojo.po.primary.LxSummary;
import com.bright.stats.repository.primary.LxSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * <p> Project: stats - LxSummaryManagerImpl </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/10/21 10:01
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
public class LxSummaryManagerImpl implements LxSummaryManager {

    private final LxSummaryRepository lxSummaryRepository;

    /**
     * 获取汇总合计的名称列表
     *
     * @param typeCode  模式名
     * @return          汇总合计的名称列表
     */
    @Override
    public List<String> summaryNameList(String typeCode) {
        return this.lxSummaryRepository.findSummaryNameListByTypeCode(typeCode);
    }

    /**
     * 获取需要添加额外汇总的配置列表
     *
     * @param typeCode  模式名
     * @return          额外汇总的配置列表
     */
    @Override
    public List<LxSummary> findLxSummaryListByTypeCode(String typeCode) {
        return this.lxSummaryRepository.findLxSummaryListByTypeCode(typeCode);
    }
}
