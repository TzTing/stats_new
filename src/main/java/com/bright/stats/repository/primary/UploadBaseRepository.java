package com.bright.stats.repository.primary;

import com.bright.stats.pojo.po.primary.UploadBase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * @Author txf
 * @Date 2022/6/22 16:19
 * @Description
 */
public interface UploadBaseRepository extends JpaRepository<UploadBase, Integer>, JpaSpecificationExecutor<UploadBase> {

    /**
     * 分页查询
     * @param years 年份
     * @param typeCode 模式
     * @param distNo 地区编号
     * @param userDistNo 用户关联地区编号
     * @param pageable 分页查询参数
     * @return 上报单位数据
     */
    @Query("from UploadBase u where u.years=:years and u.tableType=:typeCode and u.distNo like :distNo% and u.distNo like :userDistNo% order by u.distNo")
    Page<UploadBase> findUploadBase(@Param("years") Integer years, @Param("typeCode") String typeCode, @Param("distNo") String distNo, @Param("userDistNo") String userDistNo, Pageable pageable);

}