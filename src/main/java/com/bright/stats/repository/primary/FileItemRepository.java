package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.FileItem;
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
public interface FileItemRepository extends JpaRepository<FileItem, Integer>, JpaSpecificationExecutor<FileItem> {

    /**
     * 查询显示的FileItems
     * @param years
     * @param tableName
     * @return
     */
    @Query("from FileItem where years=:years and tableName=:tableName and disFlag='1' order by disId")
    List<FileItem> findFileItemByDisFlag(@Param("years") int years, @Param("tableName") String tableName);


    /**
     * 查询FileItems
     * @param years
     * @param tableName
     * @return
     */
    @Query("from FileItem where years=:years and tableName=:tableName order by disId")
    List<FileItem> findFileItem(@Param("years") int years, @Param("tableName") String tableName);
}