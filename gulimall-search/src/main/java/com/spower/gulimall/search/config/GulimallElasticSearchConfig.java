package com.spower.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-hight.html 文档
 * @author CZQ
 */
@Configuration
public class GulimallElasticSearchConfig {

//    @Bean
//    public RestHighLevelClient esRestClient() {
//
//        RestClientBuilder builder = null;
//        builder = RestClient.builder(new HttpHost("175.178.212.59"), 9200, "http");
//        RestHighLevelClient client = new RestHighLevelClient(builder);
//        return client;
//    }

    public static final RequestOptions COMMON_OPTIONS;

    /**
     * 通用设置项
     */
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        // 如果此处是集群，传入多个主机就可以了
                        new HttpHost("175.178.212.59", 9200, "http")));
        return client;
    }
}
