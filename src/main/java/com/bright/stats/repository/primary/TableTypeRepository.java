package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.TableType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface TableTypeRepository extends JpaRepository<TableType, Integer>, JpaSpecificationExecutor<TableType> {

    /**
     * 查询TableType
     * @return TableType集合
     */
    @Query("from TableType where visible=true order by id")
    List<TableType> findTableType();

}