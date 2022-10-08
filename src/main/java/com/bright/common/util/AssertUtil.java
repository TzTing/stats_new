package com.bright.common.util;

import com.bright.common.exception.ParameterException;
import com.bright.common.result.ResultEnum;

/**
 * @Author txf
 * @Date 2022/3/2 10:02
 * @Description 断言工具类
 */
public class AssertUtil {

    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new ParameterException(message);
        }

    }
    public static void notNull(Object object, ResultEnum resultEnum) {
        if (object == null) {
            throw new ParameterException(resultEnum);
        }
    }

}
