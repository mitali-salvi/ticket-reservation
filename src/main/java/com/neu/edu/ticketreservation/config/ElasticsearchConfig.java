package com.neu.edu.ticketreservation.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Value("${elasticsearch.host}")
    private String elasticsearchHost;

    public RestHighLevelClient client() {

        RestHighLevelClient client = new RestHighLevelClient(RestClient.builder(new HttpHost(elasticsearchHost, 9200, "http")));
        return client;

    }
}