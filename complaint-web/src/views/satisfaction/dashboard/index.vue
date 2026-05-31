<template>
  <div class="satisfaction-dashboard">
    <el-card class="filter-card">
      <el-form :inline="true" :model="queryForm" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
        </el-form-item>
        <el-form-item label="科室">
          <el-select v-model="queryForm.deptId" placeholder="全部科室" clearable style="width: 150px">
            <el-option v-for="dept in deptList" :key="dept.id" :label="dept.deptName" :value="dept.id" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
            刷新
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-row :gutter="16" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-label">总体满意度评分</div>
            <div class="stat-value-row">
              <span class="stat-value primary">{{ stats.overallAvgScore.toFixed(1) }}</span>
              <span class="stat-change" :class="getChangeClass(stats.avgScoreChange)">
                <el-icon v-if="stats.avgScoreChange > 0"><Top /></el-icon>
                <el-icon v-else-if="stats.avgScoreChange < 0"><Bottom /></el-icon>
                {{ stats.avgScoreChange > 0 ? '+' : '' }}{{ stats.avgScoreChange.toFixed(1) }}
              </span>
            </div>
            <el-progress
              :percentage="(stats.overallAvgScore / 10) * 100"
              :stroke-width="8"
              :color="progressColor"
              :show-text="false"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-label">满意率</div>
            <div class="stat-value-row">
              <span class="stat-value success">{{ (stats.overallSatisfactionRate * 100).toFixed(1) }}%</span>
              <span class="stat-change" :class="getChangeClass(stats.satisfactionRateChange)">
                <el-icon v-if="stats.satisfactionRateChange > 0"><Top /></el-icon>
                <el-icon v-else-if="stats.satisfactionRateChange < 0"><Bottom /></el-icon>
                {{ stats.satisfactionRateChange > 0 ? '+' : '' }}{{ (stats.satisfactionRateChange * 100).toFixed(1) }}%
              </span>
            </div>
            <el-progress
              :percentage="stats.overallSatisfactionRate * 100"
              :stroke-width="8"
              :color="successColor"
              :show-text="false"
            />
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-label">本月评价数量</div>
            <div class="stat-value-row">
              <span class="stat-value warning">{{ stats.totalResponses.toLocaleString() }}</span>
              <span class="stat-change" :class="getChangeClass(stats.responseCountChange)">
                <el-icon v-if="stats.responseCountChange > 0"><Top /></el-icon>
                <el-icon v-else-if="stats.responseCountChange < 0"><Bottom /></el-icon>
                {{ stats.responseCountChange > 0 ? '+' : '' }}{{ stats.responseCountChange }}
              </span>
            </div>
            <div class="stat-sub">上月：{{ stats.lastMonthResponses.toLocaleString() }}</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-content">
            <div class="stat-label">净推荐值 NPS</div>
            <div class="stat-value-row">
              <span class="stat-value info">{{ stats.nps.toFixed(1) }}</span>
              <span class="stat-change" :class="getChangeClass(stats.npsChange)">
                <el-icon v-if="stats.npsChange > 0"><Top /></el-icon>
                <el-icon v-else-if="stats.npsChange < 0"><Bottom /></el-icon>
                {{ stats.npsChange > 0 ? '+' : '' }}{{ stats.npsChange.toFixed(1) }}
              </span>
            </div>
            <div class="nps-bar">
              <div class="nps-segment promoters">推荐者 {{ stats.promoters }}%</div>
              <div class="nps-segment passives">被动者 {{ stats.passives }}%</div>
              <div class="nps-segment detractors">贬损者 {{ stats.detractors }}%</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="chart-title">满意度趋势</span>
          </template>
          <div ref="trendChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="chart-title">各维度评分</span>
          </template>
          <div ref="radarChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="chart-title">科室满意度排名</span>
          </template>
          <div ref="deptChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card" shadow="hover">
          <template #header>
            <span class="chart-title">评分分布</span>
          </template>
          <div ref="pieChartRef" class="chart-container"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card class="table-card" shadow="hover">
      <template #header>
        <span class="chart-title">科室满意度明细</span>
      </template>
      <el-table :data="deptTableData" :stripe="true" :header-cell-style="{ background: '#f5f7fa', color: '#606266' }">
        <el-table-column prop="ranking" label="排名" width="80" align="center">
          <template #default="{ row }">
            <span :class="getRankingClass(row.ranking)">{{ row.ranking }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="科室名称" min-width="150" />
        <el-table-column prop="responseCount" label="评价数量" width="120" align="center">
          <template #default="{ row }">
            {{ row.responseCount.toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column prop="avgScore" label="平均评分" width="120" align="center">
          <template #default="{ row }">
            <span class="score-badge" :style="{ background: getScoreColor(row.avgScore) }">
              {{ row.avgScore.toFixed(1) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="satisfactionRate" label="满意率" width="120" align="center">
          <template #default="{ row }">
            <el-progress
              :percentage="row.satisfactionRate * 100"
              :stroke-width="10"
              :color="getProgressColor(row.satisfactionRate)"
              :show-text="true"
              :format="(val: number) => val.toFixed(0) + '%'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="nps" label="NPS" width="100" align="center">
          <template #default="{ row }">
            <span :class="getNpsClass(row.nps)">{{ row.nps.toFixed(1) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="趋势" width="150" align="center">
          <template #default="{ row }">
            <div class="trend-sparkline" :ref="el => setSparklineRef(el, row)"></div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, Refresh, Top, Bottom } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { getSatisfactionStatistics, type SatisfactionStatistics, type DeptScore } from '@/api/satisfaction'

const loading = ref(false)
const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive({
  startDate: '',
  endDate: '',
  deptId: undefined as number | undefined,
  period: 'MONTH'
})

const deptList = ref<{ id: number; deptName: string }[]>([])

const stats = reactive({
  overallAvgScore: 8.6,
  overallSatisfactionRate: 0.852,
  totalResponses: 1256,
  nps: 42.5,
  promoters: 65,
  passives: 25,
  detractors: 10,
  avgScoreChange: 0.3,
  satisfactionRateChange: 0.025,
  responseCountChange: 156,
  npsChange: 3.2,
  lastMonthResponses: 1100
})

const deptTableData = ref<DeptScore[]>([])

const trendChartRef = ref<HTMLElement>()
const radarChartRef = ref<HTMLElement>()
const deptChartRef = ref<HTMLElement>()
const pieChartRef = ref<HTMLElement>()

let trendChart: echarts.ECharts | null = null
let radarChart: echarts.ECharts | null = null
let deptChart: echarts.ECharts | null = null
let pieChart: echarts.ECharts | null = null

const progressColor = '#409EFF'
const successColor = '#67C23A'

const getChangeClass = (change: number) => {
  if (change > 0) return 'positive'
  if (change < 0) return 'negative'
  return ''
}

const getRankingClass = (ranking: number) => {
  if (ranking <= 3) return 'ranking-top'
  return ''
}

const getScoreColor = (score: number) => {
  if (score >= 9) return '#67C23A'
  if (score >= 8) return '#85CE61'
  if (score >= 7) return '#E6A23C'
  if (score >= 6) return '#F56C6C'
  return '#909399'
}

const getProgressColor = (rate: number) => {
  if (rate >= 0.9) return '#67C23A'
  if (rate >= 0.8) return '#85CE61'
  if (rate >= 0.7) return '#E6A23C'
  if (rate >= 0.6) return '#F56C6C'
  return '#909399'
}

const getNpsClass = (nps: number) => {
  if (nps >= 50) return 'nps-excellent'
  if (nps >= 30) return 'nps-good'
  if (nps >= 0) return 'nps-average'
  return 'nps-poor'
}

const sparklineRefs = new Map<number, HTMLElement>()

const setSparklineRef = (el: HTMLElement | null, row: DeptScore) => {
  if (el) {
    sparklineRefs.set(row.ranking, el)
  }
}

const initTrendChart = () => {
  if (!trendChartRef.value) return

  trendChart = echarts.init(trendChartRef.value)

  const months = ['2024-01', '2024-02', '2024-03', '2024-04', '2024-05', '2024-06', '2024-07', '2024-08', '2024-09', '2024-10', '2024-11', '2024-12']
  const scores = [8.2, 8.3, 8.4, 8.3, 8.5, 8.6, 8.5, 8.7, 8.6, 8.7, 8.8, 8.9]
  const rates = [0.78, 0.79, 0.80, 0.81, 0.82, 0.83, 0.84, 0.85, 0.84, 0.86, 0.87, 0.88]

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      formatter: (params: any) => {
        let result = params[0].name + '<br/>'
        params.forEach((p: any) => {
          result += `${p.marker} ${p.seriesName}: ${p.seriesName === '满意率' ? (p.value * 100).toFixed(1) + '%' : p.value.toFixed(1)}<br/>`
        })
        return result
      }
    },
    legend: {
      data: ['满意度评分', '满意率'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: months,
      axisLabel: { rotate: 30 }
    },
    yAxis: [
      {
        type: 'value',
        name: '评分',
        min: 7,
        max: 10,
        axisLabel: { formatter: '{value}' }
      },
      {
        type: 'value',
        name: '满意率',
        min: 0.7,
        max: 1,
        axisLabel: { formatter: (val: number) => (val * 100).toFixed(0) + '%' }
      }
    ],
    series: [
      {
        name: '满意度评分',
        type: 'line',
        data: scores,
        smooth: true,
        itemStyle: { color: '#409EFF' },
        lineStyle: { width: 3 },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])}
      },
      {
        name: '满意率',
        type: 'line',
        yAxisIndex: 1,
        data: rates,
        smooth: true,
        itemStyle: { color: '#67C23A' },
        lineStyle: { width: 3 }
      }
    ],
    dataZoom: [
      { type: 'inside', start: 0, end: 100 },
      { type: 'slider', start: 0, end: 100 }
    ]
  }

  trendChart.setOption(option)
}

const initRadarChart = () => {
  if (!radarChartRef.value) return

  radarChart = echarts.init(radarChartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item'
    },
    legend: {
      data: ['本月', '上月'],
      bottom: 0
    },
    radar: {
      indicator: [
        { name: '服务态度', max: 10 },
        { name: '医疗技术', max: 10 },
        { name: '就诊环境', max: 10 },
        { name: '等候时间', max: 10 },
        { name: '沟通解释', max: 10 }
      ],
      radius: '65%'
    },
    series: [
      {
        type: 'radar',
        data: [
          {
            value: [8.8, 8.6, 8.9, 8.2, 8.7],
            name: '本月',
            itemStyle: { color: '#409EFF' },
            areaStyle: { color: 'rgba(64, 158, 255, 0.3)' }
          },
          {
            value: [8.5, 8.3, 8.6, 7.9, 8.4],
            name: '上月',
            itemStyle: { color: '#909399' },
            areaStyle: { color: 'rgba(144, 147, 153, 0.2)' }
          }
        ]
      }
    ]
  }

  radarChart.setOption(option)
}

const initDeptChart = () => {
  if (!deptChartRef.value) return

  deptChart = echarts.init(deptChartRef.value)

  const depts = ['急诊科', '内科', '外科', '儿科', '妇产科', '门诊部', '检验科', '放射科', '药剂科', '护理部']
  const scores = [9.2, 8.9, 8.7, 8.6, 8.5, 8.4, 8.3, 8.2, 8.1, 7.9]

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' },
      formatter: (params: any) => {
        const p = params[0]
        return `${p.name}<br/>满意度: <strong>${p.value.toFixed(1)}</strong>`
      }
    },
    grid: {
      left: '3%',
      right: '10%',
      bottom: '3%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'value',
      min: 7,
      max: 10,
      axisLabel: { formatter: '{value}' }
    },
    yAxis: {
      type: 'category',
      data: depts.reverse(),
      axisLabel: { fontSize: 12 }
    },
    series: [
      {
        type: 'bar',
        data: scores.reverse(),
        barWidth: 20,
        itemStyle: {
          color: (params: any) => {
            const value = params.value
            if (value >= 9) return '#67C23A'
            if (value >= 8) return '#85CE61'
            if (value >= 7) return '#E6A23C'
            return '#F56C6C'
          },
          borderRadius: [0, 4, 4, 0]
        },
        label: {
          show: true,
          position: 'right',
          formatter: '{c}',
          fontSize: 12
        }
      }
    ]
  }

  deptChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return

  pieChart = echarts.init(pieChartRef.value)

  const option: echarts.EChartsOption = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: '5%',
      top: 'center',
      itemWidth: 12,
      itemHeight: 12
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '50%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 8,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}\n{d}%',
          fontSize: 12
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 14,
            fontWeight: 'bold'
          }
        },
        data: [
          { value: 580, name: '非常满意(9-10)', itemStyle: { color: '#67C23A' } },
          { value: 320, name: '满意(7-8)', itemStyle: { color: '#85CE61' } },
          { value: 180, name: '一般(5-6)', itemStyle: { color: '#E6A23C' } },
          { value: 100, name: '不满意(3-4)', itemStyle: { color: '#F56C6C' } },
          { value: 76, name: '非常不满意(1-2)', itemStyle: { color: '#909399' } }
        ]
      }
    ]
  }

  pieChart.setOption(option)
}

