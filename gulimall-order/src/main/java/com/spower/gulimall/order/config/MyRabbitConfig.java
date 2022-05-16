package com.spower.gulimall.order.config;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author CZQ
 **/
@Configuration
public class MyRabbitConfig {

//    @Resource
//    private RabbitTemplate rabbitTemplate;

    RabbitTemplate rabbitTemplate;

    @Primary
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        this.rabbitTemplate = rabbitTemplate;
        rabbitTemplate.setMessageConverter(messageConverter());
        initRabbitTemplate();
        return rabbitTemplate;
    }

    /**
     * rabbitMQ接受到的消息序列化后的消息转为人看得懂的
     *
     * @return
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 定制RabbitTemplate
     * 1、服务收到消息就会回调
     * 1、spring.rabbitmq.publisher-confirms: true
     * 2、设置确认回调
     * 2、消息正确抵达队列就会进行回调
     * 1、spring.rabbitmq.publisher-returns: true
     * spring.rabbitmq.template.mandatory: true
     * 2、设置确认回调ReturnCallback
     * <p>
     * 3、消费端确认(保证每个消息都被正确消费，此时才可以broker删除这个消息)
     *
     * @PostConstruct :MyRabbitConfig对象创建完成以后，执行这个方法
     */
//    @PostConstruct
    public void initRabbitTemplate() {

        /**
         * 确认消息的方式使用该注解
         * 1、只要消息抵达Broker就ack=true 参考ppt 19
         * correlationData：当前消息的唯一关联数据(这个是消息的唯一id)
         * ack：消息是否成功收到
         * cause：失败的原因
         */
        //设置确认回调
        rabbitTemplate.setConfirmCallback(new RabbitTemplate.ConfirmCallback() {
            @Override
            public void confirm(CorrelationData correlationData, boolean ack, String cause) {
                /**
                 * 1.做好消息确认机制（publisher、consumer 手动ack）
                 * 2.每一个发送的消息都在数据库做好记录，定期将失败的消息重新发送。gulimall_oms -- mq_message该表做好记录。
                 */
                /**
                 * 服务器接收到了消息
                 * 根据correlationData的唯一id，修改消息的状态 gulimall_oms --- mq_message的message_status字段
                 */
                System.out.println("confirm...correlationData==>[" + correlationData + "]==>ack:[" + ack + "]==>cause:[" + cause + "]");
            }
        });


        /**
         * 设置消息抵达队列的确认回调
         * 只要消息没有投递给指定的队列，就触发这个失败回调
         * message：投递失败的消息详细信息
         * replyCode：回复的状态码
         * replyText：回复的文本内容
         * exchange：当时这个消息发给哪个交换机
         * routingKey：当时这个消息用哪个路由键
         */
        rabbitTemplate.setReturnCallback(new RabbitTemplate.ReturnCallback() {
            @Override
            public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
                //报错误了 修改数据库当前消息的状态-》错误
                System.out.println("Fail Message[" + message + "]==>replyCode[" + replyCode + "]" +
                        "==>replyText[" + replyText + "]==>exchange[" + exchange + "]==>routingKey[" + routingKey + "]");
            }
        });
    }

}
