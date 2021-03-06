package com.spower.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.spower.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    /**
     * Auto-generated: 2022-03-25 22:4:19
     *
     * @author bejson.com (i@bejson.com)
     * @website http://www.bejson.com/java2pojo/
     */
    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;

    }

    /**
     * ##??????address?????????mill?????????????????????????????????????????????
     * GET bank/_search
     * {
     * "query": {
     * "match": {
     * "address": "Mill"
     * }
     * },
     * "aggs": {
     * "ageAgg": {
     * "terms": {
     * "field": "age",
     * "size": 10
     * }
     * },
     * "ageAvg": {
     * "avg": {
     * "field": "age"
     * }
     * },
     * "balanceAvg": {
     * "avg": {
     * "field": "balance"
     * }
     * }
     * },
     * "size": 0
     * }
     */
    @Test
    public void searchData() throws IOException {
        //1.??????????????????
        SearchRequest searchRequest = new SearchRequest();
        //2.????????????
        searchRequest.indices("bank");
        //3.??????DSL???????????????
        SearchSourceBuilder SourceBuilder = new SearchSourceBuilder();
        //3.1) ??????????????????
        /**
         *         SourceBuilder.query();
         *         SourceBuilder.from();
         *         SourceBuilder.size();
         *         SourceBuilder.aggregation();
         */
        SourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));


        //3.2) ????????????????????????????????????
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        SourceBuilder.aggregation(ageAgg);

        //3.3) ??????????????????
        AvgAggregationBuilder balanceAvg = AggregationBuilders.avg("balanceAvg").field("balance");
        SourceBuilder.aggregation(balanceAvg);

        System.out.println("???????????????" + SourceBuilder.toString());
        searchRequest.source(SourceBuilder);
        //4.????????????
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        //5.????????????searchResponse
        System.out.println(searchResponse.toString());

        //?????????map
//        Map map = JSON.parseObject(searchResponse.toString(), Map.class);

        //???????????????????????????
        SearchHits hits = searchResponse.getHits();
        SearchHit[] searchHits = hits.getHits();
        for (SearchHit hit : searchHits) {
            /**
             * 		"hits": [{
             * 			"_index": "bank",
             * 			"_type": "account",
             * 			"_id": "970",
             * 			"_score": 5.4032025,
             * 			"_source": {
             */
            // hit.getIndex();hit.getType();hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account:" + account);
        }
        //??????????????????????????????????????????????????????
        Aggregations aggregations = searchResponse.getAggregations();
//        for (Aggregation aggregation : aggregations) {
//            System.out.println("???????????????" + aggregation.getName());
//        }

        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("?????????" + keyAsString + "==>" + bucket.getDocCountError());
        }

        Avg balanceAvg1 = aggregations.get("balanceAvg");
        System.out.println("???????????????" + balanceAvg1.getValue());
    }

    /**
     * ????????????/????????????
     *
     * @throws IOException
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1"); //?????????id
//        indexRequest.source("userName","zhangsan","age",18,"gender","???");
        User user = new User();
        user.setUserName("jackc");
        user.setGender("???");
        user.setAge(200);
        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON);//??????????????????

        //????????????
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);
        System.out.println(index);
    }

    @Data
    public class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
