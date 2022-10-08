package com.bright.stats.manager;

import com.bright.stats.pojo.dto.SummaryDTO;
import com.bright.stats.pojo.dto.TableDataDTO;
import com.bright.stats.pojo.po.primary.FileList;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.query.BaseDataQuery;
import com.bright.stats.pojo.query.ExistDataQuery;
import com.bright.stats.pojo.vo.CheckVO;
import com.bright.stats.pojo.vo.SummaryVO;

import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 9:33
 * @Description
 */
public interface BaseDataManager {

    /**
     * 按形参typeClass返回不同类型的数据
     * @param baseDataQuery 参数
     * @param isPage 是否分页
     * @param typeClass 返回值类型
     * @return 根据形参typeClass返回对应的类型
     */
    <T> T listTableData(BaseDataQuery baseDataQuery, boolean isPage, Class<T> typeClass);

    /**
     * 保存数据
     * @param tableDataDTO 参数
     */
    void saveTableData(TableDataDTO tableDataDTO);

    /**
     * 汇总
     * @param summaryDTO 汇总参数
     * @return 汇总结果
     */
    SummaryVO summary(SummaryDTO summaryDTO);

    /**
     * 稽核
     * @param tableType
     * @param fileList
     * @param ids
     * @param years
     * @param months
     * @param distNo
     * @param grade
     * @param isAllDist
     * @param isSb
     * @param isGrade
     * @return
     */
    List<CheckVO> check(TableType tableType, FileList fileList, Object[] ids, Integer years, Integer months, String distNo, Integer grade, Boolean isAllDist, Boolean isSb, Boolean isGrade);

    Map<String, Object> importExcelByTemplate(TableType tableType, Integer excelTemplateId, String rootPath, String filePath, String distNo, Integer years, Integer months);

    Map<String, Object> listLxsAndLxNames(String typeCode, String tableName, String distNo, Integer years, Integer months);

    Map<String, Object> listExistData(ExistDataQuery existDataQuery);

    Map<String, Object> getPreviousYearData(String typeCode, String tableName, Integer years, Integer months, String paramJson);
}
