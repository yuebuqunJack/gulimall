package com.spower.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.spower.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author CZQ
 * DEMO下载：https://opendocs.alipay.com/open/270/106291
 * 沙箱：https://open.alipay.com/develop/sandbox/app
 */
@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    // 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
    public String app_id = "2021000119689079"; //2021000119689079

    // 商户私钥，您的PKCS8格式RSA2私钥
    public String merchant_private_key = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQDJNIhT9uT9VGcaU1KK6xWgs9BuS6GXoF4ob7Zevy97dsTAItvVaXj8RcQIrMA4C6c9EhjbSroyoq3FuU+m3N/GA3H+lSctbHSoPzTgUsX2Yr7OaZiGsm1VHrE8joj9xusRm+bPBIU/CgwLe1lsrFZS/WGmq+lwauMB1oVweAl8VJmjY+jfeg9MG94FumIKOhInBZhJZVw2+La4FCK9evifpgc9rQHIrA1C9xvzo27mp2jHxdzoXQFp0fHMDPQoSOCJoGCwXUKmXSxubYs/KCB2bbCcgwGJjIXDjIsf0NzZ+Z3+pI3ofMu+x9x3vycLNbDLc1g/FimC6M0Lg45CIcxfAgMBAAECggEAQjTsCxz52rY0OwBh795NPpUMp85xPNCDSzD/RIwKlsjNEyAlUEMlkXVvfR9DHO4QuNLEpRwgytqm7aH9qL2TULf/gbMYsiS2+knaH1p9U4bInGk4zFHYEiNIVNHeGAulfCTdvQ8SvVyT/A85rL3EpghEYrC83sX9LrSlMg39qRAjPjEfQbR7jDcW1rCdb2Ud7ZYQ38YEyHtOrzdhnR1Q7Nuyg/71olCWWDIEPi9QWg8SfYxZcRLTX+0bAnR6Qh80ONEbqaaZIH+Spk2tFRS2+bLrLxZoKJY+dLLKVVhINlHasivTmsdlT7xKT2iFUQZTxe3Vn5rN6TxQmvhCavQomQKBgQD9VFJ38KUWgD6n1SEqfBNpYi2JBhsASWJJTZIfGZcaddDmO2Le6uH0JYk9UygMc5cpai151Hpf6My1WIsiQXFok7tBQVYMsa2gLkjiAkd8i4a2mNAX7FxP7EP1t5RZmONfhG5ljigKZ0lPgh2dMnR4PkoGmqvVFLOpCceMrHQN+wKBgQDLU4oIwRuK36hTSdrTuCT1NdtXqqEqbnuYxmCfdEaolk2aquCMGa0sHHyxAP/Jzn1TDWhwmAToxOG7AJDB1UFOoAoVatiwy/KeJEouVt2f10TLkx5LpUGBiBzBVB6UPxqHgg1Y9TB87VkJlfO5qzi0rzBH2vIwMxAqAppDktKh7QKBgQCGaLro0FGHQrv7qGVUeUiDXYfzb73bxRPU+MdSoO0/KMuRnGi1BcKlG1VZqElTcfD+FEvUnBvhz07t2PDZWAeoCNoNoXDag1sv5d7dq3/qYM01DJc8WjgQX7cfJCwd7bSV9VntoCM2gd6vxMWZpi+NrN7x6hbJk5ZPVdT+mnFpCQKBgQCxt/Qn0LpFOVbEbgJyGjtbhVaSGtdB4k3v7D1aQmTcSHvOW9tnOa4upzup7wikmPLb3BVUjNuFpUj0jfr5IXAyzBnkPH7okWsgJPId7NlMM/mywoWxcuF92b3gkakkxL9ogBkwjydxmne/tCzTNcCb1aCqvcoArTFMtvelZrjqxQKBgFpljSkns1X7T6oPm4L52Z7YfszuT0EThVdnRHJfaHaXJRnaVKYDm7t5tTUuojF3BW6MDq5ZfOgPNAa/5phtkb0SSDGNmWrNT9/Guxg0LD+EqWVJU/491f7FnTw/G8OXAJV9BIAMrXlB29mW9izWIntAyjHmM4tI5twjyNLNwIzM";

    // 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    public String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnkEsqfpA6XHfekYhvDVESzc+xCot+xSCMmpbk2pKceDXfJf2Z90mQhEKZQjR3yKBywENZx+4gceALs80wLjAOWBGOGXaQ128iz/0oYQIIrOzE9RJk8JhH6XjUEpoHFXExjXA1b0pJz3qb6t5no5LKAZuED0plnnh2H565tjHJHulpagW+XmD2r8RMfZ/z8XbUC/YBoBRBayl7zvV183FLoi/j3i6dA3Zo/9v3oMNNAeqiGcakz6t7T+6jhCrFkpzMu4oEwrG3PHEzWxhb5ELPQu1CjQCvYElMvZ83436HS2P3Sim8cfSQGnbMuxQkgbM1MQZYxmuV/z7Glwduw9IIwIDAQAB";

    //
    //
    /**
     * 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
     * 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
     * localhost:8080需要内网穿透使支付宝能发送请求到该服务
     */
    public String notify_url = "http://alipay.free.idcfengye.com/alipay.trade.page.pay-JAVA-UTF-8/notify_url.jsp";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    //同步通知，支付成功，一般跳转到成功页
    public String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private  String sign_type = "RSA2";

    // 字符编码格式
    private  String charset = "utf-8";

    //订单超时时间
    private String timeout = "1m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    public String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public  String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\""+timeout+"\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        System.out.println("支付宝的响应："+result);

        return result;

    }
}
