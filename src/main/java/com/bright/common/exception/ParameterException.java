package com.bright.common.exception;

import com.bright.common.result.ResultEnum;
import lombok.Getter;

/**
 * @Author txf
 * @Date 2022/3/2 9:16
 * @Description 参数异常类
 */
@Getter
public class ParameterException extends RuntimeException {

    private ResultEnum resultEnum;

    public ParameterException() {
        super();
    }

    public ParameterException(String message) {
        super(message);
    }

    public ParameterException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.resultEnum = resultEnum;
    }
}
