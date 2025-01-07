package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.LxSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p> Project: stats - LxSummaryRepository </p>
 *
 * @author Tz
 * @version 1.0.0
 * @date 2024/10/21 9:59
 * @since 1.0.0
 */
public interface LxSummaryRepository extends JpaRepository<LxSummary, Integer>, JpaSpecificationExecutor<LxSummary> {

    /**
     * 获取汇总合计的名称列表
     * @param typeCode 模式名
     * @return         汇总合计的名称列表
     */
    @Query("from LxSummary where 1 = 1 and typeCode=:typeCode")
    List<String> findSummaryNameListByTypeCode(@Param("typeCode") String typeCode);


    /**
     * 获取需要添加额外汇总的配置列表
     *
     * @param typeCode  模式名
     * @return          额外汇总的配置列表
     */
    @Query("from LxSummary where 1 = 1 and typeCode=:typeCode and visible = true")
    List<LxSummary> findLxSummaryListByTypeCode(@Param("typeCode") String typeCode);
}
