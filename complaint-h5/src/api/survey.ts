import request from '@/utils/request'

export interface SurveyFormData {
  departmentId: number
  visitDate: string
  patientName?: string
  contactPhone?: string
  overallScore: number
  dimensionScores: {
    dimension: string
    score: number
    comment?: string
  }[]
  suggestions?: string
}

export interface SurveySubmitResponse {
  id: number
  surveyNo: string
}

export function submitSurvey(data: SurveyFormData) {
  return request<SurveySubmitResponse>({
    url: '/satisfaction/submit',
    method: 'post',
    data
  })
}

export function getSurveyStatistics(params?: { startDate?: string; endDate?: string }) {
  return request<{
    totalCount: number
    averageScore: number
    satisfactionRate: number
    dimensionScores: {
      dimension: string
      averageScore: number
    }[]
  }>({
    url: '/satisfaction/statistics',
    method: 'get',
    params
  })
}

export function getSurveyDetail(id: number) {
  return request<SurveyFormData & { id: number; createTime: string }>({
    url: `/satisfaction/${id}`,
    method: 'get'
  })
}
