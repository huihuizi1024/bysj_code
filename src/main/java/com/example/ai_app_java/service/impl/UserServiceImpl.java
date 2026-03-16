package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.User;
import com.example.ai_app_java.mapper.UserMapper;
import com.example.ai_app_java.entity.UserRequest;
import com.example.ai_app_java.service.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
/*
1、继承基类ServiceImpl<UserMapper,User>：
使 Service 直接拥有了MP提供的CRUD能力
2、实现UserService
 */
@Service    //告诉Sping：我是业务层组件
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {
    @Override
    //注册逻辑
    public Result register(UserRequest user) {
        //这里写复杂的视线逻辑，比如判断密码长度等
        //1、模拟校验用户名长度以及密码强度
        if(user.getUsername()==null || user.getUsername().length()<=3){
            return new Result(400,"fail","注册失败： 用户名长度不能少于3位",null);
        }
        if(user.getPassword().length()<6){
            return new Result(400,"fail","注册失败：密码长度过短，至少需要6位！",null);
        }
        //2、对象转换
        User userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setCreateTime(LocalDateTime.now());

        //3、对象落库并且实现数据库持久化
        System.out.println("【数据库日志】正在向MySQL写入用户信息："+user.getUsername());

        //调用ServiceImpl 提供的save方法
            boolean success = this.save(userEntity);
            if(success){
                return new Result(200, "success",
                        "用户 " + user.getUsername() + " 注册成功！", null);
            }else {
                return new Result(500,"error","数据库写入失败", null);
            }
    }



}
