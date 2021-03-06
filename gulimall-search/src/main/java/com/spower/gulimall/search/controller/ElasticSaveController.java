package com.spower.gulimall.search.controller;

import com.spower.common.exception.BizCodeEnume;
import com.spower.common.to.es.SkuEsModel;
import com.spower.common.utils.R;
import com.spower.gulimall.search.service.impl.ProductSaveServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

/**
 * es保存
 * @author CZQ
 */
@Slf4j
@RequestMapping("/search/save")
@RestController
public class ElasticSaveController {

    @Autowired
    ProductSaveServiceImpl productSaveService;

    /**
     * 上架商品
     */
    @PostMapping("/product")
    public R productStatusUp(@RequestBody List<SkuEsModel> skuEsModels) {
        // 是否执行成功
        boolean result = false;
        try {
            result = productSaveService.productStatusUp(skuEsModels);
        } catch (IOException e) {
            // es客户端连接失败
            log.error("ElasticSaveController商品上架错误：{}", e);
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
        if (!result) {
            // 执行成功
            return R.ok();
        } else {
            return R.error(BizCodeEnume.PRODUCT_UP_EXCEPTION.getCode(), BizCodeEnume.PRODUCT_UP_EXCEPTION.getMsg());
        }
    }

}
