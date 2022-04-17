package com.spower.gulimall.auth.controller;

import com.alibaba.fastjson.TypeReference;
import com.spower.common.constant.auth.AuthConstant;
import com.spower.common.exception.BizCodeEnume;
import com.spower.common.utils.R;
import com.spower.common.vo.auth.MemberResponseVO;

import com.spower.gulimall.auth.feign.MemberFeignService;
import com.spower.gulimall.auth.feign.ThirdPartFeignService;
import com.spower.gulimall.auth.vo.UserLoginVo;
import com.spower.gulimall.auth.vo.UserRegisterVo;
import com.tencentcloudapi.asr.v20190614.models.Model;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 登录
 *
 * @Author: jackc
 * @Date: 2022/04/08 22:26
 */
@Controller
public class LoginController {

    @Autowired
    ThirdPartFeignService thirdPartFeignService;

    @Autowired
    MemberFeignService memberFeignService;

    @Autowired
    StringRedisTemplate redisTemplate;

    /**
     * 发送短信验证码
     *
     * @param phone 号码
     */
    @ResponseBody
    @GetMapping("/sms/sendcode")
    public R sendCode(@RequestParam(name = "phone", required = true) String phone) {

        /**
         * 1.判断60秒间隔发送，防刷
         * 根据前缀加前端传进来的电话查询value，再判断 当这个value不为空且(当前系统时间 - 存入redis的时间戳)的差小于60s则抛出错误：验证码获取频率太高，请稍后再试
         */
        String _code = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + phone);
        if (StringUtils.isNotBlank(_code) && System.currentTimeMillis() - Long.parseLong(_code.split("_")[1]) < 60000) {
            // 调用接口小于60秒间隔不允许重新发送新的验证码
            return R.error(BizCodeEnume.SMS_CODE_EXCEPTION.getCode(), BizCodeEnume.SMS_CODE_EXCEPTION.getMsg());
        }

        // 2.验证码存入缓存
        Integer codeD = (int) (Math.random() * 9000 + 1000);
        String code = String.valueOf(codeD);

        // 验证码缓存到redis中（并且记录当前时间戳） key---ksms:code:18290013344, value---7788_2021/11/26 22:26  TTL---10
        redisTemplate.opsForValue().set(AuthConstant.SMS_CODE_CACHE_PREFIX + phone, code + "_" + System.currentTimeMillis(), 10, TimeUnit.MINUTES);

        // 3.发送验证码,远程调用服务
        thirdPartFeignService.sendCode(phone, code);

        return R.ok();
    }

    /**
     * 注册接口
     * <p>
     * 注册成功回到登录页
     *
     * @param vo         = user         接收注册信息
     * @param result     接收参数校验结果
     * @param attributes 重定向保存数据（原理：使用session，重定向请求后根据cookie拿到session的数据）TODO 分布式session
     */
    @PostMapping(value = "/register")
    public String register(@Valid UserRegisterVo vo, BindingResult result, RedirectAttributes attributes) {
        // 1.参数校验
        if (result.hasErrors()) {
            // 校验出错，返回注册页
            Map<String, String> errMap = new HashMap<>();
            result.getFieldErrors().forEach(err -> errMap.put(err.getField(), err.getDefaultMessage()));
            // 封装异常返回前端显示
            attributes.addFlashAttribute("errors", errMap);// flash，session中的数据只使用一次
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
            // 1、return "redirect:http://auth.gulimall.com/reg.html"【采用】 重定向Get请求【配合RedirectAttributes共享数据】
            // 2、return "redirect:http:/reg.html"                   【采用】 重定向Get请求，省略当前服务url【配合RedirectAttributes共享数据】
            // 3、return "redirect:/reg.html"                                重定向Get请求，使用视图控制器拦截请求并映射reg视图【配合RedirectAttributes共享数据】【bug：会以ip+port来重定向】
            // 4、return "forward:http://auth.gulimall.com/reg.html";        请求转发与当前请求方式一致（Post请求）【配合Model共享数据】【异常404：当前/reg.html不存在post请求】
            // 5、return "forward:http:/reg.html";                           请求转发与当前请求方式一致（Post请求），省略当前服务url 【配合Model共享数据】【异常404：当前/reg.html不存在post请求】
            // 6、return "forward:/reg.html";                                请求转发与当前请求方式一致（Post请求），使用视图控制器拦截请求并映射reg视图【配合Model共享数据】【异常405：Request method 'POST' not supported，视图控制器必须使用GET请求访问，而当前请求转发使用post方式，导致异常】
            // 7、return "reg";                                              视图解析器前后拼串查找资源返回【配合Model共享数据】
        }

        // 2.验证码校验 查redis
        String code = vo.getCode();
        String redisCode = redisTemplate.opsForValue().get(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        if (StringUtils.isBlank(redisCode)) {
            // 验证码过期
            Map<String, String> errMap = new HashMap<>();
            errMap.put("code", "验证码失效");
            // 封装异常返回前端显示
            attributes.addFlashAttribute("errors", errMap);// flash，session中的数据只使用一次
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
        }
        if (!code.equals(redisCode.split("_")[0])) {
            // 验证码错误
            Map<String, String> errMap = new HashMap<>();
            errMap.put("code", "验证码错误");
            // 封装异常返回前端显示
            attributes.addFlashAttribute("errors", errMap);// flash，session中的数据只使用一次
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
        }

        // 3.调用login实现注册 ------------调用远程服务gulimall-member
        redisTemplate.delete(AuthConstant.SMS_CODE_CACHE_PREFIX + vo.getPhone());
        R r = memberFeignService.register(vo);
        if (r.getCode() == 0) {
            // 注册成功，重定向到登录页
            return "redirect:http://auth.gulimall.com/login.html";// 重定向
        } else {
            //　注册失败，封装异常
            HashMap<String, String> errMap = new HashMap<>();
            errMap.put("msg", r.getData(new TypeReference<String>() {
            }));
            attributes.addFlashAttribute("errors", errMap);// flash，session中的数据只使用一次
            return "redirect:http://auth.gulimall.com/reg.html";// 采用重定向有一定防刷功能
        }
    }

    /**
     * 访问登录页
     * 如果首页已显示用户名登录成功  那么访问登录页自动跳转
     */
    @GetMapping(value = "/login.html")
    public String login(HttpSession session) {
        Object attribute = session.getAttribute(AuthConstant.LOGIN_USER);
        if (attribute == null) {
            return null;
        } else {
            return "redirect:http://auth.gulimall.com";
        }

    }

    /**
     * 登录接口
     */
    @PostMapping(value = "/login")
    public String login(UserLoginVo user, RedirectAttributes attributes, HttpSession session) {
        // 1.远程调用登录
        R r = memberFeignService.login(user);
        if (r.getCode() == 0) {
            // 2.登录成功，设置session值
            MemberResponseVO data = r.getData(new TypeReference<MemberResponseVO>() {
            });
            session.setAttribute(AuthConstant.LOGIN_USER, data.getNickname());
            // 3.重定向，视图可以从session中拿到用户信息
            return "redirect:http://gulimall.com";
        } else {
            // 4.登录失败，封装异常信息重定向返回
            Map<String, String> errors = new HashMap<>();
            errors.put("msg", r.getData(new TypeReference<String>() {
            }));
            attributes.addFlashAttribute("errors", errors);
            return "redirect:http://auth.gulimall.com/login.html";
        }
    }

}