package com.example.ai_app_java.config;

import com.example.ai_app_java.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
        //保护区域：所有/chat开头的接口都必须检票
                .addPathPatterns("/chat/**")
        //放行区域：登录和注册接口谁都能进
                .excludePathPatterns("/login","/register");
    }
}
