package com.bright.stats.manager.impl;

import com.bright.common.result.PageResult;
import com.bright.common.util.PageQueryUtil;
import com.bright.stats.manager.UploadBaseManager;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.query.UploadBaseQuery;
import com.bright.stats.repository.primary.UploadBaseRepository;
import lombok.RequiredArgsConstructor;
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

        Pageable pageable = PageQueryUtil.toPageable(pageNumber, pageSize, sorts);
        Page<UploadBase> page = uploadBaseRepository.findUploadBase(years, typeCode, distNo, userDistNo, pageable);
        return PageResult.of(page.getTotalElements(), page.getContent());
    }
}
