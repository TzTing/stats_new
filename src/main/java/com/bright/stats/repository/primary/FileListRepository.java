package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.FileList;
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
public interface FileListRepository extends JpaRepository<FileList, Integer>, JpaSpecificationExecutor<FileList> {

    /**
     * 查询FileList
     *
     * @param typeCode
     * @param tableType
     * @param years
     * @param tableName
     * @return
     */
    @Query("from FileList where typeCode=:typeCode and tableType=:tableType and tableName=:tableName and years=:years ")
    FileList findFileList(@Param("typeCode") String typeCode, @Param("tableType") String tableType, @Param("tableName") String tableName, @Param("years") Integer years);

    /**
     * 查询FileLists
     *
     * @param typeCode
     * @param tableType
     * @param years
     * @return
     */
    @Query("from FileList where typeCode=:typeCode and tableType=:tableType and years=:years and useFlag='是' order by orderId")
    List<FileList> findFileList(@Param("typeCode") String typeCode, @Param("tableType") String tableType, @Param("years") Integer years);
}