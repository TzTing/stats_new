package com.bright.stats.service;

import com.bright.stats.pojo.po.primary.TableType;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 16:20
 * @Description
 */
public interface NavigateService {

    /**
     * 获取多个TableType
     * @return 多个TableType
     */
    List<TableType> listTableTypes();

    /**
     * 选择模式
     * @param tableTypeId TableTypeId
     */
    void selectMode(Integer tableTypeId);

}
