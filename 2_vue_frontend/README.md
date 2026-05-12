# 心理支持对话系统 - 前端界面

> 基于 Vue 3 + Element Plus + Pinia + ECharts 的心理支持对话系统前端。提供用户登录注册、多会话聊天、SSE 流式对话（打字机效果）、AI 模型切换、情绪报告图表、正念空间练习、管理控制台、疗效评估等完整功能界面。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 前端框架（Composition API + `<script setup>` 语法糖） |
| Vite | 8.0 | 构建工具 |
| Element Plus | 2.13 | UI 组件库 |
| Vue Router | 5.0 | 路由管理（含全局前置守卫） |
| Pinia | 3.0 | 状态管理（用户状态 + 全局应用状态） |
| Axios | 1.14 | HTTP 请求（含 Token 自动拦截） |
| ECharts / vue-echarts | — | 情绪报告图表（折线趋势图 + 饼图分布） |
| Font Awesome | 6.4 | 图标库 |

## 快速开始

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务器（热更新）
npm run build      # 构建生产版本（产物在 dist/）
npm run preview    # 预览生产构建
```

确保后端已在 `http://localhost:8080` 运行。

## 页面说明

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 用户登录（默认首页） |
| 注册页 | `/register` | 用户注册 |
| 聊天主页 | `/home` | 主应用页面（需登录，自动跳转） |
| 个人中心 | `/profile` | 用户画像编辑（需登录） |
| 管理控制台 | `/admin` | 危机预警处理 + 资源库管理（仅管理员） |

## 目录结构

```
src/
├── App.vue                  # 根组件（挂载全局 ModelDialog 弹窗组件）
├── main.js                  # 入口文件（应用挂载后自动加载当前 AI 模型）
├── router/index.js          # 路由配置（含全局前置守卫 + 管理员角色校验）
├── stores/
│   ├── user.js              # Pinia 用户状态（登录状态、用户信息、Token）
│   └── app.js               # Pinia 全局应用状态（AI 模型弹窗、当前选中模型）
├── api/
│   ├── index.js             # Axios 实例（Base URL 代理 + 请求/响应拦截器 + Token 自动注入）
│   ├── chat.js              # 聊天与会话 API（创建会话、加载历史、SSE）
│   ├── emotion.js           # 情绪 API（趋势、分布）
│   ├── resource.js          # 资源库 API（推荐、详情、分类）
│   ├── user.js              # 用户 API（登录、注册、登出）
│   ├── model.js             # AI 模型 API（列表、当前、切换）
│   ├── voting.js            # 认知投票 API（提交、获取问题、检查触发）
│   └── checkin.js           # 行为打卡 API（打卡、获取连续天数、统计）
├── components/
│   ├── ModelDialog.vue      # AI 模型选择弹窗（显示所有可用模型，点击切换）
│   ├── CognitiveVoting.vue  # 认知投票弹窗（情绪触发、投票问题展示、提交）
│   ├── CheckInCard.vue     # 行为打卡卡片（心情/睡眠/运动/社交打卡、连续天数）
│   └── ProfileTag.vue      # 用户画像标签组件
├── views/
│   ├── Login.vue            # 登录页
│   ├── Register.vue         # 注册页
│   ├── Home.vue             # 主应用（左侧导航 + 聊天/报告/正念/档案 + 右侧分析面板）
│   ├── Profile.vue          # 个人中心（用户画像编辑）
│   └── Admin.vue           # 管理控制台（危机预警 + 资源库管理 + 模型配置）
└── styles/
    └── global.css           # 全局样式（CSS 变量、玻璃拟态、动画、ECharts 主题）
```

## 核心功能

