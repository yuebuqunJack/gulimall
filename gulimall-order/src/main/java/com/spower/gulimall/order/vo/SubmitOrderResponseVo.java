package com.spower.gulimall.order.vo;

import com.spower.gulimall.order.entity.OrderEntity;
import lombok.Data;

/**
 * @author CZQ
 **/
@Data
public class SubmitOrderResponseVo {

    private OrderEntity order;

    /**
     * 错误状态码 0成功
     **/
    private Integer code;


}
