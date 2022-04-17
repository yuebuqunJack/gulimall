package com.spower.gulimall.thirdparty.controller;

import com.spower.common.utils.R;
import com.spower.gulimall.thirdparty.SmsComponent.SmsComponent;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @Description:
 * @Created: with IntelliJ IDEA.
 * @author: jackc
 * @createTime: 2022-04-07 10:04
 **/

@RestController
@RequestMapping(value = "/sms")
public class SmsSendController {

    @Resource
    private SmsComponent smsComponent;

    /**
     * 提供给别的服务进行调用（这个接口提供给服务需要才调用而不是页面去调用）
     * 这个接口就提供给了第三方服务的 ThirdPartFeignService
     *
     * @param phone
     * @param code
     * @return
     */
    @GetMapping(value = "/sendCode")
    public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code) {

        //发送验证码
        smsComponent.sendSms(phone, code, "2");

        return R.ok();
    }

}
