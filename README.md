# ai_app_java

基于 Spring Boot 的心理支持对话系统后端原型。当前实现了基础接口与用户注册流程，后续将接入大语言模型并扩展心理支持对话能力。

## 技术栈

- Java 21
- Spring Boot 3.3.0
- MyBatis-Plus 3.5.5
- MySQL
- Lombok

## 本地运行

1. 安装 JDK 21 与 Maven
2. 启动 MySQL 并创建数据库
   - 数据库名：mental_health_db
3. 修改数据库配置
   - 文件：src/main/resources/application.properties
   - 默认账号密码为 root/root
4. 启动项目
   - 使用 IDE 运行 AiAppJavaApplication
   - 或运行：mvnw spring-boot:run

## 数据库表结构

项目使用 MyBatis-Plus 映射 user 表，字段如下：

```sql
CREATE TABLE `user` (
  `id` BIGINT NOT NULL AUTO_INCREMENT,
  `username` VARCHAR(64) NOT NULL,
  `password` VARCHAR(128) NOT NULL,
  `create_time` DATETIME NOT NULL,
  PRIMARY KEY (`id`)
);
```

## 已实现功能

- 基础连通性测试接口
- 简单问候接口
- 用户注册与数据落库

## 接口列表

### 1) 欢迎接口

- 方法：GET
- 地址：/welcome
- 说明：返回欢迎语与当前时间
- 返回示例（文本）：

```
Welcome to AI App Java恭喜！你的Java Spring Boot 环境已搭建成功！ 现在时间是： 2026-03-16T12:34:56
```

### 2) 问候接口

- 方法：GET
- 地址：/greet
- 参数：name（可选，默认“同学”）
- 返回示例（JSON）：

```json
{
  "status": "success",
  "message": "你好， Java学习者 ! 这是一份来自mac问候！",
  "tips": "2026年了，记得多用JDK 21新特性哦！"
}
```

### 3) 用户注册接口

- 方法：POST
- 地址：/register
- 请求体：

```json
{
  "username": "testuser",
  "password": "password123"
}
```

- 返回说明：
  - 用户名长度 ≤ 3：返回 code=400
  - 密码长度 < 6：返回 code=400
  - 数据库存储成功：返回 code=200
  - 数据库存储失败：返回 code=500

## 目录结构

```
src/main/java/com/example/ai_app_java
├── AiAppJavaApplication.java
├── controller
│   └── WelcomeController.java
├── entity
│   ├── Result.java
│   ├── User.java
│   └── UserRequest.java
├── mapper
│   └── UserMapper.java
└── service
    ├── UserService.java
    └── impl
        └── UserServiceImpl.java
```

## 后续计划

- 集成大语言模型 API（通义千问/智谱等）
- 增加共情提示词与危机识别逻辑
- 构建心理支持资源库
- 增加对话接口与前端页面
