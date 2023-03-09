package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: Tz
 * @Date: 2023/02/06 10:17
 */

@Data
public class InteractiveVOEx extends InteractiveVO  implements Serializable {

    private static final long serialVersionUID = 1L;

    private String windowsParam;

    private String windowsUrl;

    private String messageType;

}
