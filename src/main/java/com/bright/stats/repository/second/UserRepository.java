package com.bright.stats.repository.second;

import com.bright.stats.pojo.po.second.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface UserRepository extends JpaRepository<User, Integer>, JpaSpecificationExecutor<User> {
}