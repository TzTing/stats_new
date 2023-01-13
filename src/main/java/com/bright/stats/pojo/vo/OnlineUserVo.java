package com.bright.stats.pojo.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author: Tz
 * @Date: 2022/11/07 16:24
 */
@Data
public class OnlineUserVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String username;
    private String roleName;
    private String distName;
    private String loginIp;
    private Date loginTime;
    private String browser;

}
