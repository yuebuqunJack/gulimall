package com.spower.gulimall.member.controller;

import java.util.Arrays;
import java.util.Map;

import com.spower.common.exception.BizCodeEnum;
import com.spower.common.exception.BizCodeEnume;
import com.spower.common.to.member.MemberUserLoginTO;
import com.spower.common.to.member.MemberUserRegisterTO;
import com.spower.gulimall.member.exception.PhoneException;
import com.spower.gulimall.member.exception.UsernameException;
import com.spower.gulimall.member.feign.CouponFeignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.spower.gulimall.member.entity.MemberEntity;
import com.spower.gulimall.member.service.MemberService;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.R;


/**
 * 会员
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 17:00:29
 */
@RestController
@RequestMapping("member/member")
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private CouponFeignService couponFeignService;

    /**
     * 登录
     */
    @PostMapping("/login")
    public R login(@RequestBody MemberUserLoginTO user) {
        try {
            MemberEntity entity = memberService.login(user);
            if (entity == null) {
                return R.error(BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getCode(), BizCodeEnume.LOGINACCT_PASSWORD_EXCEPTION.getMsg());
            }
            return R.ok().setData(entity);
        } catch (Exception ex) {
            return R.error(ex.getMessage());
        }

    }

//    /**
//     * gitee社交登录调用的远程服务接口
//     * @param giteeInfo
//     * @return
//     * @throws Exception
//     */
//    @PostMapping("/gitee-login")
//    public R giteeLogin(@RequestParam("giteeInfo") String giteeInfo) throws Exception {
//        memberService.giteeLogin(giteeInfo);
//        return R.ok();
//    }
    /**
     * gitee社交登录调用的远程服务接口
     * @param giteeInfo
     * @return
     * @throws Exception
     */
    @PostMapping("/gitee-login")
    public R giteeLogin(@RequestParam("giteeInfo") String giteeInfo) throws Exception {

        MemberEntity memberEntity = memberService.giteeLogin(giteeInfo);

        if (memberEntity != null) {
            return R.ok().setData(memberEntity);
        } else {
            return R.error(BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getCode(),BizCodeEnum.LOGINACCT_PASSWORD_EXCEPTION.getMessage());
        }
    }

    /**
     * 注册
     */
    @PostMapping("/register")
    public R register(@RequestBody MemberUserRegisterTO vo) {
        try {
            memberService.register(vo);
        } catch (PhoneException e) {
            return R.error(BizCodeEnum.PHONE_EXIST_EXCEPTION.getCode(), BizCodeEnum.PHONE_EXIST_EXCEPTION.getMessage());
        } catch (UsernameException e) {
            return R.error(BizCodeEnum.USER_EXIST_EXCEPTION.getCode(), BizCodeEnum.USER_EXIST_EXCEPTION.getMessage());
        }

        return R.ok();
    }


    /**
     * openFeign测试接口
     */
    @RequestMapping("/coupons")
    public R test() {
        MemberEntity entity = new MemberEntity();
        entity.setNickname("张三");

        R membercoupons = couponFeignService.membercoupons();
        Object coupons = membercoupons.get("coupons");
        return R.ok().put("member", entity).put("coupons", coupons);
    }

    /**
     * 列表
     */
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params) {
        PageUtils page = memberService.queryPage(params);

        return R.ok().put("page", page);
    }


    /**
     * 信息
     */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id) {
        MemberEntity member = memberService.getById(id);

        return R.ok().put("member", member);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    public R save(@RequestBody MemberEntity member) {
        memberService.save(member);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    public R update(@RequestBody MemberEntity member) {
        memberService.updateById(member);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    public R delete(@RequestBody Long[] ids) {
        memberService.removeByIds(Arrays.asList(ids));

        return R.ok();
    }

}
