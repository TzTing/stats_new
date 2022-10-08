package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.NavigateButton;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface NavigateButtonRepository extends JpaRepository<NavigateButton, Integer>, JpaSpecificationExecutor<NavigateButton> {
}