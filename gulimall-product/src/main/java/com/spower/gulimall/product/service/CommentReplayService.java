package com.spower.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.product.entity.CommentReplayEntity;

import java.util.Map;

/**
 * 商品评价回复关系
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 02:24:20
 */
public interface CommentReplayService extends IService<CommentReplayEntity> {

    PageUtils queryPage(Map<String, Object> params);
}
