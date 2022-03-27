package com.spower.common.vo.order;

import com.spower.common.entity.order.OrderEntity;
import lombok.Data;

/**
 * 提交订单返回结果
 * @author: wan
 */
@Data
public class SubmitOrderResponseVO {
    private OrderEntity order;
}