const initSparklines = () => {
  sparklineRefs.forEach((el, ranking) => {
    if (!el) return

    const chart = echarts.init(el)
    const trendData = Array.from({ length: 6 }, () => Math.random() * 2 + 7.5)

    chart.setOption({
      grid: { left: 0, right: 0, top: 5, bottom: 5 },
      xAxis: { type: 'category', show: false, data: ['1', '2', '3', '4', '5', '6'] },
      yAxis: { type: 'value', show: false, min: 7, max: 10 },
      series: [{
        type: 'line',
        data: trendData,
        smooth: true,
        symbol: 'none',
        lineStyle: { width: 2, color: '#409EFF' },
        areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
          { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
        ])}
      }]
    })
  })
}

const fetchData = async () => {
  loading.value = true
  try {
    if (dateRange.value) {
      queryForm.startDate = dateRange.value[0]
      queryForm.endDate = dateRange.value[1]
    }

    const res = await getSatisfactionStatistics(queryForm)
    if (res.code === 200 && res.data) {
      const data = res.data

      stats.overallAvgScore = data.overallAvgScore || 0
      stats.overallSatisfactionRate = data.overallSatisfactionRate || 0
      stats.totalResponses = data.totalResponses || 0
      stats.nps = data.nps || 0
      stats.promoters = data.promoters || 0
      stats.passives = data.passives || 0
      stats.detractors = data.detractors || 0

      deptTableData.value = data.deptRanking || []
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取数据失败')

    deptTableData.value = [
      { deptId: 1, deptName: '急诊科', avgScore: 9.2, satisfactionRate: 0.95, responseCount: 156, ranking: 1, nps: 65.2 },
      { deptId: 2, deptName: '内科', avgScore: 8.9, satisfactionRate: 0.92, responseCount: 234, ranking: 2, nps: 58.6 },
      { deptId: 3, deptName: '外科', avgScore: 8.7, satisfactionRate: 0.88, responseCount: 189, ranking: 3, nps: 52.3 },
      { deptId: 4, deptName: '儿科', avgScore: 8.6, satisfactionRate: 0.86, responseCount: 145, ranking: 4, nps: 48.9 },
      { deptId: 5, deptName: '妇产科', avgScore: 8.5, satisfactionRate: 0.85, responseCount: 167, ranking: 5, nps: 45.2 }
    ]
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchData()
}

const handleReset = () => {
  dateRange.value = null
  Object.keys(queryForm).forEach(key => {
    if (key === 'period') queryForm.period = 'MONTH'
    else (queryForm as any)[key] = key === 'deptId' ? undefined : ''
  })
  fetchData()
}

const handleRefresh = () => {
  fetchData()
  nextTick(() => {
    initTrendChart()
    initRadarChart()
    initDeptChart()
    initPieChart()
    initSparklines()
  })
}

const handleResize = () => {
  trendChart?.resize()
  radarChart?.resize()
  deptChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  deptList.value = [
    { id: 1, deptName: '急诊科' },
    { id: 2, deptName: '内科' },
    { id: 3, deptName: '外科' },
    { id: 4, deptName: '儿科' },
    { id: 5, deptName: '妇产科' }
  ]

  fetchData()

  nextTick(() => {
    initTrendChart()
    initRadarChart()
    initDeptChart()
    initPieChart()
    initSparklines()
  })

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trendChart?.dispose()
  radarChart?.dispose()
  deptChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.satisfaction-dashboard {
  padding: 0;
}

.filter-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding: 16px;
  }
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.stats-row {
  margin-bottom: 16px;
}

.stat-card {
  .stat-content {
    padding: 8px 0;

    .stat-label {
      font-size: 14px;
      color: $text-secondary;
      margin-bottom: 8px;
    }

    .stat-value-row {
      display: flex;
      align-items: baseline;
      gap: 12px;
      margin-bottom: 12px;

      .stat-value {
        font-size: 36px;
        font-weight: 700;
        line-height: 1;

        &.primary { color: $primary-color; }
        &.success { color: $success-color; }
        &.warning { color: $warning-color; }
        &.info { color: #909399; }
      }

      .stat-change {
        font-size: 14px;
        display: flex;
        align-items: center;
        gap: 2px;

        &.positive { color: $success-color; }
        &.negative { color: $danger-color; }
      }
    }

    .stat-sub {
      font-size: 12px;
      color: $text-placeholder;
      margin-top: 4px;
    }
  }

  .nps-bar {
    display: flex;
    height: 24px;
    border-radius: 12px;
    overflow: hidden;
    margin-top: 8px;

    .nps-segment {
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 11px;
      color: #fff;
      font-weight: 500;

      &.promoters { background: $success-color; flex: 0.65; }
      &.passives { background: #909399; flex: 0.25; }
      &.detractors { background: $danger-color; flex: 0.10; }
    }
  }
}

.chart-row {
  margin-bottom: 16px;
}

.chart-card {
  height: 380px;

  :deep(.el-card__header) {
    padding: 12px 20px;
    background: #fafafa;
    font-weight: 600;
  }

  :deep(.el-card__body) {
    padding: 16px;
    height: calc(100% - 50px);
  }

  .chart-title {
    font-size: 15px;
    color: $text-primary;
  }

  .chart-container {
    width: 100%;
    height: 100%;
  }
}

.table-card {
  :deep(.el-card__header) {
    padding: 12px 20px;
    background: #fafafa;
    font-weight: 600;
  }

  :deep(.el-card__body) {
    padding: 16px;
  }

  .ranking-top {
    display: inline-block;
    width: 24px;
    height: 24px;
    line-height: 24px;
    text-align: center;
    background: $warning-color;
    color: #fff;
    border-radius: 50%;
    font-weight: 600;
  }

  .score-badge {
    display: inline-block;
    padding: 2px 10px;
    border-radius: 12px;
    color: #fff;
    font-weight: 600;
    font-size: 13px;
  }

  .nps-excellent { color: $success-color; font-weight: 600; }
  .nps-good { color: #85CE61; font-weight: 600; }
  .nps-average { color: $warning-color; }
  .nps-poor { color: $danger-color; }

  .trend-sparkline {
    width: 100px;
    height: 30px;
  }
}
</style>
