package com.bright.stats.manager;

import com.bright.common.result.PageResult;
import com.bright.stats.pojo.po.primary.UploadBase;
import com.bright.stats.pojo.query.UploadBaseQuery;

/**
 * @Author txf
 * @Date 2022/7/28 14:52
 * @Description
 */
public interface UploadBaseManager {

    /**
     * 分页获取上报单位数据
     * @param uploadBaseQuery 参数
     * @return 上报单位数据
     */
    PageResult<UploadBase> listUploadBaseForPage(UploadBaseQuery uploadBaseQuery);
}
