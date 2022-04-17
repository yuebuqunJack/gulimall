package com.spower.gulimall.auth.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.spower.common.constant.auth.AuthConstant;
import com.spower.common.utils.HttpUtils;
import com.spower.common.utils.R;
import com.spower.gulimall.auth.feign.MemberFeignService;
import com.spower.common.vo.auth.MemberResponseVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * @author cenziqiang
 * @create 2022/4/10 10:20
 */
@Slf4j
@Controller
@RequestMapping("/web/oauth2")
public class OAuth2Controller {
    @Resource
    private MemberFeignService memberFeignService;

    @GetMapping("/gitee/success")
    public String gitee(@RequestParam("code") String code, HttpSession session, HttpServletResponse servletResponse) throws Exception {
        // 准备请求参数 https://gitee.com/oauth/token?grant_type=authorization_code&code={code}&client_id={client_id}&redirect_uri={redirect_uri}&client_secret={client_secret}
        Map<String, String> params = new HashMap<>();
        params.put("client_id", "75c76c2fc2f31d7aa49f7d7ecadb1a8541264d70232a738d7700d1f738bef15d");
        params.put("redirect_uri", "http://auth.gulimall.com/web/oauth2/gitee/success");
        params.put("client_secret", "5314227be2b5e1dd31023f82be9568040eb244fd29ae7bc9e4a8934478819d7c");
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        // 获取accesstoken
        HttpResponse response = HttpUtils.doPost("https://gitee.com", "/oauth/token", "post", new HashMap<>(), new HashMap<>(), params);

        if (response.getStatusLine().getStatusCode() == 200) {
            // 说明获取到了
            // 取出返回数据
            String giteeInfo = EntityUtils.toString(response.getEntity());
            R r = memberFeignService.giteeLogin(giteeInfo);
            if (r.getCode() == 0) {
                //TODO 将对象抽取到MemberResponseVO中使用
                MemberResponseVO data = r.getData("data", new TypeReference<MemberResponseVO>() {
                });
                log.info("登陆成功：{}", data.getNickname());
                session.setAttribute(AuthConstant.LOGIN_USER, data.getNickname());
                //TODO 1.默认发的令牌。session=xxx 作用域 当前域：解决子域session共享问题
                //TODO 2.使用JSON的序列化方式来序列化对象
                //new Cookie("JSESSIONID","data").setDomain("")还可以设置作用域
//                servletResponse.addCookie(new Cookie("JSESSIONID", "data"));
                return "redirect:http://gulimall.com/";
            }

        } else {
            return "redirect:http://auth.gulimall.com/login.html";
        }

        return "redirect:http://auth.gulimall.com/login.html";
    }
}
