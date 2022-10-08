package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.RoleFunction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface RoleFunctionRepository extends JpaRepository<RoleFunction, Integer>, JpaSpecificationExecutor<RoleFunction> {

    /**
     * 查询权限
     * @param rno 角色编号
     * @return
     */
    @Query("from RoleFunction where visible=true and rno=:rno")
    Set<RoleFunction> findRoleFunction(@Param("rno") Integer rno);
}