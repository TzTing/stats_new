package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.OperationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

/**
 * @Author txf
 * @Date 2022-07-25 15:26:03
 * @Description
 */
public interface OperationLogRepository extends JpaRepository<OperationLog, Integer>, JpaSpecificationExecutor<OperationLog> {


    @Query("from OperationLog where opeBeginDate between :lastTime and :cureTime")
    List<OperationLog> findByBetweenOpeBeginDate(@Param("lastTime") Date lastTime, @Param("cureTime") Date cureTime);
}