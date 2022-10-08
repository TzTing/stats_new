package com.bright.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author txf
 * @Date 2021/12/25 15:34
 * @Description 结果类
 */
@Data
public class Result<T> implements Serializable {

    /**
     * 返回的状态码
     */
    private String code;

    /**
     * 返回的消息
     */
    private String msg;

    /**
     * 返回的数据
     */
    private T data;


    public static <T> Result<T> success() {
        return returnResult(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), null);
    }

    public static <T> Result<T> success(ResultEnum resultEnum) {
        return returnResult(resultEnum.getCode(), resultEnum.getMsg(), null);
    }

    public static <T> Result<T> success(T data) {
        return returnResult(ResultEnum.SUCCESS.getCode(), ResultEnum.SUCCESS.getMsg(), data);
    }

    public static <T> Result<T> success(ResultEnum resultEnum, T data) {
        return returnResult(resultEnum.getCode(), resultEnum.getMsg(), data);
    }

    public static <T> Result<T> fail() {
        return returnResult(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMsg(), null);
    }

    public static <T> Result<T> fail(String msg) {
        return returnResult(ResultEnum.FAIL.getCode(), msg, null);
    }

    public static <T> Result<T> fail(ResultEnum resultEnum) {
        return returnResult(resultEnum.getCode(), resultEnum.getMsg(), null);
    }

    public static <T> Result<T> fail(T data) {
        return returnResult(ResultEnum.FAIL.getCode(), ResultEnum.FAIL.getMsg(), data);
    }

    public static <T> Result<T> fail(ResultEnum resultEnum, T data) {
        return returnResult(resultEnum.getCode(), resultEnum.getMsg(), data);
    }

    private static <T> Result<T> returnResult(String code, String msg, T data){
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMsg(msg);
        result.setData(data);
        return result;
    }
}
