package com.bright.stats.manager;

import com.bright.stats.pojo.po.second.User;

/**
 * @Author txf
 * @Date 2022/7/27 14:51
 * @Description
 */
public interface UserManager {

    /**
     * 通过用户名获取用户
     * @param username
     * @return
     */
    User getUserByUsername(String username);
}
