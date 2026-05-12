# 心理支持对话系统 - 后端服务

> 基于 Spring Boot 3.3 的心理支持对话系统后端，提供用户注册/登录、JWT 鉴权、多会话管理、聊天记录持久化、SSE 流式对话、情绪分析、危机预警、用户画像、支持资源库、意图分类、认知投票、行为打卡、心理准备度评估、疗效评估等完整功能。

AI 能力支持 DeepSeek / OpenAI / Kimi / 本地模型（均采用 OpenAI 兼容接口），通过数据库 `ai_model_config` 表灵活配置，前端用户可一键切换。

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

- JDK 17+
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
# Maven Wrapper 方式（无需预装 Maven）
cd 1_java_backend
./mvnw spring-boot:run

# 或在 IDE 中直接运行主类 AiAppJavaApplication
```

### 4. Maven 构建（生产打包）

```bash
# 跳过测试打包
./mvnw clean package -DskipTests

# 产物位于 target/ai_app_java-0.0.1-SNAPSHOT.jar
java -jar target/ai_app_java-0.0.1-SNAPSHOT.jar
```

### 5. 验证启动成功

```bash
curl http://localhost:8080/welcome
# 返回：🚀 AI App Java 后端运行中！当前时间：xxxx-xx-xx xx:xx:xx
```

### 6. 生产环境部署建议

| 项目 | 建议配置 |
|------|----------|
| JVM Heap | `-Xms512m -Xmx1024m` |
| MySQL 连接池 | HikariCP 默认（`spring.datasource.hikari.*` 可覆盖） |
| 日志 | 生产环境建议关闭 `log-impl`（减少 IO），配置 Logback 文件输出 |
| 敏感配置 | 通过环境变量或 `secrets.properties` 注入，禁止提交到代码仓库 |
| 反向代理 | 建议在前方部署 Nginx（SSL 终结 + 负载均衡） |

> **Docker 部署示例**（`Dockerfile` 需自行在项目根目录创建）：
> ```dockerfile
> FROM eclipse-temurin:21-jre-alpine
> WORKDIR /app
> COPY target/ai_app_java-*.jar app.jar
> COPY secrets.properties .
> EXPOSE 8080
> ENTRYPOINT ["java", "-jar", "app.jar"]
> ```

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
- **地址**：`/chat/stream?sessionId={id}&content={内容}&modelCode={code}`
- **Header**：`Authorization: Bearer <token>` 或 URL 参数 `?token=xxx`（SSE GET 请求特殊处理）
- **返回类型**：`text/event-stream`
- **对话流程**：
  1. 用户消息落库
  2. **同步执行**情绪分析（拿到 emotionType/score/keywords）
  3. **异步**危机检测（不阻塞 AI 回复）
  4. 调用 `buildDynamicStrategy()` 根据情绪构建资源上下文，注入 system prompt
  5. 从 DB 加载最近 20 条历史消息（倒序取→反转→oldest→newest），构建完整上下文（system + history + current）
  6. 发送 AI 请求，流式推送回复
  7. 回复保存到 DB，AI 异步生成会话标题

> **SSE 测试命令**：
> ```bash
> curl -N -H "Authorization: Bearer <token>" \
>   "http://localhost:8080/chat/stream?sessionId=1&content=你好"
> ```


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

### 意图分类与角色调度模块

#### 29. 重构用户意图
- **方法**：POST
- **地址**：`/intent/reconstruct`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "userInput": "我最近压力很大", "emotionType": "anxiety", "emotionScore": 0.4 }
  ```
- **说明**：基于用户输入和情绪状态重构临床意图，返回潜在需求、临床意图、疗法模块、置信度

#### 30. 获取所有意图分类
- **方法**：GET
- **地址**：`/intent/list`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回系统支持的所有 11 种临床意图分类

#### 31. 获取所有治疗角色
- **方法**：GET
- **地址**：`/intent/roles`
- **Header**：`Authorization: Bearer <token>`
- **说明**：返回系统支持的所有 AI 治疗角色（supportive / empathetic / socratic / guided / crisis_mode / dynamic_switcher）

