package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.AnsTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * @Author txf
 * @Date 2022-07-25 15:26:03
 * @Description
 */
public interface AnsTableRepository extends JpaRepository<AnsTable, Integer>, JpaSpecificationExecutor<AnsTable> {

    /**
     * 查询AnsTable
     * @param years 年份
     * @param months 月份
     * @param ansNo
     * @return
     */
    @Query("from AnsTable where years=:years and months=:months and repNo=:ansNo order by item")
    List<AnsTable> findAnsTable(@Param("years") Integer years, @Param("months") Integer months, @Param("ansNo") Integer ansNo);
}