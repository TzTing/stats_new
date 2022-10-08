package com.bright.stats.service.impl;

import com.bright.common.util.SecurityUtil;
import com.bright.stats.manager.NavigateManager;
import com.bright.stats.manager.TableTypeManager;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.service.NavigateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 16:20
 * @Description
 */
@Service
@RequiredArgsConstructor
public class NavigateServiceImpl implements NavigateService {

    private final TableTypeManager tableTypeManager;
    private final NavigateManager navigateManager;

    @Override
    public List<TableType> listTableTypes() {
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        return tableTypes;
    }

    @Override
    public void selectMode(Integer tableTypeId) {
        TableType tableType = tableTypeManager.getTableTypeById(tableTypeId);
        List<TableType> tableTypes = tableTypeManager.listTableTypes();
        if(tableTypes.indexOf(tableType) == -1){
            throw new RuntimeException("当前选择的模式不存在！");
        }
        User loginUser = SecurityUtil.getLoginUser();
//        loginUser.setTableType(tableType);
//        List<Dist> dists = new ArrayList<>();
//        loginUser.setDists(dists);
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
//        securityUser.setUser(loginUser);
//
//        Authentication newAuthentication = new UsernamePasswordAuthenticationToken(securityUser, authentication.getCredentials());
//        SecurityContextHolder.getContext().setAuthentication(newAuthentication);

    }


}
