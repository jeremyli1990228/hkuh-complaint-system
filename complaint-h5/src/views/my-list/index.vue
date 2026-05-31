<template>
  <div class="my-list-page">
    <van-nav-bar
      title="我的投诉"
      left-arrow
      @click-left="onClickLeft"
    >
      <template #right>
        <van-icon name="plus" size="20" @click="goToSubmit" />
      </template>
    </van-nav-bar>

    <van-tabs
      v-model:active="activeTab"
      sticky
      animated
      swipeable
      @change="onTabChange"
    >
      <van-tab title="全部" name="ALL" />
      <van-tab title="待处理" name="PENDING" />
      <van-tab title="处理中" name="PROCESSING" />
      <van-tab title="已关闭" name="CLOSED" />
      <van-tab title="已驳回" name="REJECTED" />

      <div class="tab-content">
        <van-dropdown-menu class="filter-menu">
          <van-dropdown-item
            v-model="filterType"
            :options="feedbackTypeOptions"
            @change="onFilterChange"
          />
          <van-dropdown-item
            v-model="sortOrder"
            :options="sortOptions"
            @change="onFilterChange"
          />
        </van-dropdown-menu>

        <van-pull-refresh v-model="refreshing" @refresh="onRefresh" class="pull-refresh">
          <van-list
            v-model:loading="loading"
            :finished="finished"
            :finished-text="finishedText"
            :error-text="errorText"
            @load="onLoad"
            class="list-container"
          >
            <div
              v-for="item in list"
              :key="item.id"
              class="list-item"
              @click="goToDetail(item)"
            >
              <div class="item-header">
                <div class="item-left">
                  <span v-if="item.priority === 'URGENT'" class="urgent-dot" />
                  <span class="item-no">{{ item.feedbackNo }}</span>
                </div>
                <div class="item-right">
                  <van-tag
                    :type="getStatusTagType(item.status)"
                    size="small"
                    plain
                  >
                    {{ getStatusText(item.status) }}
                  </van-tag>
                </div>
              </div>

              <div class="item-title">{{ item.title }}</div>

              <div class="item-meta">
                <van-tag :type="getTypeTagType(item.feedbackTypeName)" size="small">
                  {{ item.feedbackTypeName || '其他' }}
                </van-tag>
                <span class="item-time">{{ formatTime(item.createTime) }}</span>
              </div>

              <div v-if="item.lastRecord" class="item-record">
                <van-icon name="chat-o" size="14" />
                <span>{{ item.lastRecord }}</span>
              </div>
            </div>

            <van-empty
              v-if="!loading && list.length === 0"
              :image="EmptyImage"
              description="暂无投诉记录"
              class="empty-state"
            />
          </van-list>
        </van-pull-refresh>
      </div>
    </van-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast, Empty as EmptyImage } from 'vant'
import { getMyFeedbackList, type FeedbackItem } from '@/api/feedback'
import { getFeedbackTypes } from '@/api/common'

const router = useRouter()

const activeTab = ref('ALL')
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)
const errorText = ref('请求失败，点击重新加载')
const list = ref<FeedbackItem[]>([])
const page = ref(1)
const pageSize = ref(10)

const filterType = ref(0)
const sortOrder = ref('desc')

const feedbackTypes = ref<{ id: number; name: string }[]>([])

const feedbackTypeOptions = computed(() => [
  { text: '全部类型', value: 0 },
  ...feedbackTypes.value.map(t => ({ text: t.name, value: t.id }))
])

const sortOptions = [
  { text: '最新优先', value: 'desc' },
  { text: '最早优先', value: 'asc' }
]

const finishedText = computed(() => {
  return list.value.length > 0 ? '没有更多了' : ''
})

const statusMap: Record<string, string> = {
  ALL: '',
  PENDING: 'PENDING',
  PROCESSING: 'PROCESSING',
  REPLIED: 'REPLIED',
  CLOSED: 'CLOSED',
  REJECTED: 'REJECTED'
}

const onClickLeft = () => {
  router.back()
}

const goToSubmit = () => {
  router.push('/submit')
}

const goToDetail = (item: FeedbackItem) => {
  router.push(`/detail/${item.id}`)
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'warning',
    ASSIGNED: 'primary',
    PROCESSING: 'primary',
    REPLIED: 'success',
    CLOSED: 'success',
    REJECTED: 'danger',
    DRAFT: 'default'
  }
  return map[status] || 'default'
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待处理',
    ASSIGNED: '已指派',
    PROCESSING: '处理中',
    REPLIED: '已回复',
    CLOSED: '已关闭',
    REJECTED: '已驳回',
    DRAFT: '草稿'
  }
  return map[status] || status
}

