package com.spower.gulimall.thirdparty;

import com.spower.gulimall.thirdparty.SmsComponent.SmsComponent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * @author cenziqiang
 * @create 2022/4/7 22:33
 */
@SpringBootTest
public class testTencentSms {

    @Autowired
    SmsComponent smsComponent;

    @Test
    public void testTencentSms() {
        smsComponent.sendSms("+8618290013344", "7879", "2");
    }
}
