# ai_app_java

基于 Spring Boot 的心理支持对话系统后端原型。当前实现了基础接口与用户注册流程，后续将接入大语言模型并扩展心理支持对话能力。

## 项目简介

该项目提供一个轻量后端原型，包含欢迎与问候接口，以及基于 MyBatis-Plus 的用户注册落库流程，便于后续接入心理支持对话能力与更复杂的业务逻辑。

## ai_app_java

基于 Spring Boot 的心理支持对话系统后端原型。已实现用户注册/登录、JWT 鉴权、聊天记录落库与对话接口，支持调用外部大模型（以 DeepSeek 为例）。

## 项目简介

该项目提供一个轻量后端原型，包含欢迎与问候接口、用户体系、聊天接口与历史记录查询，便于后续扩展心理支持对话能力与更多业务逻辑。

## 功能特性

- 欢迎与问候接口
- 用户注册与登录（BCrypt 加密）
- JWT 鉴权与拦截保护
- 发送消息并调用大模型返回回复
- 聊天记录持久化与历史查询（最近 10 条）

## 技术栈

- Java 21
- Spring Boot 3.3.0
- MyBatis-Plus 3.5.5
- MySQL
- Lombok
- JJWT 0.11.5

## 快速开始

1. 安装 JDK 21 与 Maven
2. 启动 MySQL 并初始化数据库
   - 数据库名：mental_health_db
   - 执行脚本：init.sql
3. 修改配置
   - 文件：src/main/resources/application.properties
   - 数据库账号密码默认 root/root
   - 配置 jwt.secret、ai.api.key 等
4. 启动项目
   - 使用 IDE 运行 AiAppJavaApplication
   - 或运行：mvnw spring-boot:run

## 配置说明

- 数据库：spring.datasource.*
- JWT：jwt.secret、jwt.expiration
- AI：ai.api.url、ai.api.key、ai.api.model

## 接口文档

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

### 4) 用户登录接口

- 方法：POST
- 地址：/login
- 请求体同注册
- 成功返回：Result.data 中包含 token 与 userInfo

### 5) 发送聊天消息

- 方法：POST
- 地址：/chat/send
- 鉴权：需要 Authorization: Bearer <token>
- 请求体：

```json
{
  "content": "你好"
}
```

### 6) 查询历史记录

- 方法：GET
- 地址：/chat/history
- 鉴权：需要 Authorization: Bearer <token>
- 返回：最近 10 条聊天记录（按时间正序）

## 鉴权说明

- 受保护路径：/chat/**
- 放行路径：/login、/register
- 请求头格式：Authorization: Bearer <token>

## 数据库表结构

```sql
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
);

CREATE TABLE `chat_message` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `role` varchar(20) NOT NULL,
  `content` text NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
);
```

## 目录结构

```
src/main/java/com/example/ai_app_java
├── AiAppJavaApplication.java
├── config
│   ├── RestTemplateConfig.java
│   └── WebMvcConfig.java
├── controller
│   ├── ChatController.java
│   └── WelcomeController.java
├── entity
│   ├── ChatMessage.java
│   ├── Result.java
│   ├── User.java
│   └── UserRequest.java
├── interceptor
│   └── JwtInterceptor.java
├── mapper
│   ├── ChatMessageMapper.java
│   └── UserMapper.java
├── service
│   ├── AiService.java
│   ├── ChatMessageService.java
│   └── UserService.java
├── service/impl
│   ├── AiServiceImpl.java
│   ├── ChatMessageServiceImpl.java
│   └── UserServiceImpl.java
└── utils
    └── JwtUtils.java
```

## 调试与测试

- 参考 test.http 快速测试注册、登录与聊天接口
- /chat/send 与 /chat/history 需携带 Authorization 头

## 后续计划

- 优化提示词与危机识别逻辑
- 构建心理支持资源库
- 增加对话接口与前端页面
