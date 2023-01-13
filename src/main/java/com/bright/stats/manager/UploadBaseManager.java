package com.bright.stats.manager;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.query.UploadBaseQuery;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 14:52
 * @Description
 */
public interface UploadBaseManager {

    /**
     * 分页获取上报单位数据
     * @param uploadBaseQuery 参数
     * @return 上报单位数据
     */
    PageResult<UploadBase> listUploadBaseForPage(UploadBaseQuery uploadBaseQuery);

    /**
     * 根据id查询上报或退回单位
     * @param id
     * @return
     */
    UploadBase findById(Integer id);

    /**
     * 根据地区编号和类型查询出所有上报数据单位
     * @param years 上报单位年份
     * @param months 上报单位月份
     * @param distNo 上报单位地区号
     * @param tableType 模式类型
     * @return
     */
    List<UploadBase> findReportByDistAndTableType(Integer years, Integer months, String distNo, String tableType);
}
