package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.RoleMain;
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
public interface RoleMainRepository extends JpaRepository<RoleMain, Integer>, JpaSpecificationExecutor<RoleMain> {

    /**
     * 查询角色
     * @param rno 角色编号
     * @return
     */
    @Query("from RoleMain where visible=true and rno=:rno")
    Set<RoleMain> findRoleMain(@Param("rno") Integer rno);
}