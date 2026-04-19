# 心理支持对话系统 - 前端界面

基于 Vue 3 + Element Plus + Pinia 的心理支持对话系统前端。提供用户登录注册、多会话聊天、SSE 流式对话（打字机效果）、路由守卫、状态管理等完整功能界面。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5 | 前端框架（Composition API + `<script setup>` 语法糖） |
| Vite | 8.0 | 构建工具 |
| Element Plus | 2.13 | UI 组件库 |
| Vue Router | 5.0 | 路由管理（含全局前置守卫） |
| Pinia | 3.0 | 状态管理（统一管理登录状态和用户信息） |
| Axios | 1.14 | HTTP 请求（含 Token 自动拦截） |

## 快速开始

```bash
npm install        # 安装依赖
npm run dev        # 启动开发服务器（热更新）
npm run build      # 构建生产版本
```

确保后端已在 `http://localhost:8080` 运行。

## 页面说明

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 用户登录（默认首页） |
| 注册页 | `/register` | 用户注册 |
| 聊天主页 | `/chat` | 多会话聊天界面（需登录，自动跳转） |

## 目录结构

```
src/
├── App.vue                  # 根组件
├── main.js                  # 入口文件
├── router/index.js          # 路由配置（含全局前置守卫）
├── stores/
│   └── user.js              # 用户状态管理（登录状态、用户信息、Token）
├── utils/
│   └── request.js           # Axios 封装（含请求/响应拦截器）
└── views/
    ├── Login.vue            # 登录页
    ├── Register.vue         # 注册页
    └── Home.vue             # 聊天主页（会话列表 + 聊天窗口 + SSE流式对话）
```

## 核心功能

- **登录鉴权**：Token 自动携带与存储，持久化在 localStorage
- **路由守卫**：未登录自动跳转登录页，已登录访问登录/注册页自动跳转聊天页
- **SSE 流式对话**：使用原生 `fetch` + `ReadableStream` 实现逐字显示 AI 回复（打字机效果）
- **会话管理**：创建/切换/自动总结标题（无感刷新机制）
- **情绪标签显示**：危机预警消息有特殊样式（红色背景高亮）
- **状态管理**：使用 Pinia 统一管理用户登录状态，刷新不丢失

## 路由守卫说明

前端通过 Vue Router 的 `beforeEach` 全局前置守卫实现页面访问控制：

- 访问 `/chat` 时若无 Token → 重定向到 `/login`
- 已登录用户访问 `/login` 或 `/register` → 重定向到 `/chat`

## SSE 流式对话实现

前端使用原生 `fetch` API 配合 `ReadableStream` 读取 SSE 流：

```javascript
const response = await fetch(`${BASE_URL}/chat/stream?sessionId=${id}&content=${encodeURIComponent(text)}`, {
  headers: { 'Authorization': `Bearer ${token}` }
})
const reader = response.body.getReader()
const decoder = new TextDecoder('utf-8')
while (true) {
  const { done, value } = await reader.read()
  if (done) break
  // 逐帧追加内容到消息中
}
```

## 环境变量

| 变量 | 说明 | 默认值 |
|------|------|--------|
| `VITE_API_BASE_URL` | 后端 API 地址 | `http://localhost:8080` |

在 `vite.config.js` 中配置代理解决跨域问题。
