package com.bright.stats.manager.impl;

import com.bright.stats.manager.NavigateManager;
import com.bright.stats.pojo.po.primary.Navigate;
import com.bright.stats.repository.primary.NavigateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 17:29
 * @Description
 */
@Component
@RequiredArgsConstructor
public class NavigateManagerImpl implements NavigateManager {

    private final NavigateRepository navigateRepository;

    @Override
    public List<Navigate> listNavigates() {
        List<Navigate> navigates = navigateRepository.findNavigate();
        return navigates;
    }
}
