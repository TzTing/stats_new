package com.bright.common.util;

import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author txf
 * @Date 2022/6/2 17:12
 * @Description 排序参数转换工具类
 */
public class SortQueryUtil {

    public static Sort toSort(List<String> sorts){
        if(null == sorts || sorts.size() == 0){
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String sort : sorts) {
            Sort.Order order = null;
            if(sort.indexOf(",") == -1){
                order = Sort.Order.asc(sort);
                orders.add(order);
            }else {
                if("desc".equalsIgnoreCase(sort.substring(sort.indexOf(",")))){
                    order = Sort.Order.desc(sort.substring(sort.indexOf(",")));
                    orders.add(order);
                }else {
                    order = Sort.Order.asc(sort.substring(sort.indexOf(",")));
                    orders.add(order);
                }
            }
        }
        return Sort.by(orders);
    }
}
