# 心理支持对话系统 - 后端服务

基于 Spring Boot 3.3 的心理支持对话系统后端，提供用户注册/登录、JWT 鉴权、多会话管理、聊天记录持久化、SSE 流式对话、情绪分析、危机预警、用户画像、支持资源库等完整功能。

AI 能力当前由 DeepSeek API 驱动（OpenAI 兼容接口），支持无缝替换为本地微调模型（LLaMA-Factory）。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | JDK 21，支持虚拟线程等新特性 |
| Spring Boot | 3.3.0 | Web 框架 |
| MyBatis-Plus | 3.5.5 | ORM 持久层，CRUD 零配置 |
| MySQL | 8.x | 关系型数据库，utf8mb4 字符集 |
| JJWT | 0.11.5 | JWT 令牌签发与验签 |
| jBCrypt | 0.4 | 密码单向加密 |
| Lombok | — | 减少样板代码 |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.8+
- MySQL 8.x

### 1. 初始化数据库

```bash
cd 1_java_backend
mysql -u root -p < init.sql
```

> **注意**：`init.sql` 中的 `DROP TABLE` 语句每次运行会清空旧数据。生产环境部署前请注释掉第 15-20 行的 DROP 语句。

### 2. 修改配置文件

文件路径：`src/main/resources/application.properties`

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `spring.datasource.username` | 数据库用户名 | `root` |
| `spring.datasource.password` | 数据库密码 | `root` |
| `ai.api.key` | DeepSeek API Key | `${AI_API_KEY:...}` |
| `jwt.secret` | JWT 签名密钥（生产务必更换） | 内置字符串 |

> **安全提示**：生产环境建议通过环境变量注入敏感配置：`ai.api.key=${AI_API_KEY}`。

### 3. 启动项目

```bash
# 命令行方式
./mvnw spring-boot:run

# 或在 IDE 中运行主类 AiAppJavaApplication
```

### 4. 验证启动成功

```bash
curl http://localhost:8080/welcome
# 返回：🚀 AI App Java 后端运行中！当前时间：xxxx-xx-xx xx:xx:xx
```

---

## 接口文档

### 认证模块（无需登录）

#### 1. 欢迎接口
- **方法**：GET
- **地址**：`/welcome`
- **说明**：返回后端运行状态，无需鉴权

#### 2. 问候接口
- **方法**：GET
- **地址**：`/greet`
- **参数**：`name`（可选，默认"同学"）

#### 3. 用户注册
- **方法**：POST
- **地址**：`/user/register`
- **请求体**：
  ```json
  { "username": "testuser", "password": "password123" }
  ```
- **业务规则**：用户名≥4字符，密码≥6字符，BCrypt 加密存储，username 唯一

#### 4. 用户登录
- **方法**：POST
- **地址**：`/user/login`
- **请求体**：
  ```json
  { "username": "testuser", "password": "password123" }
  ```
- **成功响应**：
  ```json
  {
    "code": 200,
    "status": "success",
    "data": {
      "token": "eyJhbG...",
      "userInfo": { "id": 1, "username": "testuser", "role": "USER" }
    }
  }
  ```

---

### 会话管理模块（需登录）

#### 5. 创建会话
- **方法**：POST
- **地址**：`/chat/session/create`
- **Header**：`Authorization: Bearer <token>`
- **请求体**（可选）：
  ```json
  { "title": "考研压力咨询" }
  ```
- **默认标题**：`"新的心理探索"`（AI 会在首次对话后自动生成更合适的标题）

#### 6. 获取会话列表
- **方法**：GET
- **地址**：`/chat/session/list`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回当前用户所有会话，按创建时间倒序排列

#### 7. 查询历史消息
- **方法**：GET
- **地址**：`/chat/history?sessionId={id}`
- **Header**：`Authorization: Bearer <token>`
- **业务规则**：校验 session 归属权，用户只能查询自己的会话

---

### 聊天对话模块（需登录）

