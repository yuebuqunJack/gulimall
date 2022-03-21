package com.spower.gulimall.thirdparty;

import com.aliyun.oss.OSSClient;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

@SpringBootTest
class GulimallThirdPartyApplicationTests {

    @Resource
    OSSClient ossClient;

    @Test
    void contextLoads() {
    }

    @Test
    public void testUpload() {
//        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
//        String endpoint = "oss-cn-shenzhen.aliyuncs.com";
//        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
//        String accessKeyId = "LTAI5t95sFwNdrGuoy2FMJtR";
//        String accessKeySecret = "VhLu77rWpm74QefRuJn7mlxsVcDq8Z";//D4fbpj6cK2eYQ3ePCz61IsL3GHuFss
//
//        // 创建OSSClient实例。
//        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            InputStream InputStream = new FileInputStream("C:\\Users\\aa841\\Pictures\\Saved Pictures\\94bf77733509a2782ca67eeb1058d2f7.jpg");
            ossClient.putObject("spower-gulimall","94bf77733509a2782ca67eeb1058d2f7.jpg", InputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        // 关闭OSSClient。
        ossClient.shutdown();
        System.out.println("上传成功！！！！！！！！！！！！！");
    }

}
