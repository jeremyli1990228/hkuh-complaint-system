import request from '@/utils/request'

// 获取菜单列表
export function getMenuList() {
  return request({
    url: '/system/menu',
    method: 'get'
  })
}

// 获取用户列表
export function getUserList(params: any) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params
  })
}
