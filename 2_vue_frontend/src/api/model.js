import request from './index'

// 获取可选模型列表
export function getModelList() {
  return request.get('/model/list')
}

// 获取当前模型
export function getCurrentModel() {
  return request.get('/model/current')
}

// 切换模型
export function selectModel(modelCode) {
  return request.post('/model/select', { modelCode })
}
