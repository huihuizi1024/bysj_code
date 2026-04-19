import request from './index'

// 获取待处理预警（管理员）
export function getPendingAlerts() {
  return request.get('/crisis/pending')
}

// 获取所有预警（管理员，支持状态筛选）
export function getAllAlerts(status) {
  return request.get('/crisis/all', { params: { status } })
}

// 处理危机预警（管理员）
export function handleCrisisAlert(alertId, handlerNotes) {
  return request.post(`/crisis/handle/${alertId}`, { handlerNotes })
}

// 获取我的危机预警记录
export function getMyAlerts() {
  return request.get('/crisis/user/alerts')
}
