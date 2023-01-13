package com.bright.stats.manager.impl;

import com.bright.common.result.PageResult;
import com.bright.common.util.PageQueryUtil;
import com.bright.stats.manager.DistManager;
import com.bright.stats.manager.UploadBaseManager;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.query.UploadBaseQuery;
import com.bright.stats.repository.primary.UploadBaseRepository;
import lombok.RequiredArgsConstructor;
import lombok.var;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/7/28 14:53
 * @Description
 */
@Component
@RequiredArgsConstructor
public class UploadBaseManagerImpl implements UploadBaseManager {

    private final UploadBaseRepository uploadBaseRepository;
    private final DistManager distManager;

    @Override
    public PageResult<UploadBase> listUploadBaseForPage(UploadBaseQuery uploadBaseQuery) {
        Integer years = uploadBaseQuery.getYears();
        Integer months = uploadBaseQuery.getMonths();
        String distNo = uploadBaseQuery.getDistNo();
        String typeCode = uploadBaseQuery.getTypeCode();
        String userDistNo = uploadBaseQuery.getUserDistNo();
        Integer pageNumber = uploadBaseQuery.getPageNumber();
        Integer pageSize = uploadBaseQuery.getPageSize();
        List<String> sorts = uploadBaseQuery.getSorts();

        if("0".equals(distNo)){
            distNo = "%";
        }
        if("0".equals(userDistNo)){
            userDistNo = "%";
        }

        int distLen = distNo.length();

        //只查询当前级别和下一级地区的数据
        List<Integer> allGrade = distManager.getDistAllGrade();
        int distIndex = allGrade.indexOf(distLen);
        if(distIndex >= 0 && distIndex + 1 < allGrade.size()){
            distLen = allGrade.get(distIndex + 1);
        } else {
            distLen = allGrade.get(distIndex);
        }

        Pageable pageable = PageQueryUtil.toPageable(pageNumber, pageSize, sorts);
//        Page<UploadBase> page = uploadBaseRepository.findUploadBase(years, typeCode, distNo, userDistNo, pageable);

        Page<UploadBase> page = uploadBaseRepository.findUploadBase(years, typeCode, distNo, userDistNo, distLen, pageable);
        return PageResult.of(page.getTotalElements(), page.getContent());
    }

    /**
     * 根据id查询上报或退回单位
     *
     * @param id
     * @return
     */
    @Override
    public UploadBase findById(Integer id) {
        return uploadBaseRepository.findById(id).get();
    }

    /**
     * 根据地区编号和类型查询出所有上报数据单位
     * @param years 上报单位年份
     * @param months 上报单位月份
     * @param distNo 上报单位地区号
     * @param tableType 模式类型
     * @return
     */
    @Override
    public List<UploadBase> findReportByDistAndTableType(Integer years, Integer months, String distNo, String tableType) {
        List<UploadBase> uploadBaseList = uploadBaseRepository.findReportByDistAndTableType(years, months, distNo, tableType);
        return uploadBaseList;
    }
}
