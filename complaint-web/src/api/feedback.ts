import request from '@/utils/request'

export interface FeedbackListItem {
  id: number
  feedbackNo: string
  title: string
  content?: string
  feedbackTypeId: number
  feedbackTypeName: string
  source: string
  priority: string
  status: string
  deptId?: number
  deptName?: string
  handlerId?: number
  handlerName?: string
  complainantId?: number
  complainantName?: string
  slaDeadline?: string
  slaWarningTime?: string
  isAnonymous?: number
  contactPhone?: string
  contactEmail?: string
  rating?: number
  ratingComment?: string
  createTime: string
  createBy?: string
  updateTime?: string
  remark?: string
}

export interface FeedbackQuery {
  pageNum?: number
  pageSize?: number
  feedbackNo?: string
  title?: string
  feedbackTypeId?: number
  source?: string
  priority?: string
  status?: string
  deptId?: number
  handlerId?: number
  startDate?: string
  endDate?: string
  keyword?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export interface FeedbackDetail {
  id: number
  feedbackNo: string
  title: string
  content: string
  feedbackTypeId: number
  feedbackTypeName: string
  source: string
  priority: string
  status: string
  deptId?: number
  deptName?: string
  handlerId?: number
  handlerName?: string
  slaDeadline?: string
  slaWarningTime?: string
  isAnonymous?: number
  contactPhone?: string
  contactEmail?: string
  rating?: number
  ratingComment?: string
  createTime: string
  createBy?: string
  updateTime?: string
  remark?: string
  complainant?: {
    name?: string
    phone?: string
    email?: string
    gender?: string
    age?: number
    address?: string
    patientId?: string
  }
  handleRecords?: HandleRecord[]
  attachments?: Attachment[]
}

export interface HandleRecord {
  id: number
  feedbackId: number
  handlerId: number
  handlerName: string
  action: string
  content: string
  result?: string
  isVisible?: number
  createTime: string
  attachments?: Attachment[]
}

export interface Attachment {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
  createTime: string
}

export interface FeedbackType {
  id: number
  typeName: string
  typeCode: string
  slaDays: number
  slaHours?: number
  description?: string
  status: string
}

export interface Dept {
  id: number
  deptName: string
  parentId?: number
}

export interface FeedbackHandleDTO {
  id: number
  action: string
  content?: string
  result?: string
  transferTo?: number
  transferDept?: number
  attachmentIds?: number[]
  isVisible?: boolean
}

export function getFeedbackList(params: FeedbackQuery) {
  return request<PageResult<FeedbackListItem>>({
    url: '/feedback/list',
    method: 'get',
    params
  })
}

export function getFeedbackDetail(id: number) {
  return request<FeedbackDetail>({
    url: `/feedback/${id}`,
    method: 'get'
  })
}

export function createFeedback(data: any) {
  return request<FeedbackListItem>({
    url: '/feedback/create',
    method: 'post',
    data
  })
}

export function handleFeedback(id: number, data: FeedbackHandleDTO) {
  return request({
    url: `/feedback/handle/${id}`,
    method: 'post',
    data
  })
}

export function assignFeedback(id: number, data: any) {
  return request({
    url: `/feedback/assign/${id}`,
    method: 'post',
    data
  })
}

export function rateFeedback(id: number, data: any) {
  return request({
    url: `/feedback/${id}/rate`,
    method: 'post',
    data
  })
}

export function exportFeedback(params: FeedbackQuery & { exportType: string }) {
  return request({
    url: '/feedback/export',
    method: 'post',
    params,
    responseType: 'blob'
  })
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

export function getStatistics(params: { startDate: string; endDate: string; deptId?: number }) {
  return request({
    url: '/feedback/statistics',
    method: 'get',
    params
  })
}
