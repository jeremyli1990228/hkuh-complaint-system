import request from '@/utils/request'

// 提交满意度调查
export function submitSurvey(data: any) {
  return request({
    url: '/satisfaction/submit',
    method: 'post',
    data,
  })
}

// 获取满意度调查
export function getSurvey(id: number) {
  return request({
    url: `/satisfaction/${id}`,
    method: 'get',
  })
}