#### 32. 确定推荐角色
- **方法**：POST
- **地址**：`/intent/determineRole`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "userId": 1, "sessionId": 1, "intent": "existential_crisis", "emotionType": "depression", "emotionScore": 0.3, "prsScore": 0.5 }
  ```
- **说明**：根据意图、情绪得分、PRS 得分等多维度确定推荐的治疗角色

---

### 认知投票模块（CBT）

#### 33. 提交投票
- **方法**：POST
- **地址**：`/voting`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "votingType": "thought_distortion", "question": "面对压力时，你通常会：", "selectedOption": "积极面对，想办法解决", "sessionId": 1 }
  ```

#### 34. 获取投票历史
- **方法**：GET
- **地址**：`/voting/history?limit=20`
- **Header**：`Authorization: Bearer <token>`
- **参数**：`limit`（默认20条）

#### 35. 按会话获取投票记录
- **方法**：GET
- **地址**：`/voting/session/{sessionId}`
- **Header**：`Authorization: Bearer <token>`

#### 36. 获取下一个投票问题
- **方法**：GET
- **地址**：`/voting/next?emotionType=depression&recentType=`
- **Header**：`Authorization: Bearer <token>`
- **参数**：`emotionType`（情绪类型），`recentType`（最近投票类型，避免重复）

#### 37. 检查是否应触发投票
- **方法**：GET
- **地址**：`/voting/shouldTrigger?emotionType=depression&emotionScore=0.35`
- **Header**：`Authorization: Bearer <token>`
- **说明**：根据情绪类型和得分判断是否应触发认知投票

---

### 行为打卡模块

#### 38. 提交打卡
- **方法**：POST
- **地址**：`/checkin`
- **Header**：`Authorization: Bearer <token>`
- **请求体**：
  ```json
  { "checkinType": "mood", "checkinValue": "happy", "note": "今天心情很好！" }
  ```
- **打卡类型**：`mood`（心情）/ `sleep`（睡眠）/ `exercise`（运动）/ `social`（社交）

#### 39. 获取连续打卡天数
- **方法**：GET
- **地址**：`/checkin/streak`
- **Header**：`Authorization: Bearer <token>`

#### 40. 获取打卡统计
- **方法**：GET
- **地址**：`/checkin/stats?days=7`
- **Header**：`Authorization: Bearer <token>`
- **返回**：`stats`（各类型打卡次数）/ `streak`（连续天数）/ `activeScore`（活跃度评分）

#### 41. 获取最近打卡记录
- **方法**：GET
- **地址**：`/checkin/recent?days=7`
- **Header**：`Authorization: Bearer <token>`

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

### 11. intent_classification（意图分类表）
```sql
CREATE TABLE `intent_classification` (
  `id`                  bigint       NOT NULL AUTO_INCREMENT,
  `code`                varchar(50)  NOT NULL COMMENT '意图代码',
  `name`                varchar(50)  NOT NULL COMMENT '意图名称',
  `description`         varchar(200),
  `therapy_dimensions`  varchar(50)  COMMENT '关联疗法维度: CBT,ACT,DBT',
  `ai_role`            varchar(30)  COMMENT '对应角色',
  `priority`            int  DEFAULT 99,
  PRIMARY KEY (`id`),
  KEY `idx_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意图分类表';
```

### 12. cognitive_voting（认知投票表）
```sql
CREATE TABLE `cognitive_voting` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `user_id`         bigint       NOT NULL,
  `voting_type`     varchar(30)  NOT NULL COMMENT '投票类型: thought_distortion/self_efficacy/coping_strategy',
  `question`        varchar(200) NOT NULL,
  `selected_option` varchar(200) NOT NULL,
  `session_id`      bigint,
  `created_at`      datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='认知投票表';
