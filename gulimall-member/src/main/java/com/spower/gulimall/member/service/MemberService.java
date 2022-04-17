package com.spower.gulimall.member.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.spower.common.to.member.MemberUserLoginTO;
import com.spower.common.to.member.MemberUserRegisterTO;
import com.spower.common.utils.PageUtils;
import com.spower.gulimall.member.entity.MemberEntity;
import com.spower.gulimall.member.exception.PhoneException;
import com.spower.gulimall.member.exception.UsernameException;

import java.util.Map;

/**
 * 会员
 *
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 17:00:29
 */
public interface MemberService extends IService<MemberEntity> {

    PageUtils queryPage(Map<String, Object> params);

    /**
     * 用户注册
     *
     * @param vo = user
     */
    void register(MemberUserRegisterTO vo);

    /**
     * 判断电话是否重复
     *
     * @param phone
     * @return
     */
    void checkPhoneUnique(String phone) throws PhoneException;

    /**
     * 判断用户名是否重复
     *
     * @param userName
     * @return
     */
    void checkUserNameUnique(String userName) throws UsernameException;

    MemberEntity login(MemberUserLoginTO user);

    MemberEntity giteeLogin(String giteeInfo) throws Exception;

//    /**
//     * 用户登录
//     *
//     * @param vo
//     * @return
//     */
//    MemberEntity login(MemberUserLoginVo vo);
//
//    /**
//     * 社交用户的登录
//     *
//     * @param socialUser
//     * @return
//     */
//    MemberEntity login(SocialUser socialUser) throws Exception;
//
//    /**
//     * 微信登录
//     *
//     * @param accessTokenInfo
//     * @return
//     */
//    MemberEntity login(String accessTokenInfo);
}

