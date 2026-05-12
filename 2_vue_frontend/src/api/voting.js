import service from './index'

/**
 * 认知投票 API
 */
export const submitVote = (data) => service.post('/voting', data)

export const getVotingHistory = (limit = 20) => service.get(`/voting/history?limit=${limit}`)

export const getNextQuestion = (emotionType, recentType = '') =>
  service.get(`/voting/next?emotionType=${emotionType}&recentType=${recentType}`)

export const checkShouldTrigger = (emotionType, emotionScore) =>
  service.get(`/voting/shouldTrigger?emotionType=${emotionType}&emotionScore=${emotionScore}`)

export const getVotingBySession = (sessionId) =>
  service.get(`/voting/session/${sessionId}`)
