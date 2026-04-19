import axios from 'axios'
import { ElMessage } from 'element-plus'

// 🆕 把 baseURL 提取为常量并导出，方便 SSE 等非 axios 场景引用
export const BASE_URL = 'http://localhost:8080'

const service = axios.create({
    baseURL: BASE_URL,  // 🆕 引用常量
    timeout: 10000
})

// 请求拦截器
service.interceptors.request.use(
    config => {
        const token = localStorage.getItem('token')
        if (token) {
            config.headers['Authorization'] = 'Bearer ' + token
        }
        return config
    },
    error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        const res = response.data
        if (res.code !== 200) {
            ElMessage.error(res.msg || '系统错误')
            return Promise.reject(new Error(res.msg || 'Error'))
        }
        return res
    },
    error => {
        ElMessage.error('连接后端失败，请检查后端是否启动')
        return Promise.reject(error)
    }
)

export default service
