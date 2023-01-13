package com.bright.common.config;

import com.alibaba.fastjson2.JSON;
import com.bright.common.util.SecurityUtil;
import com.bright.stats.pojo.po.primary.OperationLog;
import com.bright.stats.repository.primary.OperationLogRepository;
import com.bright.stats.util.DateUtil;
import com.bright.stats.util.SessionUnit;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author: Tz
 * @Date: 2022/10/20 0:29
 */
@Aspect
@Component
@Slf4j
public class LoggerAspect {


    @Resource
    private OperationLogRepository operationLogRepository;

    @Pointcut("execution(* org.springframework.jdbc.core.JdbcTemplate.*(..))")
    private void pointCutMethod1() {
    }

    @Pointcut("!execution(* org.springframework.jdbc.core.JdbcTemplate.q*(..))")
    private void pointCutMethod2() {
    }

    @Pointcut("execution(* com.bright.stats.service.*.*(..))")
    private void pointCutMethod3() {
    }

    @Pointcut("@annotation(org.springframework.data.jpa.repository.Query)")
    private void pointCutMethod5() {
    }




    @Around("pointCutMethod1() && pointCutMethod2()")
    public Object doAroundJdbcTemplatePoint(ProceedingJoinPoint pjp) throws Throwable {
        return doAroundJdbcTemplate(pjp);
    }

    @Around("pointCutMethod3()")
    private Object doAroundServicePoint(ProceedingJoinPoint pjp) throws Throwable {
        return doAroundService(pjp);
    }


//    @Around("pointCutMethod5()")
    public Object doAroundControllerPoint(ProceedingJoinPoint pjp) throws Throwable {
        return doAroundController(pjp);
    }




