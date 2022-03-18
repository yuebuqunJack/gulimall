package com.spower.gulimall.order.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.order.entity.OrderItemEntity;

import java.util.Map;

/**
 * 订单项信息
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 19:23:18
 */
public interface OrderItemService extends IService<OrderItemEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

