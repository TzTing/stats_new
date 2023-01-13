package com.bright.stats.service;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.dto.UnitDataDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.query.DistExQuery;
import com.bright.stats.pojo.vo.DistAdapterVO;

import java.util.List;
import java.util.Map;

/**
 * @author: Tz
 * @Date: 2022/10/24 11:18
 */
public interface DistExService {


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

    /**
     * 获取树形结构的适配地区
     * @param years
     * @param distNo
     * @param userDistNo
     * @return
     */
    List<DistAdapterVO> listDistTree(Integer years, String distNo, String userDistNo);
}
