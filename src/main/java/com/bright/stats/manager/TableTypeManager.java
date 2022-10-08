package com.bright.stats.manager;

import com.bright.stats.pojo.po.primary.TableType;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 16:21
 * @Description
 */
public interface TableTypeManager {

    /**
     * 获取多个TableType
     * @return 多个TableType
     */
    List<TableType> listTableTypes();

    /**
     * 按id获取TableType
     * @param tableTypeId id
     * @return TableType
     */
    TableType getTableTypeById(Integer tableTypeId);
}
