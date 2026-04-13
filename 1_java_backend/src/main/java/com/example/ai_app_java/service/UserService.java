package com.example.ai_app_java.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.UserRequest;
import com.example.ai_app_java.entity.User;
public interface UserService extends IService<User> {
    //只定义标准: 注册功能
    Result register(UserRequest user);
    //登录接口
    Result login(UserRequest user);
}
