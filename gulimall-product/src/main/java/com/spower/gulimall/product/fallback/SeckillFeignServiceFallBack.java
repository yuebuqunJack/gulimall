package com.spower.gulimall.product.fallback;


import com.spower.common.exception.BizCodeEnume;
import com.spower.common.utils.R;
import com.spower.gulimall.product.feign.SeckillFeignService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 熔断方法的具体实现，也可以是降级方法的具体实现
 * @author CZQ
 */
@Slf4j
@Component
public class SeckillFeignServiceFallBack implements SeckillFeignService {

    @Override
    public R getSkuSeckilInfo(Long skuId) {
        log.debug("熔断方法调用...getSkuSeckilInfo，获取秒杀商品详情");
        return R.error(BizCodeEnume.TO_MANY_REQUEST.getCode(), BizCodeEnume.TO_MANY_REQUEST.getMsg());
    }

}
