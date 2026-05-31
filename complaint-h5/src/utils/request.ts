import axios, { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'

const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

// 请求拦截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 可以在这里添加token
    return config
  },
  (error) => {
    return Promise.reject(error)
  },
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    return res
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  },
)

export default service
