package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.Navigate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface NavigateRepository extends JpaRepository<Navigate, Integer>, JpaSpecificationExecutor<Navigate> {

    /**
     * 查询导航菜单
     * @return 导航菜单
     */
    @Query("from Navigate where visible=true and navNo<>'' and navNo like '01%' order by disId, navNo, id")
    List<Navigate> findNavigate();
}