const getTypeTagType = (typeName: string) => {
  const map: Record<string, string> = {
    '医疗质量': 'danger',
    '服务态度': 'warning',
    '等候时间': 'primary',
    '环境卫生': 'success',
    '收费问题': 'orange',
    '医患沟通': 'purple',
    '其他': 'default'
  }
  return map[typeName] || 'default'
}

const formatTime = (timeStr: string) => {
  if (!timeStr) return ''

  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (days === 0) {
    const hours = Math.floor(diff / (1000 * 60 * 60))
    if (hours === 0) {
      const minutes = Math.floor(diff / (1000 * 60))
      return minutes <= 0 ? '刚刚' : `${minutes}分钟前`
    }
    return `${hours}小时前`
  } else if (days === 1) {
    return '昨天'
  } else if (days < 7) {
    return `${days}天前`
  } else {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    if (year === now.getFullYear()) {
      return `${month}-${day}`
    }
    return `${year}-${month}-${day}`
  }
}

const fetchList = async (isRefresh = false) => {
  if (loading.value && !isRefresh) return

  loading.value = true

  try {
    const params: any = {
      page: page.value,
      pageSize: pageSize.value,
      sortOrder: sortOrder.value
    }

    if (activeTab.value !== 'ALL') {
      params.status = statusMap[activeTab.value]
    }

    if (filterType.value !== 0) {
      params.feedbackTypeId = filterType.value
    }

    const res = await getMyFeedbackList(params)

    if (res.code === 200) {
      const data = res.data?.list || []

      if (isRefresh) {
        list.value = data
        page.value = 1
      } else {
        list.value.push(...data)
      }

      if (data.length < pageSize.value) {
        finished.value = true
      } else {
        page.value++
      }
    } else {
      if (isRefresh) {
        finished.value = false
      }
      showToast(res.message || '获取数据失败')
    }
  } catch (error: any) {
    if (isRefresh) {
      finished.value = false
    }
    showFailToast(error.message || '网络错误，请稍后重试')
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

const onLoad = () => {
  fetchList()
}

const onRefresh = () => {
  finished.value = false
  page.value = 1
  fetchList(true)
}

const onTabChange = () => {
  list.value = []
  finished.value = false
  page.value = 1
  fetchList(true)
}

const onFilterChange = () => {
  list.value = []
  finished.value = false
  page.value = 1
  fetchList(true)
}

const fetchFeedbackTypes = async () => {
  try {
    const res = await getFeedbackTypes()
    if (res.code === 200 && res.data) {
      feedbackTypes.value = res.data
    }
  } catch (error) {
    console.error('获取反馈类型失败', error)
  }
}

onMounted(() => {
  fetchList()
  fetchFeedbackTypes()
})
</script>

<style scoped lang="scss">
.my-list-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: env(safe-area-inset-bottom);
}

.tab-content {
  min-height: calc(100vh - 90px);
}

.filter-menu {
  background: #fff;
  border-bottom: 1px solid #eee;

  :deep(.van-dropdown-menu__bar) {
    height: 44px;
    box-shadow: none;
  }
}

.pull-refresh {
  min-height: calc(100vh - 140px);
}

.list-container {
  padding: 12px 0;
}

.list-item {
  background: #fff;
  margin: 0 12px 12px;
  padding: 14px;
  border-radius: 8px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);

  &:active {
    background: #f5f5f5;
  }
}

.item-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.item-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.urgent-dot {
  width: 8px;
  height: 8px;
  background: #ee0a24;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.item-no {
  font-size: 12px;
  color: #969799;
}

.item-right {
  display: flex;
  align-items: center;
}

.item-title {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
  line-height: 1.5;
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;

  .van-tag {
    margin-right: auto;
  }
}

.item-time {
  font-size: 12px;
  color: #969799;
}

.item-record {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 8px 10px;
  background: #f7f8fa;
  border-radius: 4px;
  font-size: 12px;
  color: #646566;
  line-height: 1.4;
  overflow: hidden;

  .van-icon {
    flex-shrink: 0;
    margin-top: 1px;
    color: #969799;
  }

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
}

.empty-state {
  padding: 80px 0;
}

:deep(.van-nav-bar__right) {
  .van-icon {
    color: #1989fa;
    font-size: 22px;
  }
}

:deep(.van-tabs__nav) {
  background: #fff;
}

:deep(.van-tab) {
  flex-basis: 20% !important;
}
</style>
