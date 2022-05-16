package com.spower.gulimall.order;

import com.spower.common.entity.order.OrderEntity;
import com.spower.gulimall.order.entity.OrderReturnReasonEntity;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;


@Slf4j
@SpringBootTest
@RunWith(SpringRunner.class)
public class GulimallOrderApplicationTests {

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private RabbitTemplate rabbitTemplate;


    @Test
    public void sendMessageTest() {

        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                OrderReturnReasonEntity reasonEntity = new OrderReturnReasonEntity();
                reasonEntity.setId(1L);
                reasonEntity.setCreateTime(new Date());
                reasonEntity.setName("reason");
                reasonEntity.setStatus(1);
                reasonEntity.setSort(2);
                String msg = "Hello World";
                //1、发送消息,如果发送的消息是个对象，会使用序列化机制，将对象写出去，对象必须实现Serializable接口
                //2、发送的对象类型的消息，可以是一个json
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
                        reasonEntity, new CorrelationData(UUID.randomUUID().toString()));
            } else {
                OrderEntity entity = new OrderEntity();
                entity.setId(1L);
                rabbitTemplate.convertAndSend("hello-java-exchange", "hello.java",
                        entity, new CorrelationData(UUID.randomUUID().toString()));
            }

            System.out.println("========>消息发送完成");
        }


    }

    /**
     * 1、如何创建Exchange、Queue、Binding
     * 1）、使用AmqpAdmin进行创建
     * 2、如何收发消息
     */
    @Test
    public void createExchange() {
        Exchange directExchange = new DirectExchange("hello-java-exchange", true, false);
        amqpAdmin.declareExchange(directExchange);
        log.info("Exchange[{}]创建成功：", "hello-java-exchange");
    }


    @Test
    public void testCreateQueue() {
        Queue queue = new Queue("hello-java-queue", true, false, false);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：", "hello-java-queue");
    }


    /**
     * String destination,  - hello-java-queue:哪个队列
     * DestinationType destinationType, - Binding.DestinationType.QUEUE：绑定的是队列（也可以是交换机）
     * String exchange, - hello-java-exchange：交换机名称
     * String routingKey,  - hello.jav：绑定的key
     *
     * @Nullable Map<String, Object> arguments
     */
    @Test
    public void createBinding() {
        Binding binding = new Binding("hello-java-queue",
                Binding.DestinationType.QUEUE,
                "hello-java-exchange",
                "hello.java",
                null);
        amqpAdmin.declareBinding(binding);
        System.out.println("Binding[{}]创建成功：hello-java-binding");
    }

    @Test
    public void create() {
        HashMap<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "order-event-exchange");
        arguments.put("x-dead-letter-routing-key", "order.release.order");
        arguments.put("x-message-ttl", 60000); // 消息过期时间 1分钟
        Queue queue = new Queue("order.delay.queue", true, false, false, arguments);
        amqpAdmin.declareQueue(queue);
        log.info("Queue[{}]创建成功：", "order.delay.queue");
    }

}

