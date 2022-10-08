package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.RuleInner;
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
public interface RuleInnerRepository extends JpaRepository<RuleInner, Integer>, JpaSpecificationExecutor<RuleInner> {

    /**
     * 查询RuleInners
     * @param years
     * @param tableName
     * @return
     */
    @Query("from RuleInner where years=:years and tableName=:tableName and useFlag=true order by orderId")
    List<RuleInner> findRuleInner(@Param("years") Integer years, @Param("tableName") String tableName);
}