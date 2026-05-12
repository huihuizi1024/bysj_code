import service from './index'

/**
 * 行为打卡 API
 */
export const submitCheckin = (data) => service.post('/checkin', data)

export const getStreak = () => service.get('/checkin/streak')

export const getCheckinStats = (days = 7) => service.get(`/checkin/stats?days=${days}`)

export const getRecentCheckins = (days = 7) => service.get(`/checkin/recent?days=${days}`)
