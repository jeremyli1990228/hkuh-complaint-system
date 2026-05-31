import request from '@/utils/request'

export interface LoginData {
  username: string
  password: string
  captchaKey: string
  captchaCode: string
  rememberMe?: boolean
}

export interface CaptchaVO {
  captchaKey: string
  captchaImage: string
}

export interface LoginVO {
  accessToken: string
  refreshToken: string
  expiresIn: number
  userInfo: UserInfo
}

export interface UserInfo {
  userId: number
  username: string
  realName: string
  deptName: string
  roles: string[]
  permissions: string[]
  avatar?: string
  phone?: string
  email?: string
}

export function getCaptcha() {
  return request<CaptchaVO>({
    url: '/auth/captcha',
    method: 'post'
  })
}

export function login(data: LoginData) {
  return request<LoginVO>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

export function getUserInfo() {
  return request<UserInfo>({
    url: '/auth/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}

export function refreshToken(refreshToken: string) {
  return request({
    url: '/auth/refresh-token',
    method: 'post',
    data: { refreshToken }
  })
}

export function changePassword(oldPassword: string, newPassword: string) {
  return request({
    url: '/auth/change-password',
    method: 'post',
    data: { oldPassword, newPassword }
  })
}