#### 8. 流式对话（SSE 打字机效果）
- **方法**：GET
- **地址**：`/chat/stream?sessionId={id}&content={内容}`
- **Header**：`Authorization: Bearer <token>`
- **返回类型**：`text/event-stream`
- **Token 传参方式**：Header 或 URL 参数 `?token=xxx`（SSE GET 请求需特殊处理）
- **说明**：
  - 用户消息先落库，再异步执行情绪分析 + 危机检测
  - AI 回复以 SSE 流式逐字推送前端
  - 完整回复保存到数据库
  - AI 自动异步生成会话标题

> **SSE 测试命令**：
> ```bash
> curl -N -H "Authorization: Bearer <token>" \
>   "http://localhost:8080/chat/stream?sessionId=1&content=你好"
> ```

#### 9. 同步对话（非流式，备用接口）
- **方法**：POST
- **地址**：`/chat/send`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "sessionId": 1, "content": "你好" }
  ```
- **说明**：AI 回复完整返回，不走 SSE。适合非实时对话场景。

---

### 情绪分析模块（需登录）

#### 10. 获取会话情绪历史
- **方法**：GET
- **地址**：`/emotion/session/{sessionId}`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回该会话所有用户消息的情绪分析记录，按时间倒序

#### 11. 获取用户情绪变化趋势
- **方法**：GET
- **地址**：`/emotion/trend?days=7`
- **Header**：`Authorization: Bearer <token>`
- **参数**：`days`（可选，默认7天）
- **说明**：返回近 N 天内所有情绪分析记录，用于绘制情绪变化图表

#### 12. 获取会话最新情绪状态
- **方法**：GET
- **地址**：`/emotion/latest/{sessionId}`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回该会话最近一条情绪分析结果

---

### 危机预警模块

#### 13. 获取待处理预警（仅管理员）
- **方法**：GET
- **地址**：`/crisis/pending`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **说明**：管理员获取所有待处理（pending）危机预警记录

#### 14. 处理危机预警（仅管理员）
- **方法**：POST
- **地址**：`/crisis/handle/{alertId}`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **请求体**：
  ```json
  { "handlerNotes": "已电话联系用户，确认安全" }
  ```
- **说明**：将预警状态从 `pending` 更新为 `handled`，记录处理时间和备注

#### 15. 获取我的危机预警记录
- **方法**：GET
- **地址**：`/crisis/user/alerts`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回当前用户自己触发的所有危机预警记录（含已处理和待处理的）

---

### 支持资源库模块

#### 16. 根据情绪推荐资源
- **方法**：GET
- **地址**：`/resource/recommend?emotionType=depression&emotionScore=0.3`
- **Header**：`Authorization: Bearer <token>`
- **参数**：`emotionType`（情绪类型），`emotionScore`（0.0~1.0）
- **说明**：根据情绪类型和得分匹配并返回推荐资源，危机热线（`crisis`）自动高亮置顶

#### 17. 获取所有启用的资源（用户浏览）
- **方法**：GET
- **地址**：`/resource/all?category=crisis`
- **Header**：`Authorization: Bearer <token>`
- **参数**：`category`（可选，资源大类筛选）

#### 18. 获取资源详情
- **方法**：GET
- **地址**：`/resource/detail/{id}`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回指定资源的完整内容

#### 19. 查看推荐记录
- **方法**：GET
- **地址**：`/resource/my/recommendations`
- **Header**：`Authorization: Bearer <token>`
- **说明**：用户查看自己的资源推荐记录

#### 20. 分页查询资源列表（仅管理员）
- **方法**：GET
- **地址**：`/resource/admin/list?category=crisis&pageNum=1&pageSize=10`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **参数**：`category`（可选），`pageNum`（默认1），`pageSize`（默认10）

#### 21. 新增资源（仅管理员）
- **方法**：POST
- **地址**：`/resource/admin/add`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **请求体**：
  ```json
  {
    "category": "selfhelp",
    "resourceType": "tips",
    "title": "睡前放松练习",
    "content": "找一个舒服的姿势...",
    "triggerEmotion": "anxiety",
    "triggerScoreMin": 0.0,
    "triggerScoreMax": 0.5,
    "applicableScene": "焦虑失眠时",
    "priority": 25,
    "enabled": 1
  }
  ```

#### 22. 更新资源（仅管理员）
- **方法**：PUT
- **地址**：`/resource/admin/update/{id}`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **请求体**：同上

#### 23. 删除资源（仅管理员）
- **方法**：DELETE
- **地址**：`/resource/admin/delete/{id}`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）

#### 24. 启用/禁用资源（仅管理员）
- **方法**：PUT
- **地址**：`/resource/admin/toggle/{id}`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **请求体**：
  ```json
  { "enabled": 0 }
  ```

#### 25. 查看所有推荐记录（仅管理员）
- **方法**：GET
- **地址**：`/resource/admin/recommendations?emotionType=depression&emotionScore=0.3`
- **Header**：`Authorization: Bearer <token>`（需 ADMIN 角色）
- **说明**：管理员统计分析，可按情绪类型和得分过滤

---

### 模型配置模块

#### 26. 获取可选模型列表
- **方法**：GET
- **地址**：`/model/list`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回所有启用的 AI 模型配置列表

#### 27. 获取当前模型
- **方法**：GET
- **地址**：`/model/current`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回当前用户选定的 AI 模型配置

#### 28. 切换模型
- **方法**：POST
- **地址**：`/model/select`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "modelCode": "DEEPSEEK" }
  ```
