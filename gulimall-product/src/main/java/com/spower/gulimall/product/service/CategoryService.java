package com.spower.gulimall.product.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.product.entity.CategoryEntity;

import java.util.List;
import java.util.Map;

/**
 * 商品三级分类
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 02:24:21
 */
public interface CategoryService extends IService<CategoryEntity> {

    PageUtils queryPage(Map<String, Object> params);

    List<CategoryEntity> listWithTree();

    /**
     * 删除菜单由ids
     *
     * @param asList 正如列表
     */
    void removeMenuByIds(List<Long> asList);

    /**
     * 找到catelogId的完整路径
     * 【父、子、孙】
     */
    Long[] findCatelogPath(Long catelogId);

    /**
     * 级联更新
     *
     * @param category 类别
     */
    void updateCascade(CategoryEntity category);

//    /**
//     * 找到catalogId路径 [parent/child/grandchild]
//     *
//     * @param catalogId catalog id
//     * @return {@link Long[]}
//     */
//    Long[] findCatalogPath(Long catalogId);
//

//
//    /**
//     * 会使类别
//     * @return
//     */
//    List<CategoryEntity> getLevel1Categories();

//    Map<String, List<Catalog2Vo>> getCatalogJson();
}

