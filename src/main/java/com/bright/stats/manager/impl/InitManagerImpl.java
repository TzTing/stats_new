package com.bright.stats.manager.impl;

import com.bright.stats.manager.BsConfigManager;
import com.bright.stats.manager.InitManager;
import com.bright.stats.util.Common;
import com.bright.stats.util.DataConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * @Author txf
 * @Date 2022/8/1 15:52
 * @Description
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InitManagerImpl implements InitManager {

    private final JdbcTemplate jdbcTemplatePrimary;
    private final JdbcTemplate jdbcTemplateSecond;
    private final BsConfigManager bsConfigManager;

    @PostConstruct
    private void init() {
        DataConstants.SZDB = this.getSzdb();
        DataConstants.funContrast = this.funcontrast();
        DataConstants.distGrades = this.getDistGrades(0);
        DataConstants.sysparams = this.bsConfigManager.querySysParam();
    }

    /**
     * 初始化地区级别
     *
     * @param years
     * @return
     */
    public int[] getDistGrades(int years) {
        String sql = "select LEN(distno) lendist from dist where 1=1 group by LEN(distno) order by lendist";
        sql = "select ${LEN}(distid) lendist from dist where  distid is not null group by ${LEN}(distid) order by lendist";
        sql = Common.replaceFun(sql);
        List<Map<String, Object>> data = jdbcTemplatePrimary.queryForList(sql);

        int[] rvalue = new int[data.size()];
        int i = 0;
        for (Map<String, Object> dataSub : data) {
            rvalue[i] = Integer.parseInt(dataSub.values().toArray()[0].toString());

            i++;
        }

        return rvalue;
    }


    public String getSzdb() {
        String rvalue = null;
        if (StringUtils.equalsIgnoreCase(DataConstants.SQLTYPE, "dm")) {
            if (rvalue == null) {
                rvalue = DataConstants.SZDB;
            }
        } else {
            try {
                rvalue = jdbcTemplateSecond.getDataSource().getConnection().getCatalog();
            } catch (SQLException e) {
                System.err.println("ReplaceSqlUtil.class 获取三资数据名失败。");
                e.printStackTrace();
            }
        }


        return rvalue;
    }


    public List<Map<String, Object>> funcontrast() {
        List<Map<String, Object>> rvalue = null;
        String sql = "select mysql_fun, sql_fun from fun_contrast where visible=1 and ttype=1 order by disid";
        rvalue = jdbcTemplatePrimary.queryForList(sql);
        return rvalue;
    }

}
