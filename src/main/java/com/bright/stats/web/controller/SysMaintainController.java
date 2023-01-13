package com.bright.stats.web.controller;

import com.bright.common.result.PageResult;
import com.bright.common.result.Result;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.UnitDataDTO;
import com.bright.stats.pojo.model.TableHeader;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.DistExQuery;
import com.bright.stats.pojo.vo.DistAdapterVO;
import com.bright.stats.pojo.vo.DistVO;
import com.bright.stats.service.DistExService;
import com.bright.stats.service.DistService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @Author Tz
 * @Date 2022/10/24 10:31
 * @Description
 */
@Api(tags = "系统维护")
@RestController
@RequestMapping("/sysMaintain")
@RequiredArgsConstructor
public class SysMaintainController {

    private final DistExService distExService;

    @ApiOperation(value = "获取账套数据")
    @PostMapping("/unit/page")
    public Result listDistTrees(@RequestBody DistExQuery distExQuery){
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();
        distExQuery.setUserDistNo(userDistNo);
        distExQuery.setTypeCode(typeCode);

        PageResult<Map<String, Object>> pageResult = distExService.listDistExForPage(distExQuery);
        return Result.success(pageResult);
    }


    @ApiOperation(value = "获取账套数据表头")
    @GetMapping("/unit/tableHeader")
    public Result listTableHeaders(String tableName, Integer years, Integer months){
        String userDistNo = "";
        List<TableHeader> tableHeaders = distExService.listTableHeaders(tableName, years, months);
        return Result.success(tableHeaders);
    }


    @PreAuthorize("hasAnyAuthority('sysMaintain:maintenanceData')")
    @ApiOperation(value = "保存账套数据")
    @PostMapping("/unit/save")
    public Result save(@RequestBody UnitDataDTO unitDataDTO){
        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();

        unitDataDTO.setTypeCode(typeCode);
        unitDataDTO.setUserDistNo(userDistNo);

        try{
            distExService.save(unitDataDTO);
        } catch (Exception e){
            return Result.fail(e.getMessage());
        }
        return Result.success();
    }


    @ApiOperation(value = "获取单位类型")
    @GetMapping("/unit/lxOrder")
    public Result listLxOrder(Integer years){

        User loginUser = SecurityUtil.getLoginUser();
        String typeCode = loginUser.getTableType().getTableType();
        String userDistNo = loginUser.getTjDistNo();

        try{
            List<Map<String, Object>> lxOrders = distExService.listLxOrder(years, typeCode);
            return Result.success(lxOrders);
        } catch (Exception e){
            return Result.fail("获取失败！");
        }

    }

    @ApiOperation(value = "获取数树形结构地区适配")
    @GetMapping("/adapter/distTree")
    public Result listDistTree(Integer years, String distNo){

        User loginUser = SecurityUtil.getLoginUser();
        String userDistNo = loginUser.getTjDistNo();

        try{
            List<DistAdapterVO> distAdapterVOS = distExService.listDistTree(years, distNo, userDistNo);
            return Result.success(distAdapterVOS);
        } catch (Exception e){
            return Result.fail("获取失败！");
        }

    }

}
