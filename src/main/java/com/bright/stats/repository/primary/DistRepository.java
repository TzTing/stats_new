package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.Dist;
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
public interface DistRepository extends JpaRepository<Dist, Integer>, JpaSpecificationExecutor<Dist> {

    /**
     * 分组查询地区编号集合
     * @param years 年份
     * @return
     */
    @Query("select distId from Dist where years=:years group by distId")
    List<String> findDistNoByGroup(@Param("years") Integer years);
}