package com.example.ai_app_java.controller;
import com.example.ai_app_java.entity.UserRequest;
import com.example.ai_app_java.service.UserService;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController//告诉Spring这是一个处理HTTP请求的控制器
public class WelcomeController {
    @Autowired
    private UserServiceImpl userServiceImpl;

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
        response.put("message","你好， "+name+" ! 这是一份来自mac问候！");
        response.put("tips","2026年了，记得多用JDK 21新特性哦！");
        return response;
    }
    @Autowired//    自动注入UserService类
    private UserService userService;
    @PostMapping("/register")
    public Result register(@RequestBody UserRequest UserInfo) {
        //1、调用Service层，拿到封装好的最后Result对象
        Result finalResponse = userService.register(UserInfo);
        //2、直接返回， Spring 的 @RestController会自动把它变成JSON格式
        return finalResponse;
    }
    @PostMapping("/login")
    public Result login(@RequestBody UserRequest UserInfo) {
        return userService.login(UserInfo);
    }

}
