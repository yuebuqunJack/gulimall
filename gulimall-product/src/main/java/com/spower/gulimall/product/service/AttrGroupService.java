package com.spower.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.product.entity.AttrGroupEntity;
import com.spower.gulimall.product.vo.AttrGroupWithAttrsVo;
import com.spower.gulimall.product.vo.SpuItemAttrGroupVo;

import java.util.List;
import java.util.Map;

/**
 * 属性分组
 *
 * @author CZQ
 */
public interface AttrGroupService extends IService<AttrGroupEntity> {

    PageUtils queryPage(Map<String, Object> params);

    PageUtils queryPage(Map<String, Object> params, Long catelogId);

    List<AttrGroupWithAttrsVo> getAttrGroupWithAttrsByCatelogId(Long catelogId);

    List<SpuItemAttrGroupVo> getAttrGroupWithAttrsBySpuId(Long spuId, Long catalogId);
}


