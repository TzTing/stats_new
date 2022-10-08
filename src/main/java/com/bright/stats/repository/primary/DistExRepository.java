package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.DistEx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/25 16:11
 * @Description
 */
public interface DistExRepository extends JpaRepository<DistEx, Integer>, JpaSpecificationExecutor<DistEx> {

    /**
     * 查询单位
     * @param years
     * @param tableName
     * @param distNo
     * @param lxName
     * @return
     */
    @Query("from DistEx where years=:years and tableName=:tableName and distId=:distNo and lxName=:lxName")
    List<DistEx> findDistEx(@Param("years") Integer years, @Param("tableName") String tableName, @Param("distNo") String distNo, @Param("lxName") String lxName);
}