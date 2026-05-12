# 心理支持对话系统

> 基于大语言模型的心理健康 AI 陪伴系统 —— 毕业设计项目

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.0-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![JDK](https://img.shields.io/badge/JDK-17%2B-orange?style=flat-square&logo=openjdk)](https://adoptium.net/)
[![Vue](https://img.shields.io/badge/Vue-3.5-42b883?style=flat-square&logo=vuedotjs)](https://vuejs.org/)
[![Vite](https://img.shields.io/badge/Vite-8.0-646cff?style=flat-square&logo=vite)](https://vitejs.dev/)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](./LICENSE)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-4479a1?style=flat-square&logo=mysql)](https://www.mysql.com/)

## 项目简介

本系统是一个完整的心理支持对话平台，采用**前后端分离**架构：

- **后端**：Spring Boot 3.3 + MyBatis-Plus + Java 17，处理 API 请求、数据库操作、AI 对话、情绪分析、危机预警
- **前端**：Vue 3 + Element Plus + Pinia + ECharts，提供登录注册、多会话聊天、SSE 流式对话（打字机效果）、AI 模型切换、情绪报告、正念空间、行为打卡、认知投票等功能
- **AI 引擎**：DeepSeek / OpenAI / Kimi / 本地模型，通过前端下拉菜单一键切换，支持无缝替换

系统面向有心理健康支持需求的用户，提供匿名、温暖、安全的 AI 心理陪伴对话体验，同时具备情绪分析、意图分类、认知投票、行为打卡、危机预警、用户画像、支持资源库等专业的心理健康辅助功能。

---

## 核心新增功能

| 功能模块 | 说明 | 技术亮点 |
|----------|------|----------|
| **意图分类与重构** | 基于用户输入和情绪状态，重构临床意图（existential_crisis / value_clarification 等11种），匹配疗法维度（ACT/CBT/DBT）和 AI 角色 | 情绪驱动 + AI 语义理解双重判断 |
| **动态角色调度** | 根据意图类型、情绪得分、PRS 准备度动态选择 AI 治疗角色（supportive / empathetic / socratic / guided / crisis_mode / dynamic_switcher） | 多维度自适应决策 |
| **认知行为投票（CBT）** | 在抑郁/焦虑情绪场景下触发认知投票，帮助用户觉察思维模式 | 情绪阈值触发，循序渐进 |
| **行为打卡系统** | 每日记录心情、睡眠、运动、社交打卡，连续天数激励，支持活跃度评分 | 追踪心理健康行为习惯 |
| **心理准备度得分（PRS）** | 基于参与度、情感价态、唤醒度计算 0~1 的 PRS 得分，映射干预深度（scaffolding / supportive / reflective） | 自适应干预强度 |
| **Guardian 安全层（输入）** | PHQ-9 语义检测 + 向量语义相似度 + 关系脉络门控 + 关键词硬匹配，四层危机检测 | 多层次输入安全过滤 |
| **Reflector 安全层（输出）** | AI 回复合规性审计，检测自我伤害建议、医疗处方、歧视性语言等违规内容 | 输出内容安全保障 |
| **向量相似度服务** | 基于 BGE 向量 embedding 与 crisis_sample 库比对，支持危机语义相似度检测 | 支持本地模型部署 |
| **MentalAlign 模型疗效评估** | CSS（认知支持得分）和 ARS（情感共鸣得分）评估 AI 回复质量，支持多模型横向对比 | AI-as-judge 评估框架 |
| **HEART 用户体验评估** | Happiness/Engagement/Adoption/Retention/Task Success 五维度用户体验评估 | Google UX 评估框架 |

---

## 系统架构

```
┌──────────────────┐       HTTP/SSE        ┌──────────────────┐      API 调用      ┌──────────────────┐
│   Vue 3 前端     │  ←──────────────────→ │  Spring Boot 3.3 │ ←───────────────→  │  DeepSeek API   │
│  Element Plus   │                       │   MyBatis-Plus   │                    │  OpenAI API     │
│  Pinia 状态管理  │                       │  Java 21         │                    │  Kimi API       │
│  ECharts 图表   │                       │  Port: 8080      │                    │  本地模型        │
│  Port: 5173    │                       └────────┬─────────┘                    └──────────────────┘
└──────────────────┘                                │
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
| | ECharts / vue-echarts | — | 情绪报告图表（趋势图 + 饼图） |
| | Font Awesome | 6.4 | 图标库 |
| **后端** | Spring Boot | 3.3.0 | Web 框架 |
| | MyBatis-Plus | 3.5.5 | CRUD 零配置 |
| | JJWT | 0.11.5 | Token 签发与验签 |
| | jBCrypt | 0.4 | 密码单向加密 |
| | Java | 17 | JDK 17 兼容 |
| **数据库** | MySQL | 8.x | utf8mb4 字符集 |
| **AI 引擎** | DeepSeek / OpenAI / Kimi / 本地模型 | — | OpenAI 兼容接口，支持多模型切换 |
| **向量服务** | BGE Embedding（本地/云端） | — | 危机语义相似度检测 |

## 目录结构

```
bysj_code/
├── 1_java_backend/          # Spring Boot 后端服务
│   ├── src/main/java/       # Java 源码
│   │   └── com.example.ai_app_java/
│   │       ├── controller/  # REST 控制器（12个）
│   │       │   ├── ChatController.java        # 聊天与会话管理
│   │       │   ├── UserController.java        # 用户注册/登录
│   │       │   ├── EmotionController.java      # 情绪历史/趋势查询
│   │       │   ├── CrisisController.java       # 危机预警管理（管理员）
│   │       │   ├── ResourceController.java     # 支持资源库
│   │       │   ├── ModelConfigController.java  # AI模型配置（列表/当前/切换）
│   │       │   ├── ResourceRepositoryController.java  # 资源库策略管理
│   │       │   ├── IntentController.java       # 意图分类与角色调度
│   │       │   ├── CognitiveVotingController.java  # 认知投票管理
│   │       │   ├── BehaviorCheckInController.java  # 行为打卡管理
│   │       │   ├── EvaluationController.java   # 模型疗效评估
│   │       │   └── WelcomeController.java      # 欢迎/问候
│   │       ├── service/      # 业务逻辑（19个接口 + 19个实现）
│   │       ├── entity/       # 数据库实体（23个，含非持久化 DTO）
│   │       ├── mapper/       # 数据访问层（20个，继承 BaseMapper）
│   │       ├── interceptor/  # JWT 鉴权 + 角色权限拦截器
│   │       ├── annotation/   # @RequireRole 注解
│   │       ├── config/       # 跨域与拦截器注册
│   │       ├── exception/    # 全局异常处理器
│   │       └── utils/        # JWT 工具类
│   ├── src/main/resources/
│   │   └── application.properties  # 数据库 / JWT / AI 配置
│   ├── init.sql              # 数据库初始化（20张表 + 预置资源数据 + 预置模型配置）
│   └── pom.xml               # Maven 依赖
│
├── 2_vue_frontend/           # Vue 3 前端应用
│   ├── src/
│   │   ├── App.vue           # 根组件（挂载 ModelDialog 弹窗）
│   │   ├── main.js           # 入口文件（挂载后加载当前 AI 模型）
│   │   ├── router/index.js   # 路由 + 全局前置守卫
│   │   ├── stores/
│   │   │   ├── user.js      # Pinia 用户状态（登录/登出）
│   │   │   └── app.js       # Pinia 全局状态（模型切换弹窗、AI 模型）
│   │   ├── api/             # API 请求封装
│   │   │   ├── index.js     # Axios 实例（请求/响应拦截器）
│   │   │   ├── chat.js      # 聊天与会话 API
│   │   │   ├── emotion.js   # 情绪 API
│   │   │   ├── resource.js  # 资源库 API
│   │   │   ├── user.js      # 用户 API
│   │   │   ├── model.js     # AI 模型 API
│   │   │   ├── voting.js    # 认知投票 API
│   │   │   ├── checkin.js   # 行为打卡 API
│   │   │   ├── crisis.js    # 危机预警 API
│   │   │   └── evaluation.js  # 模型疗效评估 API
│   │   ├── components/
│   │   │   ├── ModelDialog.vue    # AI 模型选择弹窗组件
│   │   │   ├── CognitiveVoting.vue  # 认知投票弹窗组件
│   │   │   ├── CheckInCard.vue    # 行为打卡卡片组件
│   │   │   ├── ProfileTag.vue     # 用户画像标签组件
│   │   │   ├── SatisfactionSurvey.vue  # 满意度调查组件
│   │   │   ├── TherapyRating.vue   # 疗效评分组件
│   │   │   ├── EmotionDrawer.vue    # 情绪分析抽屉组件
│   │   │   ├── EmotionChart.vue     # 情绪图表组件
│   │   │   ├── EmotionAnalysisPanel.vue  # 情绪分析面板
│   │   │   ├── ResourceDrawer.vue    # 资源库抽屉组件
│   │   │   ├── CrisisAlert.vue      # 危机预警弹窗组件
│   │   │   ├── ChatMessage.vue      # 聊天消息组件
│   │   │   ├── WelcomeCard.vue      # 欢迎卡片组件
│   │   │   └── TopNav.vue          # 顶部导航组件
│   │   ├── views/
│   │   │   ├── Login.vue    # 登录页
│   │   │   ├── Register.vue # 注册页
│   │   │   ├── Home.vue     # 聊天主页（导航 + 聊天 + 分析面板）
│   │   │   ├── Profile.vue  # 个人主页
│   │   │   ├── Admin.vue    # 管理控制台（危机预警 + 资源库 + 模型评估）
│   │   │   └── EvaluationDashboard.vue  # 模型疗效评估页
│   │   └── styles/
│   │       └── global.css   # 全局样式（CSS 变量、玻璃拟态、动画）
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
| **多轮对话** | 每次对话携带最近 20 条历史上下文，AI 能记住之前的内容 | 从数据库加载历史消息（倒序取→反转→oldest→newest 注入 system prompt） |
| **多模型切换** | 前端标题栏一键切换 DeepSeek/GPT/Kimi/本地模型 | App.vue 全局挂载 ModelDialog，切换后 SSE 请求使用对应 modelCode |
| **多会话管理** | 创建/切换聊天会话，自动总结会话标题 | AI 异步生成标题，无感刷新 |
| **流式对话（SSE）** | AI 回复逐字打字机效果，不阻塞主线程 | EventSource 原生 SSE 解析，前端稳定不丢帧 |
| **情绪分析** | 每条消息同步分析情绪类型 + 得分 + 关键词，结果用于资源匹配 | 同步执行，结果立即用于构建资源上下文 |
| **情绪报告** | 近 7/30/90 天情绪趋势折线图 + 情绪类型分布饼图 | ECharts + vue-echarts，数据来自 emotion_record 表 |
| **意图分类与重构** | 基于用户输入和情绪状态，重构临床意图（11种），匹配疗法维度（ACT/CBT/DBT）和 AI 治疗角色 | 情绪驱动 + AI 语义理解双重判断 |
| **动态角色调度** | 根据意图、情绪得分、PRS 准备度动态选择 AI 治疗角色（supportive/empathetic/socratic/guided/crisis_mode/dynamic_switcher） | 多维度自适应决策 |
| **心理准备度得分（PRS）** | 基于参与度、情感价态、唤醒度计算 PRS 得分（0~1），映射干预深度（scaffolding/supportive/reflective） | 自适应干预强度，个性化对话体验 |
| **认知行为投票（CBT）** | 在抑郁/焦虑等情绪场景下触发认知投票，帮助用户觉察思维模式 | 情绪阈值触发，记录投票历史 |
| **行为打卡系统** | 每日记录心情、睡眠、运动、社交打卡，连续天数激励 + 活跃度评分 | 追踪心理健康行为习惯 |
| **Guardian 安全层（输入）** | PHQ-9 语义检测 + 向量语义相似度 + 关系脉络门控 + 关键词硬匹配，四层危机检测 | 多层次输入安全过滤，语义理解增强 |
| **Reflector 安全层（输出）** | AI 回复合规性审计，检测自我伤害建议、医疗处方、歧视性语言等违规内容 | 输出安全保障，替换安全回复 |
| **危机预警** | 独立关键词引擎 + AI System Prompt 双层保障（原有） + Guardian 安全层（增强） | 三层保障体系 |
| **用户画像** | 统计压力等级、情绪趋势、对话次数、行为打卡活跃度 | 基于最近 10 条情绪记录动态计算 |
| **支持资源库** | 按情绪匹配策略片段注入 AI 回复，自然融入引导性练习建议 | 管理员可增删改资源，AI 在对话中自主推荐 |
| **正念空间** | 前端本地播放呼吸放松、正念冥想、意象放松、身体放松练习 | 从资源库加载，播放器带进度条和计时 |
| **管理控制台** | 危机预警实时处理 + 资源库增删改 + 模型参数配置 + 意图分类管理 | 仅 ADMIN 角色可访问 |

## 数据库表结构

系统共 **20 张数据表**，完整定义见 `1_java_backend/init.sql`：

| 表名 | 说明 | 核心字段 |
|------|------|----------|
| `user` | 匿名用户账号 | username, password(BCrypt), role(USER/ADMIN) |
| `chat_session` | 聊天会话列表 | user_id, title, create_time |
| `chat_message` | 对话消息记录 | session_id, user_id, role, content, create_time |
| `emotion_record` | 情绪分析记录 | emotion_type, emotion_score, valence, arousal, keywords, analysis_time |
| `crisis_alert` | 危机预警记录 | alert_level(high/medium/low), alert_type, status, keywords |
| `crisis_sample` | 危机样本库 | text, vector(BGE), category, priority, enabled |
| `user_profile` | 用户心理画像 | stress_level, emotional_trend, conversation_count, total_messages, personality_type, main_concern |
| `supportive_resource` | 支持性资源内容 | category, sub_category, trigger_emotion, trigger_score_min/max, priority, enabled |
| `resource_recommendation` | 资源推荐日志 | user_id, resource_id, emotion_type, emotion_score, recommended_at |
| `resource_repository` | 资源库策略片段 | category, sub_category, strategy_title, strategy_content, priority, trigger_emotion |
| `ai_model_config` | AI模型配置 | code, name, api_url, model_name, temperature, enabled, is_default |
| `user_model_preference` | 用户模型偏好 | user_id, model_code |
| `intent_classification` | 意图分类定义 | code, name, therapy_dimensions, ai_role, priority |
| `cognitive_voting` | 认知投票记录 | user_id, voting_type, question, selected_option, session_id |
| `user_behavior_checkin` | 行为打卡记录 | user_id, checkin_type(mood/sleep/exercise/social), checkin_value, checkin_date |
| `psychological_readiness_score` | 心理准备度得分 | user_id, total_score, engagement_score, valence_score, arousal_score, intervention_depth |
| `user_interaction_style` | 用户互动风格 | user_id, preferred_style, autonomous_score, guided_score, mixed_score, recent_style |
| `therapy_evaluation` | **AI疗效评估记录** | user_id, session_id, model_code, css_score, ars_score, clinical_intent, therapy_module |
| `user_satisfaction` | **用户满意度记录** | user_id, session_id, model_code, happiness, engagement, adoption, retention, task_success |
| `model_effectiveness_stats` | **模型效果统计** | model_code, stat_type, avg_css, avg_ars, avg_happiness, retention_rate |

## 快速启动

### 环境要求

- JDK 17+
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
2. **危机三层防线**：
   - 第一层：Guardian 安全层（PHQ-9 + 向量语义 + 关系脉络 + 关键词）
   - 第二层：Reflector 安全层（AI 回复合规审计）
   - 第三层：AI System Prompt（内容层面兜底）
3. **完全异步化**：情绪分析、危机检测、标题总结、AI 回复均在不同线程并行执行，互不阻塞主线程
4. **匿名设计**：无需手机号/真实姓名即可使用，降低用户寻求心理帮助的心理门槛
5. **JWT 无状态鉴权**：Token 可带在 HTTP Header 或 URL 参数（适配 SSE GET 请求），灵活安全
6. **多模型支持**：支持 DeepSeek/GPT/Kimi/本地模型一键切换，管理员可配置模型参数，切换立即生效
7. **意图驱动的个性化对话**：11 种临床意图分类 + 5 种 AI 治疗角色 + PRS 心理准备度，自适应选择最优对话策略
8. **CBT 认知行为疗法集成**：通过认知投票帮助用户觉察认知偏差，配合 AI 引导重构思维模式
9. **行为打卡激励系统**：追踪心理健康行为习惯，连续打卡激励，培养积极生活方式
10. **向量语义危机检测**：基于 BGE embedding 与 crisis_sample 库比对，增强危机识别的语义理解能力

## 环境变量参考

推荐通过环境变量注入敏感配置，敏感文件 `secrets.properties` 中已预置占位符：

```bash
# 数据库
SPRING_DATASOURCE_PASSWORD=your_mysql_password

# AI 大模型（按需配置至少一项）
AI_DEEPSEEK_API_KEY=sk-xxxxxxxxxxxxxxxx
AI_OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxx
AI_KIMI_API_KEY=sk-xxxxxxxxxxxxxxxx

# JWT（生产环境务必更换，至少 32 字符）
JWT_SECRET=your_own_secret_key_at_least_32_characters
```

> **提示**：在 IDE 中运行后端时，可通过 Run Configuration 或 `.env` 文件 + Spring Boot 的 `@ConfigurationProperties` 注入上述环境变量。

## 常见问题

### 1. 启动前端报 `npm install` 失败？

确保 Node.js 版本 >= 20，并使用国内镜像加速：
```bash
npm install --registry=https://registry.npmmirror.com
```

### 2. SSE 流式对话无法建立连接？

- 确认后端已运行在 `http://localhost:8080`
- 检查浏览器控制台是否有跨域错误，后端 `WebMvcConfig` 已配置 CORS 允许 `http://localhost:5173`
- 确认用户已登录（未登录时后端返回 401，前端不会建立 SSE 连接）

### 3. 切换 AI 模型后对话没有变化？

模型切换后，下次对话才会生效，当前对话仍使用原模型。确认 `/model/select` 接口返回 code=200，`appStore.currentModel` 已正确更新。

### 4. 危机预警未触发？

Guardian 检测依赖 `crisis_sample` 表中有足够的样本数据，请通过管理控制台确认样本库已初始化。向量相似度检测需要 Embedding 服务可用（本地模型或云端 BGE API）。

### 5. 疗效评估数据为空？

MentalAlign 疗效评估在每次 AI 回复后异步执行，记录到 `therapy_evaluation` 表。确认 AI 对话已产生足够回复数据，且 `EvalExecutorService` 日志无报错。

### 6. 心理准备度得分（PRS）始终偏低？

PRS 由参与度（对话频率）、情感价态（情绪分析 valence）、唤醒度（情绪分析 arousal）三者加权计算。请确认用户有足够的对话互动次数，情绪分析结果已正确写入 `emotion_record` 表。

## 如何贡献

欢迎提交 Issue 和 Pull Request。提交前请注意：

- 代码风格保持与现有项目一致（Java 使用标准命名规范，Vue 使用 `<script setup>` 组合式 API）
- 新增接口需在对应 README 中补充接口说明
- 新增数据库表需在 `init.sql` 中包含完整的 CREATE TABLE 语句（含注释和索引）
- 所有 API Key、密码等敏感信息不得硬编码

## 许可协议

本项目仅作为学术研究及毕业设计用途。系统提供的 AI 对话功能为辅助心理健康支持工具，**不能替代专业心理医生或危机干预热线**。遇到严重心理危机情况，请及时就医或拨打心理援助热线。

项目整体采用 [MIT License](./LICENSE)。

## 参考资料

- [Spring Boot 官方文档](https://spring.io/projects/spring-boot)
- [MyBatis-Plus 官方文档](https://baomidou.com/)
- [Vue 3 官方文档](https://vuejs.org/)
- [Element Plus 组件库](https://element-plus.org/)
- [DeepSeek API 平台](https://platform.deepseek.com/)
- [LLaMA-Factory GitHub](https://github.com/hiyouga/LLaMA-Factory)
