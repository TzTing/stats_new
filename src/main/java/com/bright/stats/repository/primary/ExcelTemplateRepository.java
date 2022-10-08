package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.ExcelTemplate;
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
public interface ExcelTemplateRepository extends JpaRepository<ExcelTemplate, Integer>, JpaSpecificationExecutor<ExcelTemplate> {

    /**
     * 查询ExcelTemplate
     * @param years 年份
     * @param typeCode 模式/表名称
     * @param username 用户名称
     * @param tableType 基础表/分析表
     * @return ExcelTemplate
     */
    @Query("from ExcelTemplate where years=:years and tableName=:typeCode and tableType=:tableType and (writer=:username or type='系统') and visible=true order by id")
    List<ExcelTemplate> findExcelTemplate(@Param("years") Integer years, @Param("typeCode") String typeCode, @Param("username") String username, @Param("tableType") String tableType);

}