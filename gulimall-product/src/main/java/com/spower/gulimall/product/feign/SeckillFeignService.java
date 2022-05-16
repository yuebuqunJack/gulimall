package com.spower.gulimall.product.feign;

import com.spower.common.utils.R;
import com.spower.gulimall.product.fallback.SeckillFeignServiceFallBack;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


/**
 * @author CZQ
 */
@FeignClient(value = "gulimall-seckill-server", fallback = SeckillFeignServiceFallBack.class,
        configuration = SeckillFeignServiceFallBack.class)
public interface SeckillFeignService {

    /**
     * 根据skuId查询商品是否参加秒杀活动
     */
    @GetMapping(value = "/sku/seckill/{skuId}")
    R getSkuSeckilInfo(@PathVariable("skuId") Long skuId);

}
