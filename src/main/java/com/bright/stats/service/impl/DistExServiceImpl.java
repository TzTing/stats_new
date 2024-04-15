package com.bright.stats.service.impl;

import com.bright.common.result.PageResult;
import com.bright.stats.manager.DistExManager;
import com.bright.stats.manager.DistManager;
import com.bright.stats.manager.FileItemManager;
import com.bright.stats.manager.FileListManager;
import com.bright.stats.pojo.dto.UnitDataDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.query.DistExQuery;
import com.bright.stats.pojo.vo.DistAdapterVO;
import com.bright.stats.service.DistExService;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author: Tz
 * @Date: 2022/10/24 11:18
 */
@Service
@RequiredArgsConstructor
public class DistExServiceImpl implements DistExService {

    private final FileListManager fileListManager;

    private final FileItemManager fileItemManager;

    private final DistExManager distExManager;

    private final DistManager distManager;

    /**
     * 分页返回账套列表
     *
     * @param distExQuery
     * @return
     */
    @Override
    public PageResult<Map<String, Object>> listDistExForPage(DistExQuery distExQuery) {
        return distExManager.listDistExForPage(distExQuery);
    }

    @Override
    public List<TableHeader> listTableHeaders(String tableName, Integer years, Integer months) {
        return distExManager.listTableHeaders(tableName, years, months);
    }


    @Override
    public void save(UnitDataDTO unitDataDTO) {
       try {
           distExManager.save(unitDataDTO);
       } catch (Exception e){
           e.printStackTrace();
           throw e;
       }
    }

    @Override
    public List<Map<String, Object>> listLxOrder(Integer years, String typeCode) {
        try{
            List<Map<String, Object>> listLxOrder = distExManager.listLxOrder(years, typeCode);
            return listLxOrder;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public List<DistAdapterVO> listDistTree(Integer years, String distNo, String userDistNo) {

        if ("0".equals(userDistNo)) {
            userDistNo = "%";
        }

        if ("0".equals(distNo)) {
            distNo = "%";
        }

        int[] distGrades = DataConstants.distGrades;
        Set<Integer> distGradesSet = distManager.listDistGrades(years);
        List<Integer> distGIntegers = distGradesSet.stream().collect(Collectors.toList());

        //暂时不需要判断 以后地区级别都是根据年份查询
        //DataConstants.distGrades 废弃
        /*if(distGrades.length != distGIntegers.size()){
            throw new RuntimeException("获取地区长度集合异常！");
        }*/

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

        //判断当前操作的地区是否是userDist地区或是其所属地区
        Boolean accordWith = false;
        for (String tempDistNo : userDistNo.split(",")) {
            //如果当前操作的地区是userDist的地区或下属地区 则符合条件
            if (tempDistNo.startsWith(distNo)
                    || distNo.startsWith(tempDistNo)) {
                accordWith = true;
                userDistNo = tempDistNo;
            }
        }

        if (!accordWith) {
            throw new RuntimeException("当前操作的地区没有权限！");
        }

        try {
            List<DistAdapterVO> distAdapterVOs = distManager.listAdapterDistForList(years, userDistNo, distNo, distNoLength);
            return distAdapterVOs;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }

    }
}
