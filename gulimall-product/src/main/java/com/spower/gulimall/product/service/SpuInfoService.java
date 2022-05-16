package com.spower.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.product.entity.SpuInfoEntity;
import com.spower.gulimall.product.vo.SpuSaveVo;

import java.util.Map;
public interface SpuInfoService extends IService<SpuInfoEntity> {

    PageUtils queryPage(Map<String, Object> params);

    void savesupInfo(SpuSaveVo vo);

    void saveBaseSpuInfo(SpuInfoEntity spuInfoEntity);

    PageUtils queryPageByCondtion(Map<String, Object> params);

    /**
     * 商品上架
     * @param spuId
     */
    void up(Long spuId);

    /**
     * 根据skuId查询spu的信息
     * @param skuId
     * @return
     */
    SpuInfoEntity getSpuInfoBySkuId(Long skuId);
}

