package com.claw.system.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class CompletionHttpConfig {

    @Bean(name = "completionRestTemplate")
    public RestTemplate completionRestTemplate(RestTemplateBuilder builder, AiChatProperties props) {
        return builder
                .setConnectTimeout(Duration.ofMillis(Math.max(100L, props.getCompletionConnectTimeoutMs())))
                .setReadTimeout(Duration.ofMillis(Math.max(3000L, props.getCompletionReadTimeoutMs())))
                .build();
    }
}
