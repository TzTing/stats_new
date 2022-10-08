package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.FileItemLinkEx;
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
public interface FileItemLinkExRepository extends JpaRepository<FileItemLinkEx, Integer>, JpaSpecificationExecutor<FileItemLinkEx> {

    /**
     * 查询FileItemLinkExs
     * @param years
     * @param tableName
     * @param prjType
     * @return
     */
    @Query("from FileItemLinkEx where years=:years and tableName=:tableName and prjType=:prjType order by id")
    List<FileItemLinkEx> findFileItemLinkEx(@Param("years") Integer years, @Param("tableName") String tableName, @Param("prjType") String prjType);

}