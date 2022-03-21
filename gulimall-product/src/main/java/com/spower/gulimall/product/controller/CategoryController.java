package com.spower.gulimall.product.controller;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spower.gulimall.product.entity.CategoryEntity;
import com.spower.gulimall.product.service.CategoryService;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.R;


/**
 * 商品三级分类
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 02:24:21
 */
@RestController
@RequestMapping("product/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    /**
     * 查询出所有分类以及子分类，以树形结构组装起来列表
     */
    @RequestMapping("/list/tree")
    public R list() {

        List<CategoryEntity> entities = categoryService.listWithTree();

        return R.ok().put("data", entities);
    }

//    /**
//     * 列表
//     */
//    @RequestMapping("/list")
//    //@RequiresPermissions("product:category:list")
//    public R list(@RequestParam Map<String, Object> params){
//        PageUtils page = categoryService.queryPage(params);
//
//        return R.ok().put("page", page);
//    }


    /**
     * 信息
     */
    @RequestMapping("/info/{catId}")
    public R info(@PathVariable("catId") Long catId) {
        CategoryEntity category = categoryService.getById(catId);

        return R.ok().put("category", category);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CategoryEntity category) {
        categoryService.save(category);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CategoryEntity category){
        categoryService.updateCascade(category);

        return R.ok();
    }

    /**
     *  删除
     *
     * @param catIds json数据 如[1432]
     * @return {@link R} 约定的返回格式，哈希字典
     * @see R
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] catIds){
//        检查当前菜单是否被其他地方引用
//		categoryService.removeByIds(Arrays.asList(catIds));
        // 批量删除
        categoryService.removeMenuByIds(Arrays.asList(catIds));

        return R.ok();
    }

    @RequestMapping("/update/sort")
    public R updateSort(@RequestBody CategoryEntity[] category){
        categoryService.updateBatchById(Arrays.asList(category));
        return R.ok();
    }

}