    /**
     * jdbcTemplate 环绕切入处理
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object doAroundJdbcTemplate(ProceedingJoinPoint pjp) throws Throwable {

        ByteArrayOutputStream messageDetail = null;
        OperationLog operationLog = new OperationLog();
        Long beginTime = System.currentTimeMillis();

        try {

            operationLog.setOpeBeginDate(DateUtil.getCurrDate());
            operationLog.setOpeStats(true);

            //请求的参数
            String params = "";
            if (pjp.getArgs() !=  null && pjp.getArgs().length > 0) {
                for ( int i = 0; i < pjp.getArgs().length; i++) {
                    params += JSON.toJSONString(pjp.getArgs()[i]) + ";";
                }
            }

            MethodSignature signature = (MethodSignature) pjp.getSignature();

            operationLog.setOpeParam(params);
            operationLog.setOpeClass((pjp.getTarget().getClass().getName() + "." + signature.getName() + "()"));

            JdbcTemplate jdbcTemplate = null;
            if(pjp.getTarget() instanceof JdbcTemplate){
//                jdbcTemplate = (JdbcTemplate) pjp.getTarget();
            }

            //异步处理
            if(RequestContextHolder.getRequestAttributes() == null){
                String uri = "异步处理";
                operationLog.setReqUri(uri);
                operationLog.setOpeEndDate(DateUtil.getCurrDate());

            } else {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

                //请求的地址
                String uri = request.getRequestURI();

                // 请求的IP
//                String ip = request.getRemoteAddr();
                String ip = SessionUnit.realIpAddr(request);

                String reqMethod = request.getMethod();

                // 读取session中的用户
                String username = SecurityUtil.getLoginUser().getUsername();

                operationLog.setReqUri(uri);
                operationLog.setOpeType("DAO");
                operationLog.setOpeIp(ip);
                operationLog.setReqType(reqMethod);
                operationLog.setOpeUser(username);

                /*if(!Objects.isNull(jdbcTemplate)){
                    String driverDetail = jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverName()
                            + " " +  jdbcTemplate.getDataSource().getConnection().getMetaData().getDriverVersion();
                    operationLog.setOpeDatabases(driverDetail);
                }*/
            }

        }  catch (Exception ignored) {

        }

        Object object = null;
        try {
            object = pjp.proceed();
            operationLog.setOpeEndDate(DateUtil.getCurrDate());

            if(!Objects.isNull(object)){
//                operationLog.setOpeResult(object.toString());
            }

            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);

            operationLogRepository.save(operationLog);

        } catch (Throwable ignored) {
            messageDetail = new ByteArrayOutputStream();
            ignored.printStackTrace(new PrintStream(messageDetail));
            String exception = messageDetail.toString();
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLog.setOpeStats(false);
            operationLog.setOpeErrorMessage(exception);
            operationLogRepository.save(operationLog);
            messageDetail.close();
            throw ignored;
        } finally {
            if(!Objects.isNull(messageDetail)){
                messageDetail.close();
            }
        }

        return object;
    }


    /**
     * service层的环绕切入处理
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object doAroundService(ProceedingJoinPoint pjp) throws Throwable {


        ByteArrayOutputStream messageDetail = null;
        OperationLog operationLog = new OperationLog();
        String uri = "";
        Long beginTime = System.currentTimeMillis();

        try {
            operationLog.setOpeStats(true);
            operationLog.setOpeBeginDate(DateUtil.getCurrDate());

            //请求的参数
            String params = "";
            if (pjp.getArgs() !=  null && pjp.getArgs().length > 0) {
                for ( int i = 0; i < pjp.getArgs().length; i++) {
                    params += JSON.toJSONString(pjp.getArgs()[i]) + ";";
                }
            }

            operationLog.setOpeParam(params);
            operationLog.setOpeClass((pjp.getTarget().getClass().getName() + "." + pjp.getSignature().getName() + "()"));

            //异步处理
            if(RequestContextHolder.getRequestAttributes() == null){
                uri = "异步处理";
                operationLog.setReqUri(uri);
                operationLog.setOpeEndDate(DateUtil.getCurrDate());

            } else {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

                //请求的地址
                uri = request.getRequestURI();

                if(StringUtils.isNotBlank(uri) && (uri.contains("navigate/listMqMessages") || uri.contains("navigate/onlineUser"))){
                   Object proceed = pjp.proceed();
                   return proceed;
                }

                // 请求的IP
//                String ip = request.getRemoteAddr();
                String ip = SessionUnit.realIpAddr(request);


                String reqMethod = request.getMethod();

                operationLog.setReqUri(uri);
                operationLog.setOpeType("SERVICE");
                operationLog.setOpeIp(ip);
                operationLog.setReqType(reqMethod);

                // 读取session中的用户
                String username = SecurityUtil.getLoginUser().getUsername();
                operationLog.setOpeUser(username);
            }

        }  catch (Exception ex) {
            //记录本地异常日志
        }

        Object proceed = null;

        try {
            //执行增强后的方法
            proceed = pjp.proceed();

            if(!Objects.isNull(proceed)){
//                operationLog.setOpeResult(proceed.toString());
            }
            operationLog.setOpeStats(true);
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLogRepository.save(operationLog);

        } catch (Throwable throwable) {
            messageDetail = new ByteArrayOutputStream();
            throwable.printStackTrace(new PrintStream(messageDetail));
            String exception = messageDetail.toString();
            operationLog.setOpeEndDate(DateUtil.getCurrDate());
            operationLog.setOpeExecTime(System.currentTimeMillis() - beginTime);
            operationLog.setOpeStats(false);
            operationLog.setOpeErrorMessage(exception);
            operationLogRepository.save(operationLog);
            messageDetail.close();
            throw throwable;
        } finally {
            if(!Objects.isNull(messageDetail)){
                messageDetail.close();
            }
        }

        return proceed;
    }



    /**
     * controller层的环绕切入处理
     * @param pjp
     * @return
     * @throws Throwable
     */
    public Object doAroundController(ProceedingJoinPoint pjp) throws Throwable {

        OperationLog operationLog = new OperationLog();

        try {

            operationLog.setOpeBeginDate(DateUtil.getCurrDate());
            MethodSignature signature = (MethodSignature) pjp.getSignature();
            Method method = signature.getMethod();
            ApiOperation apiOperation = method.getAnnotation(ApiOperation.class);

            if (apiOperation != null) {
                String operation = apiOperation.value();
                operationLog.setOpeDescription(operation);
            }

            Api api = method.getDeclaringClass().getAnnotation(Api.class);
            if(api != null){
                operationLog.setOpeMode(Arrays.toString(api.tags()));
            }

            //请求的参数
            String params = "";
            if (pjp.getArgs() !=  null && pjp.getArgs().length > 0) {
                for ( int i = 0; i < pjp.getArgs().length; i++) {
                    params += JSON.toJSONString(pjp.getArgs()[i]) + ";";
                }
            }

            operationLog.setOpeParam(params);
            operationLog.setOpeClass((pjp.getTarget().getClass().getName() + "." + pjp.getSignature().getName() + "()"));


            //异步处理
            if(RequestContextHolder.getRequestAttributes() == null){
                String requestUri = "异步处理";
                operationLog.setReqUri(requestUri);
            } else {
                /*获取请求体内容*/
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
                String requestUri = request.getRequestURI();
                String requestMethod = request.getMethod();
//                String remoteAddr = request.getRemoteAddr();
                String remoteAddr = SessionUnit.realIpAddr(request);

                operationLog.setReqUri(requestUri);
                operationLog.setReqType(requestMethod);
                operationLog.setOpeIp(remoteAddr);
            }

        }  catch (Exception ex) {

        }


        Object proceed = null;

        try {
            //执行增强后的方法
            proceed = pjp.proceed();

            if(!Objects.isNull(proceed)){
//                operationLog.setOpeResult(proceed.toString());
            }
            operationLog.setOpeStats(true);
            operationLog.setOpeEndDate(DateUtil.getCurrDate());

        } catch (Throwable throwable) {
            operationLog.setOpeStats(false);
            operationLog.setOpeErrorMessage(throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            operationLogRepository.save(operationLog);
        }

        return proceed;

    }



}
