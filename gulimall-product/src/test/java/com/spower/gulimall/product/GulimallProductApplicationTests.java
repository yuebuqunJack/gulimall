package com.spower.gulimall.product;

import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.UUID;

/**
 * @author cenziqiang
 * @create 2022/3/28 21:06
 */
@SpringBootTest
public class GulimallProductApplicationTests {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    RedissonClient redissonClient;

    @Test
    public void testStringRedisTemplate() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        ops.set("hello", "word" + UUID.randomUUID().toString());

        String hello = ops.get("hello");
        System.out.println(hello);
    }

    @Test
    public void testStringRedisTemplate1() {
        ValueOperations<String, String> ops = stringRedisTemplate.opsForValue();
        String aa = ops.get("catalogJSON");
        System.out.println(aa);
    }

    @Test
    public void testStringRedisTemplate2() {
        System.out.println(redissonClient);
    }
}
