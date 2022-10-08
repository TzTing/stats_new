package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.TemplatesParam;
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
public interface TemplatesParamRepository extends JpaRepository<TemplatesParam, Integer>, JpaSpecificationExecutor<TemplatesParam> {

    /**
     * 查询模板参数集合
     * @param templateName
     * @param skey
     * @return
     */
    @Query("from TemplatesParam where visible=true and templateName=:templateName and skey=:skey order by orderId")
    List<TemplatesParam> findTemplatesParam(@Param("templateName") String templateName, @Param("skey") String skey);
}