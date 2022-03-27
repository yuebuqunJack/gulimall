package com.spower.gulimall.search.service;

import com.spower.common.to.es.SkuEsModel;

import java.io.IOException;
import java.util.List;

/**
 * 数据保存
 */
public interface ProductSaveService {

    /**
     * 上架商品
     * @return 是否成功
     * @throws IOException
     */
    boolean productStatusUp(List<SkuEsModel> skuEsModels) throws IOException;

}
