package com.spower.gulimall.product;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.spower.gulimall.product.entity.BrandEntity;
import com.spower.gulimall.product.service.BrandService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RunWith(SpringRunner.class)
/**
 * 注解的意义在于Test测试类要使用注入的类，比如@Autowired注入的类，
 *
 * 有了@RunWith(SpringRunner.class)这些类才能实例化到spring容器中，自动注入才能生效，
 *
 * 不然直接一个NullPointerExecption
 */
@SpringBootTest
public class GulimallProductApplicationTests {

    @Resource
    private BrandService brandService;

    @Test
    public void contextLoads() {
        BrandEntity brandEntity = new BrandEntity();
        brandEntity.setBrandId(1L);
        brandEntity.setName("华为Test");
//        brandService.save(brandEntity);
//        System.out.println("save success");
//        brandService.updateById(brandEntity);

        List<BrandEntity> list = brandService.list(new QueryWrapper<BrandEntity>().eq("brand_id", 1L));
        list.forEach((item) -> {
            System.out.println(item);
        });
    }




}
