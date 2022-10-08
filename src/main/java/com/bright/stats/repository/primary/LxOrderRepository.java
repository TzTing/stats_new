package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.LxOrder;
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
public interface LxOrderRepository extends JpaRepository<LxOrder, Integer>, JpaSpecificationExecutor<LxOrder> {

    /**
     * 获取LxOrder
     * @param years
     * @param typeCode
     * @return
     */
    @Query("from LxOrder where years=:years and typeCode=:typeCode")
    List<LxOrder> findLxOrder(@Param("years") Integer years, @Param("typeCode") String typeCode);

}