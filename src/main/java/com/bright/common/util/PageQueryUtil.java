package com.bright.common.util;

import com.bright.common.pojo.query.PageQuery;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/24 10:01
 * @Description 分页参数转换工具类
 */
public class PageQueryUtil {

    public static Pageable toPageable(PageQuery pageQuery){
        Pageable pageable = null;
        if(CollectionUtils.isEmpty(pageQuery.getSorts())){
            pageable = PageRequest.of(pageQuery.getPageNumber(), pageQuery.getPageSize());
        }else {
            pageable = PageRequest.of(pageQuery.getPageNumber(), pageQuery.getPageSize(), SortQueryUtil.toSort(pageQuery.getSorts()));
        }
        return pageable;
    }

    public static Pageable toPageable(Integer pageNumber, Integer pageSize, List<String> sorts){
        Pageable pageable = null;
        if(CollectionUtils.isEmpty(sorts)){
            pageable = PageRequest.of(pageNumber, pageSize);
        }else {
            pageable = PageRequest.of(pageNumber, pageSize, SortQueryUtil.toSort(sorts));
        }
        return pageable;
    }
}
