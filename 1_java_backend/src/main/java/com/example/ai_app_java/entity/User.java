package com.example.ai_app_java.entity;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;
//
//数据库实体类
//严格对应MySQL中的user表
//
@Data //Lombok：自动生成get/set方法
@TableName("user") //告诉MyBatis-Plus，这个类对应数据库里的user表
public class User {
    @TableId(type = IdType.AUTO)//告诉MyBatis-Plus，id是主键且靠数据库自动递增
    private Long id;
    //以下两个字段和UserRequest中一样
    private String username;
    private String password;
    //驼峰命名法。MyBatis 会自动把 createTime 翻译成数据库里的 create_time 列
    private LocalDateTime createTime;
    private String role;//角色字段，USER=普通用户，ADMIN=管理员
}
