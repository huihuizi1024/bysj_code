package com.example.ai_app_java.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.ai_app_java.entity.User;
import org.apache.ibatis.annotations.Mapper;
/*
    UserMapper数据访问接口
 */
@Mapper // 加上这个注解，Spring Boot启动时就会把她注册为Bean
public interface UserMapper extends BaseMapper<User> {
    // 这里面什么都不用写！
    // BaseMapper<User> 已经帮你准备好了 insert, selectById, update 等方法。
}
