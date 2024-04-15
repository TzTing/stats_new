package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.stats.manager.DistManager;
import com.bright.stats.pojo.query.DistExQuery;
import com.bright.stats.pojo.vo.DistVO;
import com.bright.stats.service.DistService;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/7/15 11:23
 * @Description
 */
@Service
@RequiredArgsConstructor
public class DistServiceImpl implements DistService {

    private final DistManager distManager;

    @Override
    public List<DistVO> listDistTrees(Integer years, String userDistNo, String distNo) {
        if ("0".equals(userDistNo)) {
            userDistNo = "%";
        }
        if ("0".equals(distNo)) {
            distNo = "%";
        }

        int[] distGrades = DataConstants.distGrades;
        Set<Integer> distGradesSet = distManager.listDistGrades(years);
        List<Integer> distGIntegers = distGradesSet.stream().collect(Collectors.toList());

        //如果地区号带有‘，’ 表示用户持有多个地区的权限
        Set<DistVO> distVOS = new LinkedHashSet<>();
        for (String tempDistNo : distNo.split(",")) {

            Integer distNoLength = getDistNoLength(distGIntegers, tempDistNo);
            distVOS.addAll(distManager.listDistForList(years, userDistNo, tempDistNo, distNoLength));
        }

        return new ArrayList<>(distVOS);
    }


    /**
     *
     * @param distGIntegers 地区所有等级
     * @param distNo 需要获取等级的地区号
     * @return 返回地区等级
     */
    public Integer getDistNoLength(List<Integer> distGIntegers, String distNo){

        int i = distGIntegers.indexOf(distNo.length());
        Integer distNoLength = 0;
        if (i + 1 <= distGIntegers.size()) {
            if(i + 1 == distGIntegers.size()){
                distNoLength = distGIntegers.get(i);
            }else {
                distNoLength = distGIntegers.get(i + 1);
            }
        } else {
            throw new RuntimeException("获取地区长度集合异常！");
        }

        return distNoLength;
    }
}
