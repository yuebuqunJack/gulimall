package com.spower.gulimall.order.controller;

import com.spower.common.entity.order.OrderEntity;
import com.spower.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

/**
 * 拓展问题思考：消费积压   重复消费   消息重试机制   怎么破
 * @author cenziqiang
 * @create 2022/4/25 0:08
 */
@RestController
public class RabbitController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * @PathVariable：路径变量
     * @RequestHeader：获取请求头
     * @RequestParam：获取请求参数
     * @CookieValue：获取cookie值
     * @RequestBody：获取请求体
     * @RequestAttribute：获取request域属性
     * @param num
     * @return
     */
    @GetMapping("/senmq")
    public String send(@RequestParam(value = "num", defaultValue = "10")Integer num) {
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("reason");
                reasonEntity.setStatus(3333);
                reasonEntity.setSort(2);
                String msg = "Hello World";
                //1、发送消息,如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable接口
                //2、发送的对象类型的消息，可以是一个json
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
                        reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setId(1L);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello2.java",
                        entity, new CorrelationData(UUID.randomUUID().toString()));
            }

            System.out.println("========>消息发送完成");
        }
        return "ok";
    }

}
