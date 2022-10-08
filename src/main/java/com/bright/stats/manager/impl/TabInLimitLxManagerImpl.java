package com.bright.stats.manager.impl;

import com.bright.stats.manager.TabInLimitLxManager;
import com.bright.stats.pojo.po.primary.TabInLimitLx;
import com.bright.stats.repository.primary.TabInLimitLxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/3 9:58
 * @Description
 */
@Component
@RequiredArgsConstructor
public class TabInLimitLxManagerImpl implements TabInLimitLxManager {

    private final TabInLimitLxRepository tabInLimitLxRepository;

    @Override
    public List<TabInLimitLx> listTabInLimitLxs(Integer years, String tableName, String prjType) {
        List<TabInLimitLx> tabInLimitLxs = tabInLimitLxRepository.findTabInLimitLx(years, tableName, prjType);
        return tabInLimitLxs;
    }
}
