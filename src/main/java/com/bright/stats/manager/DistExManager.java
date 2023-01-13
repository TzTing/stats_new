package com.bright.stats.manager;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.dto.UnitDataDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.primary.DistEx;
import com.bright.stats.pojo.query.DistExQuery;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/3 11:14
 * @Description
 */
public interface DistExManager {
    DistEx getDistEx(Integer years, String tableName, String distNo, String lxName);

    /**
     * 分页返回账套列表
     * @param distExQuery
     * @return
     */
    PageResult<Map<String, Object>> listDistExForPage(DistExQuery distExQuery);

    /**
     * 获取账套数据表头
     * @param tableName
     * @param years
     * @param months
     * @return
     */
    List<TableHeader> listTableHeaders(String tableName, Integer years, Integer months);

    /**
     * 地区单位的数据维护
     * @param unitDataDTO
     */
    void save(UnitDataDTO unitDataDTO);

    /**
     * 查询类型列表
     * @param years
     * @param typeCode
     * @return
     */
    List<Map<String, Object>> listLxOrder(Integer years, String typeCode);
}
