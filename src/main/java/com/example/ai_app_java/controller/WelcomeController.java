package com.example.ai_app_java.controller;

import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
public class WelcomeController {

    @GetMapping("/welcome")
    public String welcome() {
        return "🚀 AI App Java 后端运行中！当前时间：" + LocalDateTime.now();
    }

    @GetMapping("/greet")
    public Map<String, Object> greet(@RequestParam(defaultValue = "同学") String name) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "你好，" + name + "！");
        return response;
    }
}