package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.FileItemLink;
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
public interface FileItemLinkRepository extends JpaRepository<FileItemLink, Integer>, JpaSpecificationExecutor<FileItemLink> {

    /**
     * 查询FileItemLinks
     * @param years
     * @param tableName
     * @return
     */
    @Query("from FileItemLink where years=:years and tableName=:tableName order by id")
    List<FileItemLink> findFileItemLink(@Param("years") Integer years, @Param("tableName") String tableName);
}