package com.claw.system.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class RetrievalHttpConfig {

    @Bean(name = "retrievalRestTemplate")
    public RestTemplate retrievalRestTemplate(RestTemplateBuilder builder, AiChatProperties props) {
        return builder
                .setConnectTimeout(Duration.ofMillis(Math.max(100L, props.getRetrievalConnectTimeoutMs())))
                .setReadTimeout(Duration.ofMillis(Math.max(1000L, props.getRetrievalReadTimeoutMs())))
                .build();
    }
}
