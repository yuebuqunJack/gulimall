package com.spower.gulimall.order.service.impl;

import com.rabbitmq.client.Channel;
import com.spower.common.entity.order.OrderEntity;
import com.spower.gulimall.order.entity.OrderReturnReasonEntity;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spower.common.utils.PageUtils;
import com.spower.common.utils.Query;

import com.spower.gulimall.order.dao.OrderItemDao;
import com.spower.gulimall.order.entity.OrderItemEntity;
import com.spower.gulimall.order.service.OrderItemService;


/**
 * @author CZQ
 */
@RabbitListener(queues = {"hello-java-queue"})
@Service("orderItemService")
public class OrderItemServiceImpl extends ServiceImpl<OrderItemDao, OrderItemEntity> implements OrderItemService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<OrderItemEntity> page = this.page(
                new Query<OrderItemEntity>().getPage(params),
                new QueryWrapper<OrderItemEntity>()
        );

        return new PageUtils(page);
    }

    /**
     * queues：声明需要监听的队列
     * channel：当前传输数据的通道
     * import org.springframework.amqp.core.Message;
     * <p>
     * 1.Message message:原生消息详细信息 头+体
     * 2.T<发送嘻嘻哦西的类型> OrderReturnReasonEntity content；
     * 3.Channel channel：当前传输数据的通道
     * <p>
     * Queue：可以很多人都来监听 只要收到消息 队列就删除消息 而且只能有一个收到此消息
     * 场景：
     * 1）订单服务启动多个：同一个消息只能一个客户端收到
     * 2）只有一个消息完全处理完，方法运行结束，我们就可以接收到下一个消息
     *
     * @RabbitListener：作用域在方法或者类 与
     * @RabbitHandler：作用域在类 场景：
     * 当需要根据判断接收不同的消息类型，有多个@RabbitHandler时候使用
     * 接收OrderReturnReasonEntity这个类型的消息
     */
//    @RabbitListener(queues = {"hello-java-queue"})
    @RabbitHandler
    public void revieveMessage(Message message,
                               OrderReturnReasonEntity content,
                               Channel channel) throws InterruptedException {
        System.out.println("OrderReturnReasonEntity内容===>" + content);
        //拿到主体内容
        byte[] body = message.getBody();
        //拿到的消息头属性信息
        MessageProperties properties = message.getMessageProperties();
//        Thread.sleep(3000);
        System.out.println("消息处理完成==>" + content.getName());
        //channel内按顺序自增的
        long deliveryTag = message.getMessageProperties().getDeliveryTag();
        System.out.println("deliveryTag==>" + deliveryTag);
        //签收货物 (签收哪个标记位的,是否批量接收--目前是做一个确认一个)
        try {
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 接收OrderEntity这个类型的消息
     *
     * @param content
     * @throws InterruptedException
     */
    @RabbitHandler
    public void revieveMessage2(OrderEntity content) throws InterruptedException {
        System.out.println("OrderEntity接收到消息==>" + content);

    }

}