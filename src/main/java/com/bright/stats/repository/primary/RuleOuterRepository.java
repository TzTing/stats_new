package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.RuleOuter;
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
public interface RuleOuterRepository extends JpaRepository<RuleOuter, Integer>, JpaSpecificationExecutor<RuleOuter> {

    /**
     * 查询RuleOuters
     * @param years
     * @param tableName
     * @return
     */
    @Query("from RuleOuter where  years=:years and tableDec=:tableName and evalFlag=true order by orderId")
    List<RuleOuter> findRuleOuter(@Param("years") Integer years, @Param("tableName") String tableName);
}