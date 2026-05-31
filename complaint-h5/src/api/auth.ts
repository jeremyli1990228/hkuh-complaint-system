import request from '@/utils/request'

// 登录
export function login(data: any) {
  return request({
    url: '/auth/login',
    method: 'post',
    data,
  })
}

// 获取用户信息
export function getUserInfo() {
  return request({
    url: '/auth/userinfo',
    method: 'get',
  })
}

// 微信授权登录
export function wechatLogin(code: string) {
  return request({
    url: '/auth/wechat',
    method: 'post',
    data: { code },
  })
}
