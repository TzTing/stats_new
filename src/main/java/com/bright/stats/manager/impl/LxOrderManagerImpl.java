package com.bright.stats.manager.impl;

import com.bright.stats.manager.LxOrderManager;
import com.bright.stats.pojo.po.primary.LxOrder;
import com.bright.stats.repository.primary.LxOrderRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/3 10:43
 * @Description
 */
@Component
@RequiredArgsConstructor
public class LxOrderManagerImpl implements LxOrderManager {

    private final LxOrderRepository lxOrderRepository;

    @Override
    public LxOrder getLxOrder(Integer years, String typeCode, String lx) {
        LxOrder rvalue = null;
        List<LxOrder> lxOrders = lxOrderRepository.findLxOrder(years, typeCode);
        for(LxOrder lxOrder:lxOrders) {
            if(StringUtils.equalsIgnoreCase(lxOrder.getLx(), lx)) {
                rvalue = lxOrder;
                break;
            }
        }
        return rvalue;
    }
}
