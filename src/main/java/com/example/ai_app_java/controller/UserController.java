package com.example.ai_app_java.controller;

import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.UserRequest;
import com.example.ai_app_java.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user") // 🔥 统一前缀，方便管理和拦截器配置
public class UserController {

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