import service from './index'

/**
 * MentalAlign + HEART 评估 API
 *
 * 提供心理支持系统的疗效评估和用户满意度接口
 */

/**
 * 获取指定模型的 MentalAlign 疗效统计数据
 * @param {Object} params - 查询参数
 * @param {string} [params.modelCode] - 模型代码（可选，不传则返回所有模型）
 * @param {number} [params.days=7] - 时间范围（天数）
 */
export const getTherapyStats = (params) => {
  return service.get('/evaluation/therapy/stats', { params })
}

/**
 * 获取所有模型的疗效对比
 * @param {number} days - 时间范围（天数）
 */
export const getTherapyComparison = (days = 7) => {
  return service.get('/evaluation/therapy/compare', { params: { days } })
}

/**
 * 获取模型的疗效评估趋势
 * @param {string} modelCode - 模型代码
 * @param {number} days - 时间范围（天数）
 */
export const getTherapyTrend = (modelCode, days = 30) => {
  return service.get('/evaluation/therapy/trend', { params: { modelCode, days } })
}

/**
 * 获取用户的疗效评估历史
 * @param {number} limit - 返回记录数限制
 */
export const getTherapyHistory = (limit = 20) => {
  return service.get('/evaluation/therapy/history', { params: { limit } })
}

/**
 * 提交用户对 AI 回复的主观评分
 * @param {Object} data - 评分数据
 * @param {number} data.sessionId - 会话ID
 * @param {number} data.messageId - 消息ID
 * @param {number} data.rating - 总体满意度评分（0-5）
 * @param {number} data.userCss - 用户认知支持评分（0-5）
 * @param {number} data.userArs - 用户情感共鸣评分（0-5）
 */
export const submitUserRating = (data) => {
  return service.post('/evaluation/therapy/rating', data)
}

// ========== HEART 用户满意度接口 ==========

/**
 * 提交用户满意度评价（完整版）
 * @param {Object} data - 满意度数据
 * @param {number} data.sessionId - 会话ID
 * @param {string} data.modelCode - 模型代码
 * @param {number} data.happiness - 满意度（0-5）
 * @param {number} data.engagement - 参与度（0-1）
 * @param {number} data.adoption - 接受度（0-1）
 * @param {number} data.retention - 留存意愿（0-1）
 * @param {number} data.taskSuccess - 任务成功度（0-1）
 * @param {string} [data.comment] - 用户反馈
 * @param {string} [data.improvementSuggestion] - 改进建议
 */
export const submitSatisfaction = (data) => {
  return service.post('/evaluation/satisfaction', data)
}

/**
 * 提交快速满意度评价（简化版）
 * @param {Object} data - 快速评价数据
 * @param {number} data.sessionId - 会话ID
 * @param {string} data.modelCode - 模型代码
 * @param {number} data.overallScore - 综合评分（0-5）
 */
export const submitQuickSatisfaction = (data) => {
  return service.post('/evaluation/satisfaction/quick', data)
}

/**
 * 获取用户满意度历史
 * @param {number} limit - 返回记录数限制
 */
export const getSatisfactionHistory = (limit = 20) => {
  return service.get('/evaluation/satisfaction/history', { params: { limit } })
}

/**
 * 获取用户维度统计
 */
export const getUserStats = () => {
  return service.get('/evaluation/satisfaction/user-stats')
}

/**
 * 获取指定模型的 HEART 指标
 * @param {string} modelCode - 模型代码
 * @param {number} days - 时间范围（天数）
 */
export const getModelHeartStats = (modelCode, days = 7) => {
  return service.get('/evaluation/satisfaction/model', { params: { modelCode, days } })
}

/**
 * 获取所有模型的 HEART 对比
 * @param {number} days - 时间范围（天数）
 */
export const getHeartComparison = (days = 7) => {
  return service.get('/evaluation/satisfaction/compare', { params: { days } })
}

/**
 * 获取用户满意度趋势
 * @param {string} [modelCode] - 模型代码（可选）
 * @param {number} days - 时间范围（天数）
 */
export const getSatisfactionTrend = (modelCode = '', days = 30) => {
  return service.get('/evaluation/satisfaction/trend', { params: { modelCode, days } })
}

/**
 * 获取平台整体 HEART 指标
 * @param {number} days - 时间范围（天数）
 */
export const getPlatformStats = (days = 7) => {
  return service.get('/evaluation/satisfaction/platform', { params: { days } })
}

/**
 * 检查用户是否已对某会话提交过满意度
 * @param {number} sessionId - 会话ID
 */
export const checkSatisfactionSubmitted = (sessionId) => {
  return service.get('/evaluation/satisfaction/check', { params: { sessionId } })
}

// ========== 综合评估接口 ==========

/**
 * 获取综合评估报告
 * @param {number} days - 时间范围（天数）
 */
export const getComprehensiveReport = (days = 7) => {
  return service.get('/evaluation/report', { params: { days } })
}

// ========== CI/CD 评测流水线接口 ==========

/**
 * 触发异步评测（使用 eval_dataset 全量数据）
 * @param {string} modelCode - 模型代码
 */
export const startEval = (modelCode) => {
  return service.post('/eval/start', null, { params: { modelCode } })
}

/**
 * 查询评测批次状态（含进度）
 * @param {number} runId - 批次ID
 */
export const getEvalStatus = (runId) => {
  return service.get(`/eval/status/${runId}`)
}

/**
 * 获取评测历史列表
 */
export const getEvalHistory = () => {
  return service.get('/eval/history')
}

/**
 * 获取模型横向对比（每模型取最新已完成批次）
 */
export const getEvalComparison = () => {
  return service.get('/eval/compare')
}

/**
 * 获取所有已配置的模型（用于评测下拉）
 */
export const getEvalModels = () => {
  return service.get('/eval/models')
}

/**
 * 取消正在运行的评测任务
 * @param {number} runId - 批次ID
 */
export const cancelEval = (runId) => {
  return service.post(`/eval/cancel/${runId}`)
}
