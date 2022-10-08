package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.DataProcessNew;
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
public interface DataProcessNewRepository extends JpaRepository<DataProcessNew, Integer>, JpaSpecificationExecutor<DataProcessNew> {

    /**
     * 获取多个DataProcessNew配置
     * @param keyword 待办事项_上报/待办事项_退回上报
     * @param tableType 模式
     * @param priorEndDisId
     * @return 多个DataProcessNew配置
     */
    @Query("from DataProcessNew where visible=true and disId>:priorEndDisId and keyword=:keyword and (tableType='系统' or tableType=:tableType) order by keyword,disId")
    List<DataProcessNew> findDataProcessNew(@Param("keyword") String keyword, @Param("tableType") String tableType, @Param("priorEndDisId") Integer priorEndDisId);
}