# 心理支持对话系统

基于大语言模型的心理健康 AI 陪伴系统 —— 毕业设计项目

## 项目简介

本系统是一个完整的心理支持对话平台，采用**前后端分离**架构：

- **后端**：Spring Boot 3.3 + MyBatis-Plus + Java 21，处理 API 请求、数据库操作、AI 对话、情绪分析、危机预警
- **前端**：Vue 3 + Element Plus + Pinia，提供登录注册、多会话聊天、SSE 流式对话（打字机效果）
- **AI 引擎**：DeepSeek API（OpenAI 兼容接口），支持替换为本地微调模型（LLaMA-Factory）

系统面向有心理健康支持需求的用户，提供匿名、温暖、安全的 AI 心理陪伴对话体验，同时具备情绪分析、危机预警、用户画像、支持资源库等专业的心理健康辅助功能。

## 系统架构

```
┌──────────────────┐       HTTP/SSE        ┌──────────────────┐      API 调用      ┌──────────────────┐
│   Vue 3 前端     │  ←──────────────────→ │  Spring Boot 3.3 │ ←───────────────→  │  DeepSeek API   │
│  Element Plus   │                       │   MyBatis-Plus   │                    │  (可替换为本地   │
│  Pinia 状态管理  │                       │  Java 21         │                    │   LLaMA-Factory) │
│  Port: 5173     │                       │  Port: 8080      │                    └──────────────────┘
└──────────────────┘                       └────────┬─────────┘
                                                   │
                                              JDBC ↓
                                         ┌──────────────────┐
                                         │    MySQL 8.x     │
                                         │ mental_health_db │
                                         └──────────────────┘
```

## 技术栈

| 模块 | 技术 | 版本 | 说明 |
|------|------|------|------|
| **前端** | Vue 3 + Vite | 3.5 / 8.0 | 组合式 API + `<script setup>` 语法 |
| | Element Plus | 2.13 | UI 组件库 |
| | Pinia | 3.0 | 状态管理 |
| | Vue Router | 5.0 | 路由守卫保护聊天页 |
| | Axios | 1.14 | 请求拦截器自动携带 Token |
| **后端** | Spring Boot | 3.3.0 | Web 框架 |
| | MyBatis-Plus | 3.5.5 | CRUD 零配置 |
| | JJWT | 0.11.5 | Token 签发与验签 |
| | jBCrypt | 0.4 | 密码单向加密 |
| | Java | 21 | 虚拟线程支持 SSE 长连接 |
| **数据库** | MySQL | 8.x | utf8mb4 字符集 |
| **AI 引擎** | DeepSeek API | — | OpenAI 兼容接口（可换本地模型） |

## 目录结构

```
bysj_code/
├── 1_java_backend/          # Spring Boot 后端服务
│   ├── src/main/java/       # Java 源码
│   │   └── com.example.ai_app_java/
│   │       ├── controller/  # REST 控制器（7个）
│   │       │   ├── ChatController.java        # 聊天与会话管理
│   │       │   ├── UserController.java        # 用户注册/登录
│   │       │   ├── EmotionController.java      # 情绪历史/趋势查询
│   │       │   ├── CrisisController.java       # 危机预警管理（管理员）
│   │       │   ├── ResourceController.java     # 支持资源库
│   │       │   ├── ModelConfigController.java  # AI模型配置（列表/当前/切换）
│   │       │   └── WelcomeController.java      # 欢迎/问候
│   │       ├── service/      # 业务逻辑（9个接口 + 9个实现）
│   │       ├── entity/       # 数据库实体（11个）
│   │       ├── mapper/       # 数据访问层（9个，继承 BaseMapper）
│   │       ├── interceptor/  # JWT 鉴权 + 角色权限拦截器
│   │       ├── annotation/   # @RequireRole 注解
│   │       ├── config/       # 跨域与拦截器注册
│   │       ├── exception/    # 全局异常处理器
│   │       └── utils/        # JWT 工具类
│   ├── src/main/resources/
│   │   └── application.properties  # 数据库 / JWT / AI 配置
│   ├── init.sql              # 数据库初始化（10张表 + 预置资源数据）
│   └── pom.xml               # Maven 依赖
│
├── 2_vue_frontend/           # Vue 3 前端应用
│   ├── src/
│   │   ├── router/index.js   # 路由 + 全局前置守卫
│   │   ├── stores/user.js    # Pinia 状态管理（登录/登出）
│   │   ├── utils/request.js  # Axios 封装（Token 自动注入）
│   │   └── views/            # 页面组件（Login / Register / Home）
│   └── package.json          # npm 依赖
│
├── 3_ai_engine/              # LLaMA-Factory 本地模型引擎（可选）
│   └── README.md             # 本地部署指南
│
└── README.md                 # 项目总览（本文件）
```

## 核心功能

