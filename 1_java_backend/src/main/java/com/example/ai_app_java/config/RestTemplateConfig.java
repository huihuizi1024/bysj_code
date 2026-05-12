package com.example.ai_app_java.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        // 连接超时：10秒（建立TCP连接的最长时间）
        factory.setConnectTimeout(10000);
        // 读取超时：60秒（等待服务器响应的最长时间）
        factory.setReadTimeout(60000);
        // 连接池参数
        factory.setBufferRequestBody(false);
        return new RestTemplate(factory);
    }
}
