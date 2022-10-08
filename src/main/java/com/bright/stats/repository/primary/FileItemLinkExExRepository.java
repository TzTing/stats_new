package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.FileItemLinkExEx;
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
public interface FileItemLinkExExRepository extends JpaRepository<FileItemLinkExEx, Integer>, JpaSpecificationExecutor<FileItemLinkExEx> {

    /**
     * 查询FileItemLinkExExs
     * @param years
     * @param tableName
     * @param prjType
     * @return
     */
    @Query("from FileItemLinkExEx where years=:years and tableName=:tableName and prjType=:prjType order by id")
    List<FileItemLinkExEx> findFileItemLinkExEx(@Param("years") Integer years, @Param("tableName") String tableName, @Param("prjType") String prjType);

}