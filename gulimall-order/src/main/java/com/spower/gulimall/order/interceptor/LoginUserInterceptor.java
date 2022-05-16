package com.spower.gulimall.order.interceptor;

import com.spower.common.vo.MemberResponseVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static com.spower.common.constant.auth.AuthConstant.LOGIN_USER;

/**
 * @author CZQ
 * @Description: 登录拦截器
 * tips:如果使用网关解决，那么如果知道了服务的地址而不用拦截器就会导致可以直接进入服务
 * 简单来说网关崩了直接访问ip进入服务没有拦截器就可以直接进入这个订单服务了
 **/

@Component
public class LoginUserInterceptor implements HandlerInterceptor {

    public static ThreadLocal<MemberResponseVo> loginUser = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        /**
         * 匹配路径  如果是请求的这个uri路径则放行
         */
        String uri = request.getRequestURI();
        AntPathMatcher antPathMatcher = new AntPathMatcher();
        boolean match = antPathMatcher.match("/order/order/status/**", uri);
        boolean match1 = antPathMatcher.match("/payed/notify", uri);
        if (match || match1) {
            return true;
        }

        //获取登录的用户信息
        System.out.println("request.getSession().getAttribute(LOGIN_USER)==>"+request.getSession().getAttribute(LOGIN_USER));
//        MemberResponseVo attribute = (MemberResponseVo) request.getSession().getAttribute(LOGIN_USER);
        String attribute1 = (String)request.getSession().getAttribute(LOGIN_USER);
        MemberResponseVo attribute = new MemberResponseVo();
        attribute.setNickname(attribute1);

        if (attribute != null) {
            //把登录后用户的信息放在ThreadLocal里面进行保存
            loginUser.set(attribute);

            return true;
        } else {
            //未登录，返回登录页面
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            out.println("<script>alert('请先进行登录，再进行后续操作！');location.href='http://auth.gulimall.com/login.html'</script>");
            // session.setAttribute("msg", "请先进行登录");
            // response.sendRedirect("http://auth.gulimall.com/login.html");
            return false;
        }
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}
