package com.spower.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.product.entity.SkuSaleAttrValueEntity;
import com.spower.gulimall.product.vo.SkuItemSaleAttrVo;

import java.util.List;
import java.util.Map;
/**
 * sku销售属性&值
 *
 * @author CZQ
 */
public interface SkuSaleAttrValueService extends IService<SkuSaleAttrValueEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<SkuItemSaleAttrVo> getSaleAttrBySpuId(Long spuId);

    List<String> getSkuSaleAttrValuesAsStringList(Long skuId);
}

