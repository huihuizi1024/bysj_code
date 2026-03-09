package com.example.ai_app_java;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController//告诉Spring这是一个处理HTTP请求的控制器
public class WelcomeController {
    /**
     * 简单的字符串返回接口
     * 访问地址：http://localhost:8080/welcome
     */
    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to AI App Java" +
                "恭喜！你的Java Spring Boot 环境已搭建成功！ 现在时间是： "+LocalDateTime.now();
    }
    /**
     * 带参数的JSON接口（模拟后端实际开发）
     * 访问地址：HTTP://localhost:8080/greet?name=Java学习者
     */
    @GetMapping("/greet")
    public Map<String, Object> greet(@RequestParam(defaultValue = "同学") String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("status","success");
        response.put("message","你好， "+name+" ! 欢迎重新开启java之旅。");
        response.put("tips","2026年了，记得多用JDK 21新特性哦！");
        return response;
    }
}
