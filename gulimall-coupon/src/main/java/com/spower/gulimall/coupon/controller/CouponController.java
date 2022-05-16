package com.spower.gulimall.coupon.controller;

import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spower.gulimall.coupon.entity.CouponEntity;
import com.spower.gulimall.coupon.service.CouponService;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.R;


/**
 * 优惠券信息
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 16:07:41
 */
//@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@RestController
@RequestMapping("coupon/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @Value("${coupon.user.name}")
    private String name;
    @Value("${coupon.user.age}")
    private Integer age;

    /**
     * 测试获取application.properties配置文件
     *
     * @return
     */
    @RequestMapping("/test")
    public R test() {

        return R.ok().put("name", name).put("age", age);
    }

    /**
     * 测试服务远程调用:会员调用优惠券
     *
     * @return
     */
    @RequestMapping("/member/list")
    public R membercoupons() {
        CouponEntity entity = new CouponEntity();
        entity.setCouponName("满100减10");
        return R.ok().put("coupons", Arrays.asList(entity));
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = couponService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        CouponEntity coupon = couponService.getById(id);

        return R.ok().put("coupon", coupon);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody CouponEntity coupon) {
        couponService.save(coupon);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody CouponEntity coupon) {
        couponService.updateById(coupon);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        couponService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
