package com.spower.gulimall.search.service;

import com.spower.common.vo.search.SearchParam;
import com.spower.common.vo.search.SearchResult;

/**
 * 商城检索
 * @Author: jackc
 * @Date: 2022/04/03 23:51
 */
public interface MallSearchService {

    /**
     * 检索商品
     * @param param 检索所有参数
     * @returns 返回检索结果，里面包含页面所有信息
     */
    SearchResult search(SearchParam param);
}