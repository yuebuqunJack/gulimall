package com.spower.gulimall.product;

import com.spower.gulimall.product.dao.AttrGroupDao;
import com.spower.gulimall.product.dao.SkuSaleAttrValueDao;
import com.spower.gulimall.product.vo.SkuItemSaleAttrVo;
import com.spower.gulimall.product.vo.SkuItemVo;
import com.spower.gulimall.product.vo.SpuItemAttrGroupVo;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
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

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    SkuSaleAttrValueDao skuSaleAttrValueDao;

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

    @Test
    public void test() {
//        List<SpuItemAttrGroupVo> attrGroupVos = attrGroupDao.getAttrGroupWithAttrsBySpuId(2L, 225L);
//        System.out.println(attrGroupVos);
        List<SkuItemSaleAttrVo> saleAttrBySpuId = skuSaleAttrValueDao.getSaleAttrBySpuId(2L);
        System.out.println(saleAttrBySpuId);
    }
}
