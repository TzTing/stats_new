package com.bright.common.handler;//package com.bright.common.handler;
//
//import com.bright.common.result.Result;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.ResponseBody;
//
///**
// * @Author txf
// * @Date 2022/6/20 9:43
// * @Description 全局异常处理程序
// */
//@ControllerAdvice
//public class GlobalExceptionHandler {
//
//    @ResponseBody
//    @ExceptionHandler(value = Exception.class)
//    public Result errorHandler(Exception e) {
//        return Result.fail(e.getMessage());
//    }
//}
