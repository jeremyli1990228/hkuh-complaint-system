import request from '@/utils/request'

export interface SatisfactionStatistics {
  totalResponses: number
  overallAvgScore: number
  overallSatisfactionRate: number
  nps: number
  promoters: number
  passives: number
  detractors: number
  categoryScores: CategoryScore[]
  scoreDistribution: Record<number, number>
  deptRanking: DeptScore[]
  monthlyTrend: MonthlyTrend[]
}

export interface CategoryScore {
  category: string
  categoryName: string
  avgScore: number
  satisfactionRate: number
  responseCount: number
  trend?: TrendItem[]
}

export interface DeptScore {
  deptId: number
  deptName: string
  avgScore: number
  satisfactionRate: number
  responseCount: number
  ranking: number
}

export interface MonthlyTrend {
  month: string
  avgScore: number
  responseCount: number
  satisfactionRate: number
  nps: number
}

export interface TrendItem {
  date: string
  total: number
  processed: number
  closed: number
  avgHandleTime: number
}

export interface SatisfactionQuery {
  startDate?: string
  endDate?: string
  deptId?: number
  period?: 'WEEK' | 'MONTH' | 'QUARTER' | 'YEAR'
}

export function getSatisfactionStatistics(params: SatisfactionQuery) {
  return request<SatisfactionStatistics>({
    url: '/satisfaction/statistics',
    method: 'get',
    params
  })
}

export function getSatisfactionStats(params: any) {
  return request({
    url: '/satisfaction/stats',
    method: 'get',
    params
  })
}

export function getSatisfactionList(params: any) {
  return request({
    url: '/satisfaction/list',
    method: 'get',
    params
  })
}

export function getDeptRanking(params: { startDate: string; endDate: string; top?: number }) {
  return request<DeptScore[]>({
    url: '/satisfaction/dept-ranking',
    method: 'get',
    params
  })
}
