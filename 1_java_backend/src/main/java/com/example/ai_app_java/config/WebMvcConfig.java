package com.example.ai_app_java.config;

import com.example.ai_app_java.interceptor.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置类：负责拦截器管理和全局跨域配置
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 1. 配置拦截器（身份校验）
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
        //保护区域：所有/chat开头的接口都必须检票
                .addPathPatterns("/chat/**")
        //放行区域：登录和注册接口谁都能进
                .excludePathPatterns("/user/login","/user/register");
    }
    /**
     * 2. 配置全局跨域（解决浏览器安全拦截）
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        //允许所有的接口路径进行跨域访问
        registry.addMapping("/**")
                //允许所有来源的域名访问（开发阶段使用patterns("*")最稳妥）
                .allowedOriginPatterns("*")
                //允许所有的HTTP方法(GET、POST、DELETE、PUT等)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                //允许所有的请求头(比如自定义的Authorization)
                .allowedHeaders("*")
                //允许前端发送Cookie等凭证信息
                .allowCredentials(true)
                //预检请求(OPPTIONS)的缓存时间，单位为秒（1小时）
                .maxAge(3600);
    }

}
