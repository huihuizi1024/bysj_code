package com.example.ai_app_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
@Configuration//告诉Spring这是一个专门做配置的类
public class RestTemplateConfig {

    @Bean//告诉Spring把造出来的这台RestTemplate发报机放进仓库里
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