- **说明**：保存用户的模型偏好，下次对话自动使用

---

## 数据库表结构

### 1. user（用户表）
```sql
CREATE TABLE `user` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `username`    varchar(50)  NOT NULL COMMENT '匿名用户名',
  `password`    varchar(100) NOT NULL COMMENT 'BCrypt加密密码',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  `role`        varchar(20)  NOT NULL COMMENT 'USER 或 ADMIN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';
```

### 2. chat_session（会话表）
```sql
CREATE TABLE `chat_session` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `user_id`     bigint       NOT NULL,
  `title`       varchar(100) NOT NULL COMMENT '会话标题',
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会话表';
```

### 3. chat_message（消息表）
```sql
CREATE TABLE `chat_message` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `session_id`  bigint       NOT NULL,
  `user_id`     bigint       NOT NULL,
  `role`        varchar(20)  NOT NULL COMMENT 'user 或 assistant',
  `content`     text         NOT NULL,
  `create_time` datetime     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息表';
```

### 4. emotion_record（情绪记录表）
```sql
CREATE TABLE `emotion_record` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `user_id`       bigint       NOT NULL,
  `session_id`    bigint       NOT NULL,
  `message_id`    bigint       NOT NULL,
  `emotion_type`  varchar(20)           COMMENT 'positive/negative/neutral/anxiety/depression/anger',
  `emotion_score` double                 COMMENT '0.0~1.0',
  `keywords`      text,
  `analysis_time` datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`),
  KEY `idx_analysis_time` (`analysis_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='情绪记录表';
```

### 5. crisis_alert（危机预警表）
```sql
CREATE TABLE `crisis_alert` (
  `id`            bigint       NOT NULL AUTO_INCREMENT,
  `user_id`       bigint       NOT NULL,
  `session_id`    bigint       NOT NULL,
  `message_id`    bigint,
  `alert_level`   varchar(10)           COMMENT 'high/medium/low',
  `alert_type`    varchar(50)           COMMENT '自杀倾向/自残倾向/消极情绪',
  `keywords`      text,
  `status`        varchar(20)           COMMENT 'pending/handled/resolved',
  `created_at`    datetime,
  `handled_at`    datetime,
  `handler_notes` text,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机预警表';
```

### 6. user_profile（用户画像表）
```sql
CREATE TABLE `user_profile` (
  `id`                  bigint       NOT NULL AUTO_INCREMENT,
  `user_id`            bigint       NOT NULL UNIQUE,
  `personality_type`   varchar(20),
  `main_concern`       varchar(100),
  `stress_level`       varchar(10) COMMENT 'high/medium/low',
  `emotional_trend`    varchar(10) COMMENT 'rising/falling/stable',
  `conversation_count` int  DEFAULT 0,
  `total_messages`     int  DEFAULT 0,
  `last_active_time`   datetime,
  `updated_at`         datetime,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户画像表';
```

### 7. supportive_resource（支持性资源表）
```sql
CREATE TABLE `supportive_resource` (
  `id`               bigint       NOT NULL AUTO_INCREMENT,
  `category`         varchar(30)           COMMENT 'crisis/counseling/selfhelp/mindfulness/tips',
  `resource_type`    varchar(20)           COMMENT 'hotline/center/exercise/tips/article',
  `title`            varchar(100) NOT NULL,
  `content`          text,
  `trigger_emotion`  varchar(50)           COMMENT 'depression/anxiety/anger/all',
  `trigger_score_min` double,
  `trigger_score_max` double,
  `applicable_scene` varchar(200),
  `priority`         int  DEFAULT 99 COMMENT '越小越优先',
  `enabled`          int  DEFAULT 1 COMMENT '1启用 0禁用',
  `create_time`      datetime,
  `update_time`      datetime,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_priority` (`priority`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支持性资源表';
```

### 8. resource_recommendation（资源推荐记录表）
```sql
CREATE TABLE `resource_recommendation` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `user_id`       bigint       NOT NULL,
  `session_id`    bigint,
  `resource_id`   bigint       NOT NULL,
  `emotion_type`  varchar(20),
  `emotion_score` double,
  `recommended_at` datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_resource_id` (`resource_id`),
  KEY `idx_recommended_at` (`recommended_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源推荐记录表';
```

### 9. ai_model_config（AI 模型配置表）
```sql
CREATE TABLE `ai_model_config` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `code`         varchar(30)  NOT NULL COMMENT '模型代码，如 DEEPSEEK/OPENAI/KIMI',
  `name`         varchar(50)  NOT NULL COMMENT '模型显示名称',
  `description`  varchar(200),
  `api_url`      varchar(200) NOT NULL COMMENT 'API 端点地址',
  `api_key_alias` varchar(50) NOT NULL COMMENT 'API Key 配置别名',
  `model_name`   varchar(100) NOT NULL COMMENT '实际模型名称',
  `temperature`  double  DEFAULT 0.7,
  `max_tokens`   int  DEFAULT 2000,
  `enabled`      int  DEFAULT 1 COMMENT '1启用 0禁用',
  `is_default`   int  DEFAULT 0 COMMENT '1默认 0非默认',
  `sort_order`   int  DEFAULT 99,
  `create_time`  datetime,
  `update_time`  datetime,
  PRIMARY KEY (`id`),
  KEY `idx_enabled` (`enabled`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI模型配置表';
```

### 10. user_model_preference（用户模型偏好表）
```sql
CREATE TABLE `user_model_preference` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `user_id`     bigint       NOT NULL UNIQUE,
  `model_code`  varchar(30)  NOT NULL COMMENT '关联ai_model_config.code',
  `updated_at`   datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户模型偏好表';
```

---

## 鉴权机制

### JWT Token

登录成功后，服务端签发 JWT Token，Payload 中包含 `userId`、`username`、`role`。Token 默认有效期 **24 小时**。

请求时携带 Token 的两种方式：

```
# 方式一：HTTP Header（推荐）
Authorization: Bearer eyJhbG...

# 方式二：URL 参数（用于 SSE 流式 GET 请求）
/chat/stream?sessionId=1&content=xxx&token=eyJhbG...
```

### 拦截器配置（WebMvcConfig）

| 路径模式 | 是否鉴权 | 角色要求 | 说明 |
|----------|----------|----------|------|
| `/welcome`、`/greet` | 否 | — | 公开测试接口 |
| `/user/login`、`/user/register` | 否 | — | 认证接口 |
| `/chat/**` | 是 | USER/ADMIN | 聊天接口 |
| `/emotion/**` | 是 | USER/ADMIN | 情绪分析接口 |
| `/crisis/**` | 是 | 需区分 | `pending`、`/handle/*` 仅 ADMIN；`/user/alerts` 需登录 |
| `/resource/**` | 是 | 需区分 | `/admin/**` 仅 ADMIN；其他需登录 |
| `/model/**` | 是 | USER/ADMIN | 模型配置接口 |

> 角色权限通过 `@RequireRole({"ADMIN"})` 注解控制，落在 `RoleInterceptor` 中实现。

---

## 核心服务说明

### 1. 危机检测（CrisisDetectionService）

**双层防线机制**，确保用户安全：

**第一层：独立关键词引擎**（`CrisisDetectionServiceImpl`）
- 消息到达后立即在独立线程中执行，不等待 AI 回复
- 基于关键词匹配，分为三级：
  - **高危**（high）：`想死`、`自杀`、`zs`、`si` 等 → 立即预警
  - **中危**（medium）：`自残`、`暴力`、`报复社会`、`崩溃` 等
  - **低危**（low）：`好累`、`撑不住`、`没希望了` 等
- 检测到危机后立即写入 `crisis_alert` 表，管理员可在后台处理

**第二层：AI System Prompt**（`AiServiceImpl`）
- AI 在回复内容层面检测危机表达
- 触发时 AI 输出固定格式的危机干预话术，并附带热线电话

### 2. 情绪分析（EmotionAnalysisService）

- 每条用户消息发送后自动触发，异步执行，不阻塞 AI 回复
- 调用 DeepSeek API，返回结构化 JSON：
  ```json
  {
    "emotionType": "depression",
    "emotionScore": 0.25,
    "keywords": "绝望, 无助, 失落"
  }
  ```
- 结果持久化到 `emotion_record` 表，供前端绘制情绪趋势图

### 3. 用户画像（UserProfileService）

- 基于最近 10 条情绪记录动态计算：
  - **压力等级**（`stress_level`）：avg(score)>0.6 → low，<0.4 → high，否则 medium
  - **情绪趋势**（`emotional_trend`）：比较最近3条与更早记录，rising/falling/stable
- 用户可自行更新人格类型和主要困扰字段

### 4. 支持资源库（ResourceService）

- 资源按情绪类型 + 得分区间匹配：
  - `trigger_emotion` 匹配情绪类型
  - `trigger_score_min <= score <= trigger_score_max`
  - `enabled = 1`
  - 按 `priority` 升序排列
- 危机热线（`category=crisis`）固定高亮展示在最顶部
- 每推荐一次自动记录到 `resource_recommendation` 表，供统计分析

### 5. AI 流式对话（SSE）

- 后端使用 Java 21 `HttpClient` 的 `sendAsync` 发起异步请求
- 流式读取 AI 返回，逐字通过 `SseEmitter` 推送前端
- 前端使用原生 `ReadableStream` 逐字渲染，实现打字机效果
- 完整回复保存到数据库，完成后自动触发 AI 总结会话标题

---

## 项目目录

```
src/main/java/com/example/ai_app_java/
├── AiAppJavaApplication.java           # 启动类
│
├── annotation/
│   └── RequireRole.java               # 角色权限注解
│
├── config/
│   └── WebMvcConfig.java             # 跨域配置 + 拦截器注册
│
├── controller/
│   ├── ChatController.java           # 会话管理 + 历史消息 + SSE流式对话
│   ├── CrisisController.java          # 危机预警管理（管理员）
│   ├── EmotionController.java         # 情绪历史/趋势/最新状态
│   ├── ModelConfigController.java     # AI模型配置（列表/当前/切换）
│   ├── ResourceController.java        # 支持资源库 CRUD
│   ├── UserController.java            # 注册/登录
│   └── WelcomeController.java         # 欢迎/问候
│
├── entity/
│   ├── AiModelConfig.java            # AI模型配置实体
│   ├── ChatMessage.java              # 消息实体
│   ├── ChatSession.java              # 会话实体
│   ├── CrisisAlert.java              # 危机预警实体
│   ├── EmotionRecord.java            # 情绪记录实体
│   ├── ResourceRecommendation.java   # 资源推荐记录实体
│   ├── Result.java                   # 统一响应封装 {code, status, msg, data}
│   ├── SupportiveResource.java       # 支持资源实体
│   ├── User.java                     # 用户实体
│   ├── UserModelPreference.java      # 用户模型偏好实体
│   └── UserRequest.java              # 用户请求 DTO
│
├── exception/
│   └── GlobalExceptionHandler.java   # 全局异常处理器
│
├── interceptor/
│   ├── JwtInterceptor.java           # JWT 验签 + 注入 currentUserId
│   └── RoleInterceptor.java         # 角色权限校验
│
├── mapper/
│   ├── AiModelConfigMapper.java      # AI模型配置 Mapper
│   ├── ChatMessageMapper.java        # 消息 Mapper
│   ├── ChatSessionMapper.java        # 会话 Mapper
│   ├── CrisisAlertMapper.java        # 危机预警 Mapper
│   ├── EmotionRecordMapper.java      # 情绪记录 Mapper
│   ├── ResourceRecommendationMapper.java  # 资源推荐 Mapper
│   ├── SupprotiveResourceMapper.java    # 支持资源 Mapper
│   ├── UserMapper.java              # 用户 Mapper
│   └── UserModelPreferenceMapper.java   # 用户模型偏好 Mapper
│
├── service/
│   ├── AiModelConfigService.java    # AI模型配置服务接口
│   ├── AiService.java               # AI 大模型服务接口
│   ├── ChatMessageService.java      # 消息服务接口
│   ├── ChatSessionService.java      # 会话服务接口
│   ├── CrisisDetectionService.java  # 危机检测服务接口
│   ├── EmotionAnalysisService.java  # 情绪分析服务接口
│   ├── ResourceService.java         # 资源服务接口
│   ├── UserModelPreferenceService.java  # 用户模型偏好服务接口
│   └── UserService.java             # 用户服务接口
│
└── service/impl/
    ├── AiModelConfigServiceImpl.java    # AI模型配置服务实现
    ├── AiServiceImpl.java            # AI 服务（含流式调用）
    ├── ChatMessageServiceImpl.java  # 消息服务实现
    ├── ChatSessionServiceImpl.java  # 会话服务实现
    ├── CrisisDetectionServiceImpl.java  # 危机检测（含关键词引擎）
    ├── EmotionAnalysisServiceImpl.java  # 情绪分析服务实现
    ├── ResourceServiceImpl.java      # 资源服务实现
    ├── UserModelPreferenceServiceImpl.java  # 用户模型偏好服务实现
    └── UserServiceImpl.java         # 用户服务实现
```

---

## 调试与测试

### 接口测试（test.http）

项目根目录下的 `test.http` 文件可用 IntelliJ IDEA 或 VS Code REST Client 插件直接运行：

```bash
# 需要先在 test.http 中填入登录接口返回的 token
GET http://localhost:8080/chat/session/list
Authorization: Bearer <your-token>
```

### SSE 流式测试

```bash
curl -N -H "Authorization: Bearer <token>" \
  "http://localhost:8080/chat/stream?sessionId=1&content=你好"
```

### 日志说明

开发环境下 SQL 语句默认输出到控制台（`mybatis-plus.configuration.log-impl`），便于调试。生产环境请在 `application.properties` 中删除该行。

---

## 配置参考

`src/main/resources/application.properties` 完整配置项说明：

```properties
# 应用名
spring.application.name=ai_app_java

# 数据库
spring.datasource.url=jdbc:mysql://localhost:3306/mental_health_db?serverTimezone=GMT%2B8&characterEncoding=utf-8&useSSL=false
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# MyBatis-Plus
mybatis-plus.configuration.log-impl=org.apache.ibatis.logging.stdout.StdOutImpl
mybatis-plus.type-aliases-package=com.example.ai_app_java.entity

# JWT
jwt.secret=your_secret_key_at_least_32_characters
jwt.expiration=86400000  # 24小时

# AI 大模型（DeepSeek）
ai.api.url=https://api.deepseek.com/v1/chat/completions
ai.api.key=${AI_API_KEY:sk-your-api-key}
ai.api.model=deepseek-chat
```
