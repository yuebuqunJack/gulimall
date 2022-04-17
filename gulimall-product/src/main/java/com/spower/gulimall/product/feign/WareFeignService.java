package com.spower.gulimall.product.feign;

import com.spower.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * @author CZQ
 */
@FeignClient("gulimall-ware")
public interface WareFeignService {

    /**
     * 查询sku是否有库存
     */
    @PostMapping("/ware/waresku/hasStock")  //ware/waresku/hasStock
    R getSkusHasStock(@RequestBody List<Long> skuIds);
}
