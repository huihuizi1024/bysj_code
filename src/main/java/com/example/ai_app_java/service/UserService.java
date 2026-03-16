package com.example.ai_app_java.service;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.UserRequest;
public interface UserService {
    //只定义标准: 注册功能
    Result register(UserRequest user);
}
