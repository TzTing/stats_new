package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.SqlInfo;
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
public interface SqlInfoRepository extends JpaRepository<SqlInfo, Integer>, JpaSpecificationExecutor<SqlInfo> {

    /**
     * 查询SqlInfo
     * @param years 年份
     * @param typeCode 模式
     * @return
     */
    @Query("from SqlInfo where pubFlag=true and years=:years and tableType=:typeCode order by orderId, id")
    List<SqlInfo> findSqlInfo(@Param("years") Integer years, @Param("typeCode") String typeCode);
}