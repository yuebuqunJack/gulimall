package com.spower.gulimall.product;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.api.RBucket;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
@RunWith(SpringRunner.class)
@SpringBootTest
public class WapGoodsApplicationTests {

    @Autowired
    @Qualifier("redisson")
    private RedissonClient redissonClient;

    private static List<String> succNameList= new CopyOnWriteArrayList<>();
    @Test
    public  void text1() throws InterruptedException {

        RBucket<Object> xy = redissonClient.getBucket("xy");
        xy.set("20",10, TimeUnit.MINUTES);

        RCountDownLatch countDownLatch = redissonClient.getCountDownLatch("num");
        countDownLatch.trySetCount(100);
        System.out.println("模拟100个客户端开始抢");
        for (int i = 0; i <100 ; i++) {

            new Thread(() -> {
                try {
                    System.out.println("---> 线程:"+Thread.currentThread().getName()+"在等待全部线程准备完毕 " + System.currentTimeMillis());
                    countDownLatch.await();
                    System.out.println("---> 线程:"+Thread.currentThread().getName()+"开始执行 " + System.currentTimeMillis());

                    RLock xyLock = redissonClient.getLock("xyLock");
                    xyLock.lock();
                    long stock = redissonClient.getAtomicLong("xy").addAndGet(-1);
                    if (stock<0){
                        System.out.println("库存不足");
                        redissonClient.getAtomicLong("xy").addAndGet(1);
                        return;
                    }
                    xyLock.unlock();
                    succNameList.add(Thread.currentThread().getName());
                    //System.out.println("线程:"+Thread.currentThread().getName()+"执行成功，剩余："+redissonClient.getAtomicLong("xy"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            },"name"+i).start();

            countDownLatch.countDown();


        }

        System.out.println("--------------------------------------succNameList.size():" + succNameList.size());

        succNameList.forEach(System.out::println);

        try {
            // 主线程需要等待线程执行完，否则，其他线程还没执行完，主线程就走完了，redisson会报错：Redisson is shutdown
            Thread.sleep(30000);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }


    }

}
