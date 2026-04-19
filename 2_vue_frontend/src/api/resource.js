import request from './index'

// 根据情绪智能推荐资源
export function getResourceRecommend(emotionType, emotionScore) {
  return request.get(`/resource/recommend?emotionType=${emotionType}&emotionScore=${emotionScore}`)
}

// 获取所有资源（用户浏览）
export function getAllResources(category) {
  const params = category ? `?category=${category}` : ''
  return request.get(`/resource/all${params}`)
}

// 获取资源详情
export function getResourceDetail(id) {
  return request.get(`/resource/detail/${id}`)
}

// 查看推荐记录
export function getMyRecommendations() {
  return request.get('/resource/my/recommendations')
}

// ===== 管理员接口 =====

// 分页查询资源列表
export function getAdminResourceList(params) {
  return request.get('/resource/admin/list', { params })
}

// 新增资源
export function addResource(data) {
  return request.post('/resource/admin/add', data)
}

// 更新资源
export function updateResource(id, data) {
  return request.put(`/resource/admin/update/${id}`, data)
}

// 删除资源
export function deleteResource(id) {
  return request.delete(`/resource/admin/delete/${id}`)
}

// 启用/禁用资源
export function toggleResource(id, enabled) {
  return request.put(`/resource/admin/toggle/${id}`, { enabled })
}

// 查看所有推荐记录（管理员）
export function getAdminRecommendations(params) {
  return request.get('/resource/admin/recommendations', { params })
}
