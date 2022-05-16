package com.spower.gulimall.member.feign;

import com.spower.common.utils.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * @author CZQ
 */
@FeignClient("gulimall-order")
public interface OrderFeignService {

    /**
     * 分页查询当前登录用户的所有订单信息
     *
     * @param params
     * @return
     */
    @PostMapping("/order/order/listWithItem")
    R listWithItem(@RequestBody Map<String, Object> params);

}
