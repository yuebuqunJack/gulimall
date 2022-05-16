package com.spower.gulimall.order.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author CZQ
 * @Description: feign拦截器功能
 * 解决：Feign远程调用丢失请求头的问题
 * 拦截器的原理：通过ThreadLocal共享副本，当发起请求@Configuration就会扫描配置文件，@Bean("requestInterceptor")就会将通过注解反射代理使用到本拦截器。
 * 分布式高级p70
 * eg:获取了HttpServletRequest request
 **/

@Configuration
public class GuliFeignConfig {

    @Bean("requestInterceptor")
    public RequestInterceptor requestInterceptor() {

        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {

                System.out.println("Interceptor线程===》" + Thread.currentThread().getId());
                //1、使用RequestContextHolder拿到刚进来的请求数据
                ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (requestAttributes != null) {
                    //就可以获取老请求
                    HttpServletRequest request = requestAttributes.getRequest();

                    if (request != null) {
                        //2、同步请求头的数据（主要是cookie）
                        //把老请求的cookie值放到新请求上来，进行一个同步
                        String cookie = request.getHeader("Cookie");
                        //给新请求同步老请求的cookie
                        template.header("Cookie", cookie);
                    }
                }
            }
        };

        return requestInterceptor;
    }

}
