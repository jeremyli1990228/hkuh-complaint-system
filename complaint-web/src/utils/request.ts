import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { getToken } from './auth'
import { ElMessage } from 'element-plus'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    return res
  },
  (error) => {
    ElMessage.error(error.message || '请求失败')
    return Promise.reject(error)
  }
)

export default service
