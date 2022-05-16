package com.spower.coupon;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@SpringBootTest
class GulimallCouponApplicationTests {

    /**
     * 最近三天时间范围
     */
    @Test
    void contextLoads() {
        LocalDate now = LocalDate.now();
        LocalDate plusDays = now.plusDays(1);
        LocalDate plusDays2 = now.plusDays(2);

        LocalTime min = LocalTime.MIN;
        LocalTime max = LocalTime.MAX;

        LocalDateTime start = LocalDateTime.of(now, min);
        LocalDateTime end = LocalDateTime.of(plusDays2, max);
    }

}
