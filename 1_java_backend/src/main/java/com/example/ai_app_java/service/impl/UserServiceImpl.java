package com.example.ai_app_java.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.ai_app_java.entity.Result;
import com.example.ai_app_java.entity.User;
import com.example.ai_app_java.mapper.UserMapper;
import com.example.ai_app_java.entity.UserRequest;
import com.example.ai_app_java.service.UserService;
import com.example.ai_app_java.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.mindrot.jbcrypt.BCrypt;//引入BCrypt 算法加密工具
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.time.LocalDateTime;
/*
1、继承基类ServiceImpl<UserMapper,User>：
使 Service 直接拥有了MP提供的CRUD能力
2、实现UserService
 */
@Service    //告诉Sping：我是业务层组件
public class UserServiceImpl extends ServiceImpl<UserMapper,User> implements UserService {

    @Autowired
    private JwtUtils jwtUtils;  //注入造票机

    @Override
    //注册逻辑
    public Result register(UserRequest user) {
        //这里写复杂的视线逻辑，比如判断密码长度等
        //1、模拟校验用户名长度以及密码强度
        if(user.getUsername()==null || user.getUsername().length()<=3){
            return new Result(400,"fail","注册失败： 用户名长度不能少于3位",null);
        }
        if(user.getPassword()==null || user.getPassword().length()<6){
            return new Result(400,"fail","注册失败：密码长度过短，至少需要6位！",null);
        }
        //用户名重复性检查
        User existingUser = this.lambdaQuery()
                .eq(User::getUsername,user.getUsername())
                .one();
        if(existingUser!=null){
            //如果查到了，说明名字已经被占用了，返回错误信息
            return Result.fail(400,"哎呀，这个名字已经有人用啦，换一个吧！");
        }
        try{
        //2、对象转换
        User userEntity = new User();
        userEntity.setUsername(user.getUsername());
        userEntity.setPassword(user.getPassword());
        userEntity.setCreateTime(LocalDateTime.now());

        //3、对象落库并且实现数据库持久化，用BCrypt算法明文密码加密成乱码
        String hashedPassword = BCrypt.hashpw(user.getPassword(),BCrypt.gensalt());
        System.out.println("【数据库日志】正在向MySQL写入用户信息："+user.getUsername());
        userEntity.setPassword(hashedPassword);
        userEntity.setCreateTime(LocalDateTime.now());
        //调用ServiceImpl 提供的save方法
            boolean success = this.save(userEntity);
            if(success){
                return Result.success("注册成功！",null);
            }else {
                return new Result(500,"error","数据库写入失败", null);
            }
        }catch (Exception e){
            return Result.fail(500,"系统异常： "+e.getMessage());
        }
    }

    @Override
    public Result login(UserRequest user) {
        //1.基础判空
        if(user.getUsername()==null || user.getPassword() == null){
            return new Result(400,"fail","登陆失败：用户名或密码不能为空！",null);
        }
        //2.用MyBatis-Plus的特性，去数据库里捞这个用户
        User dbUser = this.lambdaQuery().eq(User::getUsername,user.getUsername()).one();
        //3.如果查无此人
        if(dbUser == null){
            return new Result(400,"fail","登陆失败：用户不存在",null);
        }
        //4.用BCrypt算法进行密码核对
        //原理：此时user.getPassword()是前端传来的明文，而dbUser.getPassword()是数据库里存的乱码
        //此时不能用‘--’去比较，而是要用BCrypt.checkpw()去处理
        if(BCrypt.checkpw(user.getPassword(),dbUser.getPassword())){
            //5.脱敏处理（保护用户隐私-）
            //如登陆成功了，我们需要把用户信息返回前端，但是需要抹去敏感信息（如“密码“字段）
            // 这样前端的网络请求拦截器里，就看不到这个人的密码密文了。
            dbUser.setPassword(null);
            //签发JWT令牌
            String token = jwtUtils.createToken(dbUser.getId(), dbUser.getUsername());
            //把token和用户信息装进一个Map里返回给前端
            Map<String,Object> responseData = new HashMap<>();
            responseData.put("token",token);
            responseData.put("userInfo",dbUser);
            return new Result(200,"success","登陆成功！欢迎回来："+dbUser.getUsername(),responseData);
        }else{
            //密码比对失败
            return new Result(400,"fail","登陆失败：用户名或密码错误！",null);
        }
    }



}
