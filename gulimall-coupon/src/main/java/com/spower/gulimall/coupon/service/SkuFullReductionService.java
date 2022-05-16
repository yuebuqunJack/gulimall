package com.spower.gulimall.coupon.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.to.SkuReductionTo;
import com.spower.common.to.product.SkuReductionTO;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.coupon.entity.SkuFullReductionEntity;

import java.util.Map;

/**
 * 商品满减信息
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 16:07:41
 */
public interface SkuFullReductionService extends IService<SkuFullReductionEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void saveSkuReduction(SkuReductionTO skuReductionTO);
}

