package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.TabInLimitLx;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface TabInLimitLxRepository extends JpaRepository<TabInLimitLx, Integer>, JpaSpecificationExecutor<TabInLimitLx> {

    /**
     * 获取TabInLimitLx列表
     * @param years 年份
     * @param tableName 表名
     * @param prjType
     * @return
     */
    @Query("from TabInLimitLx where visible=true and years=:years and tableName=:tableName and prjType=:prjType")
    List<TabInLimitLx> findTabInLimitLx(@Param("years") Integer years, @Param("tableName") String tableName, @Param("prjType") String prjType);
}