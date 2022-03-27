package com.spower.gulimall.search.service;

import com.spower.common.vo.search.SearchParam;
import com.spower.common.vo.search.SearchResult;

/**
 * 商城检索
 * @Author: wanzenghui
 * @Date: 2021/11/10 23:51
 */
public interface MallSearchService {

    /**
     * 检索商品
     */
    SearchResult search(SearchParam param);
}