```

### 13. user_behavior_checkin（行为打卡表）
```sql
CREATE TABLE `user_behavior_checkin` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `user_id`        bigint       NOT NULL,
  `checkin_type`   varchar(20)  NOT NULL COMMENT '打卡类型: mood/sleep/exercise/social',
  `checkin_value`  varchar(30)  NOT NULL COMMENT '打卡值',
  `note`           varchar(200),
  `checkin_date`   date         NOT NULL,
  `created_at`     datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_checkin_date` (`checkin_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='行为打卡表';
```

### 14. psychological_readiness_score（心理准备度得分表）
```sql
CREATE TABLE `psychological_readiness_score` (
  `id`                  bigint       NOT NULL AUTO_INCREMENT,
  `user_id`             bigint       NOT NULL,
  `session_id`          bigint       NOT NULL,
  `message_id`          bigint       NOT NULL,
  `total_score`         double       COMMENT 'PRS总分 0~1',
  `engagement_score`    double       COMMENT '参与度量表 0~1',
  `valence_score`       double       COMMENT '情感价态 -1~1',
  `arousal_score`       double       COMMENT '唤醒度 0~1',
  `intervention_depth`  varchar(20)  COMMENT '干预深度: scaffolding/supportive/reflective',
  `calculated_at`        datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心理准备度得分表';
```

### 15. crisis_sample（危机样本表）
```sql
CREATE TABLE `crisis_sample` (
  `id`        bigint       NOT NULL AUTO_INCREMENT,
  `text`      text         NOT NULL COMMENT '危机样本文本',
  `vector`    text         COMMENT 'BGE向量(JSON数组字符串)',
  `category`  varchar(30)  COMMENT '分类: suicide/selfharm/violence/anxiety',
  `priority`  int  DEFAULT 99,
  `enabled`   int  DEFAULT 1 COMMENT '1启用 0禁用',
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='危机样本表';
```

### 16. user_interaction_style（用户互动风格表）
```sql
CREATE TABLE `user_interaction_style` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `user_id`         bigint       NOT NULL UNIQUE,
  `preferred_style` varchar(20)  COMMENT '偏好风格: autonomous/guided/mixed',
  `autonomous_score` double,
  `guided_score`     double,
  `mixed_score`      double,
  `recent_style`    varchar(20),
  `updated_at`       datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户互动风格表';
```

### 17. therapy_evaluation（AI疗效评估表）
```sql
CREATE TABLE `therapy_evaluation` (
  `id`              bigint       NOT NULL AUTO_INCREMENT,
  `user_id`         bigint       NOT NULL,
  `session_id`      bigint       NOT NULL,
  `message_id`      bigint       NOT NULL,
  `model_code`      varchar(30),
  `css_score`       double COMMENT '认知支持得分 0~1',
  `ars_score`       double COMMENT '情感共鸣得分 0~1',
  `clinical_intent` varchar(50),
  `therapy_module`  varchar(30),
  `intervention_depth` varchar(20),
  `ai_role`        varchar(30),
  `created_at`      datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI疗效评估表（MentalAlign框架）';
```

### 18. user_satisfaction（用户满意度表）
```sql
CREATE TABLE `user_satisfaction` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `user_id`        bigint       NOT NULL,
  `session_id`     bigint       NOT NULL,
  `model_code`     varchar(30),
  `happiness`      double COMMENT '满意度 0~1',
  `engagement`     double COMMENT '参与度 0~1',
  `adoption`       double COMMENT '接受度 0~1',
  `retention`      double COMMENT '留存率 0~1',
  `task_success`   double COMMENT '任务成功率 0~1',
  `created_at`     datetime,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_session_id` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户满意度表（HEART框架）';
```

### 19. model_effectiveness_stats（模型效果统计表）
```sql
CREATE TABLE `model_effectiveness_stats` (
  `id`             bigint       NOT NULL AUTO_INCREMENT,
  `model_code`     varchar(30)  NOT NULL,
  `stat_type`      varchar(20),
  `avg_css`        double,
  `avg_ars`        double,
  `avg_happiness`  double,
  `avg_engagement` double,
  `retention_rate` double,
  `total_count`    int,
  `period_start`   datetime,
  `period_end`     datetime,
  `created_at`     datetime,
  PRIMARY KEY (`id`),
  KEY `idx_model_code` (`model_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模型效果统计表';
```

---

## 模型疗效评估模块（MentalAlign + HEART）

### 42. 获取疗效统计数据
- **方法**：GET
- **地址**：`/evaluation/therapy/stats?days=7`
- **Header**：`Authorization: Bearer <token>`

### 43. 模型横向对比
- **方法**：GET
- **地址**：`/evaluation/therapy/compare?days=7`
- **Header**：`Authorization: Bearer <token>`

### 44. 获取疗效趋势
- **方法**：GET
- **地址**：`/evaluation/therapy/trend?days=30`
- **Header**：`Authorization: Bearer <token>`

### 45. 获取疗效历史
- **方法**：GET
- **地址**：`/evaluation/therapy/history`
- **Header**：`Authorization: Bearer <token>`

### 46. 提交用户评分
- **方法**：POST
- **地址**：`/evaluation/therapy/rating`
- **Header**：`Authorization: Bearer <token>`

### 47. 提交满意度
- **方法**：POST
- **地址**：`/evaluation/satisfaction`
- **Header**：`Authorization: Bearer <token>`

### 48. 快速满意度提交
- **方法**：POST
- **地址**：`/evaluation/satisfaction/quick`
- **Header**：`Authorization: Bearer <token>`

### 49. 获取满意度历史
- **方法**：GET
- **地址**：`/evaluation/satisfaction/history`
- **Header**：`Authorization: Bearer <token>`

### 50. 获取用户统计
- **方法**：GET
- **地址**：`/evaluation/satisfaction/user-stats`
- **Header**：`Authorization: Bearer <token>`

### 51. 获取模型 HEART 统计
- **方法**：GET
- **地址**：`/evaluation/satisfaction/model`
- **Header**：`Authorization: Bearer <token>`

### 52. 模型 HEART 对比
- **方法**：GET
- **地址**：`/evaluation/satisfaction/compare`
- **Header**：`Authorization: Bearer <token>`

### 53. 获取满意度趋势
- **方法**：GET
- **地址**：`/evaluation/satisfaction/trend`
- **Header**：`Authorization: Bearer <token>`

### 54. 获取平台级统计
- **方法**：GET
- **地址**：`/evaluation/satisfaction/platform?days=7`
- **Header**：`Authorization: Bearer <token>`

### 55. 检查满意度是否已提交
- **方法**：GET
- **地址**：`/evaluation/satisfaction/check?sessionId={id}`
- **Header**：`Authorization: Bearer <token>`

### 56. 获取综合评估报告
- **方法**：GET
- **地址**：`/evaluation/report?sessionId={id}`
- **Header**：`Authorization: Bearer <token>`

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
| `/resource-repo/**` | 是 | 需区分 | `/admin/**` 仅 ADMIN；其他需登录 |
| `/model/**` | 是 | USER/ADMIN | 模型配置接口 |

> 角色权限通过 `@RequireRole({"ADMIN"})` 注解控制，落在 `RoleInterceptor` 中实现。

---

## 核心服务说明

### 1. 危机检测（GuardianService + CrisisDetectionService）

**三层防线机制**，确保用户安全：

**第一层：Guardian 安全层**（`GuardianService`）
- PHQ-9 第9项语义检测（自伤意念）
- 向量语义相似度检测（调用 VectorSimilarityService，与 crisis_sample 库比对）
- 关系脉络门控（人称代词 + 高危情境词组合检测）
- 关键词硬匹配（扩充规模）
- 四层并行检测，返回综合风险等级（high/medium/low/none）

**第二层：Reflector 安全层**（`ReflectorService`）
- AI 回复合规性审计
- 检测违规类型：自我伤害建议 / 医疗处方 / 歧视性语言 / 伦理违规
- 发现违规时替换为安全替代回复

**第三层：AI System Prompt**（`AiServiceImpl`）
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
- **对话集成**：`streamChat` 每轮对话时调用 `buildDynamicStrategy()`，根据当前情绪类型从 `resource_repository` 选取匹配的资源库策略片段（策略文本 + 最相关的练习名称），注入 AI 的 system prompt，让 AI 在回复中自然融入引导性练习建议，而非机械罗列资源列表
- 危机热线（`category=crisis`）固定高亮展示在最顶部
- 每推荐一次自动记录到 `resource_recommendation` 表，供统计分析

### 5. AI 流式对话（SSE）

- 后端使用 Java 21 `HttpClient` 的 `sendAsync` 发起异步请求
- 流式读取 AI 返回，逐字通过 `SseEmitter` 推送前端
- 完整回复保存到数据库，完成后自动触发 AI 总结会话标题
- **多轮上下文**：从 `chat_message` 表加载最近 20 条历史消息（SQL 按 `create_time DESC LIMIT 20` 取→内存反转→oldest→newest 顺序注入 system prompt），确保 AI 能正确理解对话进展
- **情绪驱动资源注入**：情绪分析结果同步传入 `buildDynamicStrategy()`，生成轻量级资源上下文（策略片段 + 练习名称），AI 根据对话语境自主决定是否引入、如何引入引导性练习
- **多模型支持**：`streamChat` 通过 `modelCode` 参数动态加载对应模型配置（API URL / Key / 模型名），用户切换模型后立即生效

### 6. 意图分类与重构（IntentReconstructService）

- 根据用户输入和情绪状态，使用 AI 重构临床意图
- 支持 11 种意图分类：existential_crisis / value_clarification / cognitive_restructuring / behavioral_activation / emotion_regulation / distress_tolerance / social_skill / grief_processing / sleep_hygiene / self_compassion / crisis_stabilization
- 每种意图关联疗法维度（ACT/CBT/DBT）和推荐 AI 角色
- 返回潜在需求、临床意图、疗法模块、置信度

### 7. 动态角色调度（RoleSchedulerService）

- 根据意图类型、情绪得分、PRS 准备度动态选择 AI 治疗角色
- 角色类型：supportive（支持引导者）/ empathetic（共情倾听者）/ socratic（苏格拉底式引导者）/ guided（指导型）/ crisis_mode（危机支持者）/ dynamic_switcher（动态角色切换者）
- 每个角色有对应的 prompt 片段，注入 AI system prompt

### 8. 认知行为投票（CognitiveVotingService）

- 在抑郁/焦虑等情绪场景下触发认知投票
- 投票类型：thought_distortion（认知扭曲）/ self_efficacy（自我效能）/ coping_strategy（应对策略）
- 基于情绪阈值判断是否触发（depression > 0.3 / anxiety > 0.4）
- 记录用户选择到数据库，供后续分析

### 9. 行为打卡（BehaviorCheckInService）

- 支持四种打卡类型：mood（心情）/ sleep（睡眠）/ exercise（运动）/ social（社交）
- 计算连续打卡天数（streak）
- 统计近 N 天各类型打卡次数
- 计算活跃度评分（activeScore）
- 打卡后自动触发用户画像动态修正

### 10. 心理准备度得分（PsychologicalReadinessService）

- PRS = w1 × engagement + w2 × valence_normalized + w3 × arousal
- 权重：w1=0.4（参与度）/ w2=0.35（情感价态）/ w3=0.25（唤醒度）
- 干预深度映射：
  - PRS < 0.35 → scaffolding（强支架）
  - PRS 0.35-0.65 → supportive（中度支持）
  - PRS > 0.65 → reflective（反思性对话）
- PRS 上下文注入 AI system prompt，实现自适应干预强度

### 11. 向量相似度服务（VectorSimilarityService）

- 基于 BGE embedding 计算文本向量
- 与 crisis_sample 库中的样本进行相似度比对
- 支持批量检测，用于历史消息回溯
- 返回匹配样本ID、相似度得分

### 12. 疗效评估（TherapyEvaluationService + UserSatisfactionService）

- **MentalAlign 评估框架**：每次 AI 回复后自动评估 CSS（认知支持得分）和 ARS（情感共鸣得分），记录到 `therapy_evaluation` 表，支持多模型横向对比
- **HEART 用户体验评估**：Happiness / Engagement / Adoption / Retention / Task Success 五维度满意度评分，记录到 `user_satisfaction` 表
- 平台级统计聚合（平均 CSS、ARS、满意度、总评估次数）
- 支持按 7/14/30 天时间范围筛选

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
│   ├── ResourceRepositoryController.java  # 资源库策略管理（管理员）
│   ├── IntentController.java         # 意图分类与角色调度
│   ├── CognitiveVotingController.java  # 认知投票管理
│   ├── BehaviorCheckInController.java # 行为打卡管理
│   ├── EvaluationController.java      # 模型疗效评估（MentalAlign/HEART）
│   ├── UserController.java            # 注册/登录
│   └── WelcomeController.java         # 欢迎/问候
│
├── entity/
│   ├── AiModelConfig.java            # AI模型配置实体
│   ├── ChatMessage.java              # 消息实体
│   ├── ChatSession.java              # 会话实体
│   ├── CrisisAlert.java              # 危机预警实体
│   ├── CrisisSample.java             # 危机样本实体（向量相似度检测）
│   ├── EmotionRecord.java            # 情绪记录实体
│   ├── IntentClassification.java     # 意图分类实体
│   ├── CognitiveVoting.java          # 认知投票实体
│   ├── UserBehaviorCheckin.java      # 行为打卡实体
│   ├── PsychologicalReadinessScore.java  # 心理准备度得分实体
│   ├── UserInteractionStyle.java    # 用户互动风格实体
│   ├── TherapyEvaluation.java        # AI疗效评估实体（MentalAlign）
│   ├── UserSatisfaction.java         # 用户满意度实体（HEART）
│   ├── ModelEffectivenessStats.java  # 模型效果统计实体
│   ├── GuardianResult.java           # Guardian 检测结果（非持久化 DTO）
│   ├── ReflectorResult.java          # Reflector 审计结果（非持久化 DTO）
│   ├── ResourceRecommendation.java    # 资源推荐记录实体
│   ├── ResourceRepository.java       # 资源库策略片段实体
│   ├── Result.java                   # 统一响应封装（非持久化 DTO）
│   ├── PageResult.java               # 分页响应封装（非持久化 DTO）
│   ├── SupportiveResource.java       # 支持资源实体
│   ├── User.java                     # 用户实体
│   ├── UserModelPreference.java      # 用户模型偏好实体
│   └── UserRequest.java              # 用户请求 DTO（非持久化）
│
├── exception/
│   └── GlobalExceptionHandler.java   # 全局异常处理器
│
├── interceptor/
│   ├── JwtInterceptor.java           # JWT 验签 + 注入 currentUserId
│   └── RoleInterceptor.java         # 角色权限校验
│
├── mapper/
│   ├── AiModelConfigMapper.java        # AI模型配置 Mapper
│   ├── ChatMessageMapper.java          # 消息 Mapper
│   ├── ChatSessionMapper.java          # 会话 Mapper
│   ├── CrisisAlertMapper.java          # 危机预警 Mapper
│   ├── CrisisSampleMapper.java         # 危机样本 Mapper
│   ├── EmotionRecordMapper.java        # 情绪记录 Mapper
│   ├── IntentClassificationMapper.java  # 意图分类 Mapper
│   ├── CognitiveVotingMapper.java      # 认知投票 Mapper
│   ├── UserBehaviorCheckinMapper.java  # 行为打卡 Mapper
│   ├── PsychologicalReadinessScoreMapper.java  # PRS Mapper
│   ├── UserInteractionStyleMapper.java  # 用户互动风格 Mapper
│   ├── ResourceRecommendationMapper.java  # 资源推荐 Mapper
│   ├── ResourceRepositoryMapper.java   # 资源库策略 Mapper
│   ├── TherapyEvaluationMapper.java    # 疗效评估 Mapper
│   ├── UserSatisfactionMapper.java     # 用户满意度 Mapper
│   ├── ModelEffectivenessStatsMapper.java  # 模型效果统计 Mapper
│   ├── SupportiveResourceMapper.java   # 支持资源 Mapper
│   ├── UserMapper.java                # 用户 Mapper
│   └── UserModelPreferenceMapper.java  # 用户模型偏好 Mapper
│
├── service/
│   ├── AiModelConfigService.java         # AI模型配置服务接口
│   ├── AiService.java                    # AI 大模型服务接口
│   ├── ChatMessageService.java           # 消息服务接口
│   ├── ChatSessionService.java           # 会话服务接口
│   ├── CrisisDetectionService.java       # 危机检测服务接口
│   ├── EmotionAnalysisService.java       # 情绪分析服务接口
│   ├── GuardianService.java              # Guardian 安全层接口
│   ├── ReflectorService.java             # Reflector 安全层接口
│   ├── IntentReconstructService.java     # 意图重构服务接口
│   ├── RoleSchedulerService.java        # 角色调度服务接口
│   ├── CognitiveVotingService.java      # 认知投票服务接口
│   ├── BehaviorCheckInService.java       # 行为打卡服务接口
│   ├── PsychologicalReadinessService.java  # PRS心理准备度服务接口
│   ├── VectorSimilarityService.java     # 向量相似度服务接口
│   ├── ResourceService.java              # 资源服务接口
│   ├── ResourceRepositoryService.java    # 资源库策略服务接口
│   ├── UserModelPreferenceService.java   # 用户模型偏好服务接口
│   ├── TherapyEvaluationService.java     # 疗效评估服务接口（MentalAlign/HEART）
│   ├── UserSatisfactionService.java     # 用户满意度服务接口
│   └── UserService.java                 # 用户服务接口

└── service/impl/
    ├── AiModelConfigServiceImpl.java          # AI模型配置服务实现
    ├── AiServiceImpl.java                     # AI 服务（含流式调用 + 多模型路由）
    ├── ChatMessageServiceImpl.java            # 消息服务实现
    ├── ChatSessionServiceImpl.java            # 会话服务实现
    ├── CrisisDetectionServiceImpl.java         # 危机检测实现
    ├── EmotionAnalysisServiceImpl.java          # 情绪分析服务实现
    ├── GuardianServiceImpl.java                # Guardian 安全层实现
    ├── ReflectorServiceImpl.java               # Reflector 安全层实现
    ├── IntentReconstructServiceImpl.java       # 意图重构实现
    ├── RoleSchedulerServiceImpl.java           # 角色调度实现
    ├── CognitiveVotingServiceImpl.java         # 认知投票实现
    ├── BehaviorCheckInServiceImpl.java         # 行为打卡实现
    ├── PsychologicalReadinessServiceImpl.java # PRS实现
    ├── VectorSimilarityServiceImpl.java        # 向量相似度实现
    ├── ResourceServiceImpl.java                # 资源服务实现
    ├── ResourceRepositoryServiceImpl.java       # 资源库策略实现
    ├── UserModelPreferenceServiceImpl.java     # 用户模型偏好实现
    ├── TherapyEvaluationServiceImpl.java      # 疗效评估实现
    ├── UserSatisfactionServiceImpl.java      # 用户满意度实现
    └── UserServiceImpl.java                   # 用户服务实现
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

# AI 大模型（默认 DeepSeek，可通过数据库 ai_model_config 表配置更多模型）
ai.deepseek.url=https://api.deepseek.com/v1/chat/completions
ai.deepseek.key=${AI_DEEPSEEK_API_KEY:sk-your-deepseek-key}
ai.openai.url=https://api.openai.com/v1/chat/completions
ai.openai.key=${AI_OPENAI_API_KEY:sk-your-openai-key}
ai.kimi.url=https://api.moonshot.cn/v1/chat/completions
ai.kimi.key=${AI_KIMI_API_KEY:sk-your-kimi-key}
ai.local.url=http://localhost:8000/v1/chat/completions
ai.local.key=${AI_LOCAL_API_KEY:sk-local}
```
