package com.bright.common.util;

import com.bright.common.security.SecurityUser;
import com.bright.stats.pojo.po.primary.Dist;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/27 15:47
 * @Description
 */
public class SecurityUtil {

    public static User getLoginUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        SecurityUser securityUser = null;
        if ("anonymousUser".equals(principal.toString())) {
            securityUser = new SecurityUser();
            User user = new User();
            user.setUsername("超级用户");
            user.setTjDistNo("0102");
            List<Dist> dists = new ArrayList<>();
            user.setDistNo("01");
            user.setDistName("广州市");
            user.setDists(dists);
            TableType tableType = new TableType();
            tableType.setTableType("年报(政改处)");
            tableType.setOptType(1);
            user.setTableType(tableType);
            securityUser.setUser(user);

        } else {
            securityUser = (SecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        }

        return securityUser.getUser();
    }
}
