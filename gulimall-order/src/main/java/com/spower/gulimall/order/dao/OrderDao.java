package com.spower.gulimall.order.dao;

import com.spower.gulimall.order.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 19:23:18
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {
	
}
