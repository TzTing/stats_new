package com.bright.stats.manager.impl;

import com.bright.stats.manager.TableTypeManager;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.repository.primary.TableTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * @Author txf
 * @Date 2022/7/28 16:21
 * @Description
 */
@Component
@RequiredArgsConstructor
public class TableTypeManagerImpl implements TableTypeManager {

    private final TableTypeRepository tableTypeRepository;

    @Override
    @Cacheable(value = "TableType", key = "#root.methodName")
    public List<TableType> listTableTypes() {
        List<TableType> tableTypes = tableTypeRepository.findTableType();
        return tableTypes;
    }

    @Override
    public TableType getTableTypeById(Integer tableTypeId) {
        Optional<TableType> optional = tableTypeRepository.findById(tableTypeId);
        return optional.get();
    }
}
