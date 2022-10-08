package com.bright.stats.manager.impl;

import com.bright.stats.manager.DistExManager;
import com.bright.stats.pojo.po.primary.DistEx;
import com.bright.stats.repository.primary.DistExRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/3 11:14
 * @Description
 */
@Component
@RequiredArgsConstructor
public class DistExManagerImpl implements DistExManager {

    private final DistExRepository distExRepository;

    @Override
    public DistEx getDistEx(Integer years, String tableName, String distNo, String lxName) {
        DistEx rvalue = null;
        List<DistEx> distExs = distExRepository.findDistEx(years, tableName, distNo, lxName);
        if(distExs.size()>0) {
            rvalue = distExs.get(0);
        }

        return rvalue;
    }
}
