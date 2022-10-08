package com.bright.stats.manager.impl;

import com.bright.stats.manager.DistManager;
import com.bright.stats.pojo.vo.DistVO;
import com.bright.stats.repository.primary.DistRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.query.internal.NativeQueryImpl;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Author txf
 * @Date 2022/7/27 16:50
 * @Description
 */
@Component
@RequiredArgsConstructor
public class DistManagerImpl implements DistManager {

    @PersistenceContext
    private EntityManager entityManagerPrimary;

    @Autowired
    private DistManager distManager;

    private final DistRepository distRepository;

    @Override
    @Cacheable(value = "Dist", key = "#root.methodName + #years")
    public Set<Integer> listDistGrades(Integer years) {
        List<String> distNos = distRepository.findDistNoByGroup(years);
        Set<Integer> distNoLengths = distNos.stream().map(s -> s.length()).collect(Collectors.toSet());
        return distNoLengths;
    }

    @Override
    public int getMaxDistNoLength(String distNo, Integer years, Integer grades) {
        Set<Integer> listDistGrades = distManager.listDistGrades(years);
        Integer[] distGrades = listDistGrades.toArray(new Integer[listDistGrades.size()]);

        if (distNo.indexOf(",") != -1) {
            int ii = distNo.indexOf(",");
            distNo = distNo.substring(0, ii);
        }

        int grade = -1;
        if (null == distGrades || distGrades.length == 0) {
            return grade;
        }
        try {
            Integer distNoLength = distNo.length();
            if (StringUtils.isEmpty(distNo)) {
                distNoLength = 1;
            }
            for (int i = 0; i < distGrades.length; i++) {
                if (distGrades[i].equals(distNoLength)) {
                    grade = i;
                    break;
                }
            }

            if (grade != -1) {
                grade = ((grade + grades - 1) > distGrades.length - 1) ? distGrades.length : grade + grades - 1;
                if (grade == -1) {
                    grade = 0;
                }
            } else {
                grade = 0;
            }

            if (distGrades.length <= grade) {
                grade = distGrades.length - 1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return distGrades[grade];
    }

    @Override
    public List<DistVO> listDistForList(Integer years, String userDistNo, String distNo, Integer distNoLength) {
        String sql = "select id, distId as distNo, distName, distIdParent as parentDistNo, distType, case when (select COUNT(*) from dist as d where dist.distId=d.distIdParent)>=1 then 1 else 0 end as childrenExistFlag from dist where years=:years and distId like :userDistNo and distId like :distNo and len(distId) <= :distNoLength order by distId";
        Query nativeQuery = entityManagerPrimary.createNativeQuery(sql);
        nativeQuery.setParameter("years", years);
        nativeQuery.setParameter("userDistNo", userDistNo + "%");
        nativeQuery.setParameter("distNo", distNo + "%");
        nativeQuery.setParameter("distNoLength", distNoLength);
        nativeQuery.unwrap(NativeQueryImpl.class).setResultTransformer(Transformers.aliasToBean(DistVO.class));
        List<DistVO> resultList = nativeQuery.getResultList();
        return resultList;
    }
}
