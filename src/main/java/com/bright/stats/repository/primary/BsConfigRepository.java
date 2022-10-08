package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.BsConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @Author txf
 * @Date 2022-07-25 15:26:03
 * @Description
 */
public interface BsConfigRepository extends JpaRepository<BsConfig, Integer>, JpaSpecificationExecutor<BsConfig> {

    /**
     * 查询BsConfig配置集合
     * @return
     */
    @Query("from BsConfig where  type='层级统计' and chkFlag=true ")
    List<BsConfig> findBsConfig();

}