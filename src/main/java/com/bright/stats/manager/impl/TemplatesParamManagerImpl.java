package com.bright.stats.manager.impl;

import com.bright.stats.manager.DataProcessNewManager;
import com.bright.stats.manager.TemplatesParamManager;
import com.bright.stats.pojo.po.primary.DataProcessNew;
import com.bright.stats.pojo.po.primary.TemplatesParam;
import com.bright.stats.repository.primary.TemplatesParamRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 14:18
 * @Description
 */
@Component
@RequiredArgsConstructor
public class TemplatesParamManagerImpl implements TemplatesParamManager {

    private final TemplatesParamRepository templatesParamRepository;
    private final DataProcessNewManager dataProcessNewManager;

    @Override
    public List<TemplatesParam> listTemplatesParams(String templateName, String skey) {
        List<TemplatesParam> templatesParams = templatesParamRepository.findTemplatesParam(templateName, skey);
        return templatesParams;
    }

    @Override
    public Map<Object,Object> execDataProcess(String keyword,Map<Object,Object> params){
        if(null==keyword || StringUtils.isEmpty(keyword.trim()))return null;
        List<DataProcessNew> dataProecss=dataProcessNewManager.listDataProcessNews(keyword, params.get("tabletype").toString(), -1);
        Map<Object,Object> rvalues=dataProcessNewManager.parseProcess(dataProecss,keyword, params);
        return rvalues;
    }

}
