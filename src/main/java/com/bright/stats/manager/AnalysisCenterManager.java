package com.bright.stats.manager;

import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.query.AnalysisCenterQuery;
import com.bright.stats.pojo.vo.SqlInfoVO;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/8/5 9:49
 * @Description
 */
public interface AnalysisCenterManager {

    /**
     * 获取分析方案列表
     * @param years 年份
     * @param typeCode 模式
     * @return 分析方案列表
     */
    List<SqlInfoVO> listAnalysisSchemes(Integer years, String typeCode);

    /**
     * 获取表头列表
     * @param years 年份
     * @param typeCode 模式
     * @param sqlNo 方案编号
     * @return 表头
     */
    List<TableHeader> listTableHeaders(Integer years, String typeCode, String sqlNo);

    /**
     * 按形参typeClass返回不同类型的数据
     * @param analysisCenterQuery 参数
     * @param isPage 是否分页
     * @param typeClass 返回值类型
     * @return 根据形参typeClass返回对应的类型
     */
    <T> T listTableData(AnalysisCenterQuery analysisCenterQuery, boolean isPage, Class<T> typeClass);
}