| 功能模块 | 说明 | 技术亮点 |
|----------|------|----------|
| **匿名账号体系** | 注册/登录，BCrypt 密码加密，JWT 无状态鉴权 | 匿名设计保护用户隐私 |
| **多会话管理** | 创建/切换聊天会话，自动总结会话标题 | AI 异步生成标题，无感刷新 |
| **流式对话（SSE）** | AI 回复逐字打字机效果，不阻塞主线程 | Java 21 HttpClient 异步流式读取 |
| **情绪分析** | 每条消息自动分析情绪类型 + 得分 + 关键词 | 异步调用，不影响对话响应速度 |
| **危机预警** | 独立关键词引擎 + AI System Prompt 双层保障 | 高/中/低三级预警，管理员后台处理 |
| **用户画像** | 统计压力等级、情绪趋势、对话次数 | 基于最近 10 条情绪记录动态计算 |
| **支持资源库** | 按情绪匹配合适的心理技巧、热线、自我练习 | 危机热线高亮展示，管理员可增删改 |
| **AI 模型配置** | 多模型切换（DeepSeek/GPT/Kimi/本地模型） | 用户可自定义偏好，管理员可配置模型参数 |

## 数据库表结构

系统共 **10 张数据表**，完整定义见 `1_java_backend/init.sql`：

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `user` | 匿名用户账号 | username, password(BCrypt), role(USER/ADMIN) |
| `chat_session` | 聊天会话列表 | user_id, title, create_time |
| `chat_message` | 对话消息记录 | session_id, user_id, role, content, create_time |
| `emotion_record` | 情绪分析记录 | emotion_type, emotion_score, keywords, analysis_time |
| `crisis_alert` | 危机预警记录 | alert_level(high/medium/low), alert_type, status, keywords |
| `user_profile` | 用户心理画像 | stress_level, emotional_trend, conversation_count, total_messages |
| `supportive_resource` | 支持性资源内容 | category, trigger_emotion, trigger_score_min/max, priority, enabled |
| `resource_recommendation` | 资源推荐日志 | user_id, resource_id, emotion_type, emotion_score, recommended_at |
| `ai_model_config` | AI模型配置 | code, name, api_url, model_name, temperature, enabled, is_default |
| `user_model_preference` | 用户模型偏好 | user_id, model_code |

## 快速启动

### 环境要求

- JDK 21+
- Maven 3.8+
- Node.js 20+
- MySQL 8.x

### 第一步：初始化数据库

```bash
mysql -u root -p < 1_java_backend/init.sql
```

### 第二步：配置后端

编辑 `1_java_backend/src/main/resources/application.properties`：

```properties
# 数据库账号密码（必须修改）
spring.datasource.username=root
spring.datasource.password=your_password

# AI 能力（通过环境变量注入更安全）
ai.api.key=${AI_API_KEY:sk-your-deepseek-key}

# JWT 密钥（生产环境务必更换，至少32字符）
jwt.secret=your_own_secret_key_at_least_32_chars
```

### 第三步：启动后端

```bash
cd 1_java_backend
./mvnw spring-boot:run
# 验证：http://localhost:8080/welcome
```

### 第四步：启动前端

```bash
cd 2_vue_frontend
npm install
npm run dev
# 访问：http://localhost:5173
```

## 账号说明

| 角色 | 创建方式 | 说明 |
|------|----------|------|
| 普通用户 | 前端注册页面自行注册 | 聊心理话、查看资源库 |
| 管理员 | 在数据库中手动插入 | 处理危机预警、管理资源库 |

管理员账号创建方法（需先在数据库中执行）：

```sql
INSERT INTO `user` (username, password, role, create_time) VALUES
('admin', '$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'ADMIN', NOW());
-- BCrypt 加密密码请使用工具生成，例如在线工具或编程语言库
```

## 项目亮点

1. **AI 打字机效果**：后端 SSE 流式推送，前端 `ReadableStream` 逐字渲染，体验接近真人对聊
2. **危机双层防线**：独立关键词引擎在消息到达瞬间异步检测，AI System Prompt 作为内容层面兜底，双重保障用户安全
3. **完全异步化**：情绪分析、危机检测、标题总结、AI 回复均在不同线程并行执行，互不阻塞主线程
4. **匿名设计**：无需手机号/真实姓名即可使用，降低用户寻求心理帮助的心理门槛
5. **JWT 无状态鉴权**：Token 可带在 HTTP Header 或 URL 参数（适配 SSE GET 请求），灵活安全
6. **多模型支持**：支持 DeepSeek/GPT/Kimi/本地模型一键切换，管理员可配置模型参数

## 注意事项

- 生产环境中敏感信息（API Key、数据库密码、JWT 密钥）务必通过**环境变量**注入，不要硬编码在配置文件中
- `init.sql` 中的 `DROP TABLE` 语句每次运行会清空数据，生产部署前请注释掉这三行
- 危机预警功能为**辅助工具**，不能替代专业心理干预，遇到严重情况请及时拨打热线或就医
- 心理对话数据涉及用户隐私，分析数据集时务必**脱敏处理**，遵守相关法律法规

## 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [DeepSeek API 平台](https://platform.deepseek.com/)
- [LLaMA-Factory GitHub](https://github.com/hiyouga/LLaMA-Factory)
