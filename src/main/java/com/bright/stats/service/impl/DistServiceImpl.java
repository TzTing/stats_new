package com.bright.stats.service.impl;

import com.bright.stats.manager.DistManager;
import com.bright.stats.pojo.vo.DistVO;
import com.bright.stats.service.DistService;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
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

        if(distGrades.length != distGIntegers.size()){
            throw new RuntimeException("获取地区长度集合异常！");
        }

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
        List<DistVO> distVOS = distManager.listDistForList(years, userDistNo, distNo, distNoLength);
        return distVOS;
    }
}