- **登录鉴权**：Token 自动携带与存储，持久化在 localStorage
- **路由守卫**：未登录自动跳转登录页，已登录访问登录/注册页自动跳转首页；管理员访问 `/admin` 时自动校验 ADMIN 角色
- **SSE 流式对话**：使用原生 `EventSource` API 接收 SSE 流，`message` 事件自动解析 SSE 行协议，前端实时渲染 AI 回复（打字机效果），连接稳定不丢帧；`modelCode` 从 `appStore.currentModel.code` 动态读取，支持多模型切换
- **多轮对话**：每次发送消息时前端从 DB 加载历史记录，后端在 system prompt 中注入最近 20 条历史消息（oldest→newest 顺序），AI 能记住之前对话内容
- **会话管理**：创建/切换/自动总结标题（无感刷新机制）
- **AI 模型切换**：聊天标题栏右侧有切换按钮，点击弹出 `ModelDialog`，展示所有可用模型，当前模型高亮；切换后 SSE 请求立即使用新模型
- **情绪报告**：近 7/30/90 天情绪趋势折线图 + 情绪类型分布饼图，使用 ECharts 渲染
- **正念空间**：前端本地播放呼吸放松、正念冥想、意象放松、身体放松练习，从资源库动态加载，播放器带进度条和计时
- **认知行为投票**：在抑郁/焦虑等情绪场景下自动触发 `CognitiveVoting` 弹窗，展示认知投票问题，帮助用户觉察思维模式
- **行为打卡**：`CheckInCard` 组件支持心情/睡眠/运动/社交四种打卡，显示连续打卡天数和活跃度评分
- **危机预警消息**：特殊红色背景高亮样式
- **状态管理**：Pinia 分别管理用户状态（`user.js`）和全局应用状态（`app.js`，含当前 AI 模型）

## AI 模型切换说明

前端通过 `appStore.currentModel` 管理当前选中的 AI 模型，切换流程如下：

1. 用户点击聊天标题栏右侧模型切换按钮
2. 弹出 `ModelDialog` 弹窗，显示所有可用模型（`/model/list`）
3. 用户选择模型 → 调用 `/model/select` 保存到后端 → 更新 `appStore.currentModel`
4. 后续 SSE 请求的 URL 参数 `modelCode` 使用 `appStore.currentModel.code`

```javascript
// App.vue 根组件挂载后自动加载当前模型
appStore.loadCurrentModel()

// Home.vue 发送消息时使用当前选中的模型
const modelCode = appStore.currentModel.code || 'deepseek'
const url = `${BASE_URL}/chat/stream?sessionId=${id}&content=${...}&modelCode=${modelCode}&token=${token}`
```

## SSE 流式对话实现

前端使用原生 `EventSource` API 接收 SSE 流（相较于手动 `fetch + ReadableStream` 更稳定，不会出现 buffer 错乱和丢帧）：

```javascript
// token 通过 URL 参数传递（SSE 请求无法自定义 Header）
const url = `${BASE_URL}/chat/stream?sessionId=${id}&content=${encodeURIComponent(text)}&token=${token}`

const eventSource = new EventSource(url)
let fullResponse = ''

eventSource.addEventListener('message', (e) => {
  if (e.data === '[DONE]') {
    eventSource.close()
    return
  }
  fullResponse += e.data
  // 实时更新 AI 消息内容
  const aiMsg = messages.value.find(m => m.id === aiMsgId)
  if (aiMsg) aiMsg.content = fullResponse
})

eventSource.addEventListener('error', () => {
  eventSource.close()
})
```

## 认知投票组件（CognitiveVoting.vue）

`CognitiveVoting` 组件在抑郁/焦虑等情绪场景下自动弹出，展示认知投票问题：

```javascript
// 引入组件
import CognitiveVoting from '@/components/CognitiveVoting.vue'

// Props
// - visible: Boolean，控制弹窗显示
// - emotionType: String，当前情绪类型
// - recentVotingType: String，最近的投票类型（避免重复）

// 事件
// - close: 关闭弹窗
// - submitted: 提交成功后触发，返回投票类型和选项
```

**样式特点**：渐变色头部（A6B2A4 色调）、卡片式弹窗、选项按钮带字母标识（A/B/C/D）、选中状态高亮、提交按钮禁用状态控制。

## 行为打卡组件（CheckInCard.vue）

`CheckInCard` 组件提供心理健康行为打卡功能：

```javascript
// 引入组件
import CheckInCard from '@/components/CheckInCard.vue'

// API 调用
import { submitCheckin, getStreak, getCheckinStats } from '@/api/checkin'

// 打卡类型
// - mood: 心情（开心/一般/低落/焦虑/愤怒）
// - sleep: 睡眠（充足/不足/失眠）
// - exercise: 运动（高强度/轻度/无）
// - social: 社交（有社交/独处/线上社交）
```

**功能特点**：Tab 切换打卡类型、打卡选项带 Emoji 标识、连续打卡天数火焰徽章、备注输入框、打卡成功 Toast 提示。

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | 后端 API 地址 | `http://localhost:8080` |

在 `vite.config.js` 中配置代理解决跨域问题。
