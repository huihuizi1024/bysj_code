import request from './index'

// 获取会话情绪历史
export function getSessionEmotions(sessionId) {
  return request.get(`/emotion/session/${sessionId}`)
}

// 获取情绪趋势
export function getEmotionTrend(days = 7) {
  return request.get(`/emotion/trend?days=${days}`)
}

// 获取最新情绪状态
export function getLatestEmotion(sessionId) {
  return request.get(`/emotion/latest/${sessionId}`)
}
