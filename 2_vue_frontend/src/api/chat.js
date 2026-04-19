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

// 发送消息（同步版本，返回完整响应）
export function sendMessage(data) {
  return request.post('/chat/send', data)
}
