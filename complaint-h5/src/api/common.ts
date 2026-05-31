import request from '@/utils/request'

export interface FeedbackType {
  id: number
  name: string
  typeCode: string
}

export interface Dept {
  id: number
  name: string
  children?: Dept[]
}

export function getFeedbackTypes() {
  return request<FeedbackType[]>({
    url: '/system/dict/feedback-types',
    method: 'get'
  })
}

export function getDepts() {
  return request<Dept[]>({
    url: '/system/dept/list',
    method: 'get'
  })
}

export function getCaptcha() {
  return request<{ captchaKey: string; captchaImage: string }>({
    url: '/auth/captcha',
    method: 'post'
  })
}

export function getUserInfo() {
  return request({
    url: '/auth/info',
    method: 'get'
  })
}
