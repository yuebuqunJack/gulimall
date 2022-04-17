package com.spower.gulimall.auth.feign;

import com.spower.common.utils.R;
import com.spower.common.vo.auth.UserRegisterVO;
import com.spower.gulimall.auth.vo.SocialUser;
import com.spower.gulimall.auth.vo.UserLoginVo;
import com.spower.gulimall.auth.vo.UserRegisterVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Description:
 * @author Jack.c
 * @email aa841264873@qq.com
 * @date 2022-03-17 17:00:29
 **/

@FeignClient("gulimall-member")
public interface MemberFeignService {

    @PostMapping(value = "/member/member/register")
    R register(@RequestBody UserRegisterVo vo);


    @PostMapping(value = "/member/member/login")
    R login(@RequestBody UserLoginVo vo);

    @PostMapping(value = "/member/member/oauth2/login")
    R oauthLogin(@RequestBody SocialUser socialUser) throws Exception;

    @PostMapping(value = "/member/member/weixin/login")
    R weixinLogin(@RequestParam("accessTokenInfo") String accessTokenInfo);

    /**
     * gitee 登录
     * @param giteeInfo
     * @return
     */
    @PostMapping("/member/member/gitee-login")
    R giteeLogin(@RequestParam("giteeInfo") String giteeInfo);
}
