package com.example.ai_app_java;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@MapperScan("com.example.ai_app_java.mapper")
@EnableAsync
public class AiAppJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAppJavaApplication.class, args);
    }

}
