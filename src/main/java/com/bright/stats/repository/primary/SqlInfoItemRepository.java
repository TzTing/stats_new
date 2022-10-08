package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.SqlInfoItem;
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
public interface SqlInfoItemRepository extends JpaRepository<SqlInfoItem, Integer>, JpaSpecificationExecutor<SqlInfoItem> {

    /**
     * 查询SqlInfoItem
     * @param years 年份
     * @param modalName 方案名称
     * @return
     */
    @Query("from SqlInfoItem where disFlag='1' and years=:years and tableName=:modalName order by disId")
    List<SqlInfoItem> findSqlInfoItem(@Param("years") Integer years, @Param("modalName") String modalName);
}