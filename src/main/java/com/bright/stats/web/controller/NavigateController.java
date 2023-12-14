package com.bright.stats.web.controller;

import com.bright.common.cache.TimeExpiredPoolCache;
import com.bright.common.result.Result;
import com.bright.common.security.SecurityUser;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.dto.CreateEmptyTableDTO;
import com.bright.stats.pojo.dto.MqMessagesDTO;
import com.bright.stats.pojo.dto.ReportDTO;
import com.bright.stats.pojo.po.primary.Dist;
import com.bright.stats.pojo.po.primary.MqMessage;
import com.bright.stats.pojo.po.primary.Note;
import com.bright.stats.pojo.po.primary.TableType;
import com.bright.stats.pojo.po.second.User;
import com.bright.stats.pojo.query.MqMessagesQuery;
import com.bright.stats.pojo.vo.InteractiveVO;
import com.bright.stats.pojo.vo.InteractiveVOEx;
import com.bright.stats.pojo.vo.OnlineUserVo;
import com.bright.stats.service.NavigateService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author txf
 * @Date 2022/8/1 10:37
 * @Description
 */
@Api(tags = "导航接口")
@RestController
@RequestMapping("/navigate")
@RequiredArgsConstructor
public class NavigateController {

    private final NavigateService navigateService;

    @Autowired
    private CacheManager cacheManager;

    @ApiOperation(value = "获取模式列表")
    @GetMapping("/tableType/list")
    public Result listTableTypes(){
        List<TableType> tableTypes = navigateService.listTableTypes();
        return Result.success(tableTypes);
    }

    @ApiOperation(value = "选择模式")
    @GetMapping("/selectMode/{tableTypeId}")
    public Result selectMode(@PathVariable Integer tableTypeId, @NotNull(message = "月份不能为空") Integer years){
        navigateService.selectMode(tableTypeId, years);
        return Result.success();
    }


    @ApiOperation(value = "获取用户信息")
    @GetMapping("/userInfo")
    public Result getUserInfo(){
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        SecurityUser securityUser = (SecurityUser)principal;
        User loginUser = securityUser.getUser();
        loginUser.setPassword(null);
        return Result.success(loginUser);
    }

    @ApiOperation(value = "清除所有缓存")
    @GetMapping("/clearCache")
    public Result clearCache(){
        //清除所有缓存
        cacheManager
                .getCacheNames()
                .stream()
                .forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        return Result.success();
    }

    @ApiOperation(value = "获取在线人数")
    @GetMapping("/onlineUser")
    public Result onlineUser(HttpServletRequest request){
//        Map<String, User> onlineUsers = (Map<String, User>) request.getServletContext().getAttribute("onlineUsers");
        List<Object> onlineUsers = TimeExpiredPoolCache.getInstance().getDataPool();
//        if(CollectionUtils.isEmpty(onlineUsers)){
//            return Result.success(0);
//        }
        return Result.success(onlineUsers);
    }


    @ApiOperation(value = "获取任务消息列表")
    @PostMapping("/listMqMessages")
    public Result listMqMessages(@RequestBody MqMessagesQuery mqMessagesQuery){



        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        String username = loginUser.getUsername();

        mqMessagesQuery.setUsername(username);
        mqMessagesQuery.setTypeCode(tableType.getTableType());

        List<MqMessage> mqMessages = navigateService.listMqMessages(mqMessagesQuery);

        return Result.success(mqMessages);
    }



    @ApiOperation(value = "阅读消息")
    @PostMapping("/readMqMessages")
    public Result readMqMessages(@RequestBody MqMessagesDTO mqMessagesDTO){

        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        String username = loginUser.getUsername();
        mqMessagesDTO.setUsername(username);

        //只能读取自己的消息

        mqMessagesDTO.setTypeCode(tableType.getTableType());
        Boolean result = navigateService.readMqMessages(mqMessagesDTO);
        if(result){
            return Result.success(result);
        }
        return Result.fail("没有可读消息！");
    }


    @ApiOperation(value = "撤销操作")
    @PostMapping("/revokeMessage")
    public Result revokeMessage(@RequestBody MqMessagesDTO mqMessagesDTO){
        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        String username = loginUser.getUsername();

        mqMessagesDTO.setUsername(username);
        mqMessagesDTO.setTypeCode(tableType.getTableType());
        try{
            List<MqMessage> result = navigateService.revokeMessage(mqMessagesDTO);
            return Result.success(result);
        }catch (Exception e){
            return Result.fail(e.getMessage());
        }
    }


    @ApiOperation(value = "校验任务状态")
    @PostMapping("/validMessageTask")
    public Result validMessageTask(@RequestBody MqMessagesDTO mqMessagesDTO){
        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        String username = loginUser.getUsername();

        mqMessagesDTO.setUsername(username);
        mqMessagesDTO.setTypeCode(tableType.getTableType());
        List<Integer> result = navigateService.validMessageTask(mqMessagesDTO);
        return Result.success(result);
    }

    @ApiOperation(value = "生成空配置数据")
    @PostMapping("/createEmptyTable")
    public Result createEmptyTable(@RequestBody CreateEmptyTableDTO createEmptyTableDTO){

        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        createEmptyTableDTO.setOptType(tableType.getOptType());
        createEmptyTableDTO.setDistNo(loginUser.getTjDistNo());
        createEmptyTableDTO.setTypeCode(tableType.getTableType());

        Boolean result = navigateService.createEmptyTable(createEmptyTableDTO);
        return Result.success(result);
    }


    @ApiOperation(value = "获取消息内容列表")
    @GetMapping("/note/list")
    public Result listNotes(String keyword){

        User loginUser = SecurityUtil.getLoginUser();
        TableType tableType = loginUser.getTableType();
        List<InteractiveVOEx> notes = navigateService.listNotes(keyword, tableType.getTableType());
        return Result.success(notes);
    }

    
}
