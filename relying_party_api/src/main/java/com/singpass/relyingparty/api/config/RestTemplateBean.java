package com.singpass.relyingparty.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * This class creates REST template bean
 */
@Configuration
public class RestTemplateBean {
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
