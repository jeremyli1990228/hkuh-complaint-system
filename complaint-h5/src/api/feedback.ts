import request from '@/utils/request'

export interface FeedbackItem {
  id: number
  feedbackNo: string
  title: string
  content: string
  feedbackTypeId: number
  feedbackTypeName: string
  source: string
  priority: string
  status: string
  createTime: string
  updateTime?: string
  rating?: number
  lastRecord?: string
}

export interface FeedbackDetail extends FeedbackItem {
  complainant?: {
    name: string
    phone: string
    email: string
  }
  handleRecords?: HandleRecord[]
  attachments?: Attachment[]
}

export interface HandleRecord {
  id: number
  feedbackId: number
  handlerName: string
  action: string
  content: string
  result: string
  createTime: string
}

export interface Attachment {
  id: number
  fileName: string
  filePath: string
  fileSize: number
  fileType: string
}

export interface SubmitFeedbackDTO {
  feedbackTypeId: number
  title: string
  content: string
  source?: string
  priority?: string
  isAnonymous: boolean
  contactPhone?: string
  contactEmail?: string
  attachmentIds?: number[]
  complainant?: {
    phone?: string
    email?: string
  }
}

export function submitFeedback(data: SubmitFeedbackDTO) {
  return request<{ feedbackNo: string; id: number }>({
    url: '/feedback/create',
    method: 'post',
    data
  })
}

export function getMyFeedbackList(params: { page?: number; pageSize?: number }) {
  return request<{ list: FeedbackItem[]; total: number }>({
    url: '/feedback/my-list',
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

export function getFAQList() {
  return request<{ list: FAQItem[] }>({
    url: '/feedback/faq',
    method: 'get'
  })
}

export interface FAQItem {
  id: number
  question: string
  answer: string
  category: string
  viewCount?: number
  images?: string[]
}

export function uploadFile(formData: FormData) {
  return request<{ id: number; url: string }>({
    url: '/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

export function rateFeedback(id: number, data: { rating: number; comment?: string }) {
  return request({
    url: `/feedback/${id}/rate`,
    method: 'post',
    data
  })
}
