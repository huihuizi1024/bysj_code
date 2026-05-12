import request from './index'

// 获取会话列表
export function getSessionList() {
  return request.get('/chat/session/list')
}

// 创建会话
export function createSession(title = '新的心理探索') {
  return request.post('/chat/session/create', { title })
}

// 获取历史消息
export function getChatHistory(sessionId) {
  return request.get(`/chat/history?sessionId=${sessionId}`)
}

// 注意：发送消息统一使用 SSE 流式接口 streamChat（GET /chat/stream）
// 旧的同步 POST /chat/send 接口已废弃
