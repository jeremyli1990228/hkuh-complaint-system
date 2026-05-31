<template>
  <div class="faq-page">
    <van-nav-bar
      title="常见问题"
      left-arrow
      @click-left="onClickLeft"
    />

    <van-search
      v-model="searchQuery"
      placeholder="搜索常见问题"
      shape="round"
      background="#fff"
      @update:model-value="onSearch"
      class="search-bar"
    />

    <van-tabs
      v-model:active="activeCategory"
      sticky
      scrollspy
      @change="onCategoryChange"
      class="category-tabs"
    >
      <van-tab title="全部" name="ALL" />
      <van-tab title="医疗质量" name="MEDICAL_QUALITY" />
      <van-tab title="服务态度" name="SERVICE_ATTITUDE" />
      <van-tab title="等候时间" name="WAITING_TIME" />
      <van-tab title="环境卫生" name="ENVIRONMENT" />
      <van-tab title="收费问题" name="CHARGES" />
      <van-tab title="医患沟通" name="COMMUNICATION" />
      <van-tab title="其他" name="OTHER" />

      <div class="faq-list">
        <van-collapse
          v-model="activeNames"
          accordion
          @change="onCollapseChange"
        >
          <van-collapse-item
            v-for="item in filteredList"
            :key="item.id"
            :name="item.id"
            :title="item.question"
            size="large"
          >
            <template #icon>
              <van-icon name="question-o" class="question-icon" />
            </template>
            <template #value>
              <div class="item-meta">
                <van-tag :type="getCategoryTagType(item.category)" size="small">
                  {{ getCategoryText(item.category) }}
                </van-tag>
                <span class="view-count">
                  <van-icon name="eye-o" />
                  {{ item.viewCount }}
                </span>
              </div>
            </template>

            <div class="answer-content" v-html="item.answer"></div>

            <div v-if="item.images && item.images.length > 0" class="answer-images">
              <van-image
                v-for="(img, index) in item.images"
                :key="index"
                :src="img"
                fit="cover"
                radius="4px"
                @click="previewImage(index)"
              />
            </div>
          </van-collapse-item>
        </van-collapse>

        <van-empty
          v-if="!loading && filteredList.length === 0"
          :image="EmptyImage"
          description="没有找到相关问题"
          class="empty-state"
        />
      </div>
    </van-tabs>

    <div class="quick-entry">
      <van-divider>快捷入口</van-divider>
      <van-grid :column="3" :gutter="10" square clickable>
        <van-grid-item icon="edit" text="在线投诉" @click="goToComplaint" />
        <van-grid-item icon="phone-o" text="电话投诉" @click="makePhoneCall" />
        <van-grid-item icon="weapp-nav" text="微信投诉" @click="showWechatQR" />
      </van-grid>
    </div>

    <van-action-sheet
      v-model:show="showPhoneSheet"
      :actions="phoneActions"
      cancel-text="取消"
      @select="onPhoneSelect"
    />

    <van-overlay :show="showQRCode" @click="showQRCode = false">
      <div class="qrcode-container" @click.stop>
        <van-icon name="close" class="close-icon" @click="showQRCode = false" />
        <div class="qrcode-title">微信投诉</div>
        <van-image
          :src="wechatQRUrl"
          width="200"
          height="200"
          fit="contain"
        />
        <div class="qrcode-hint">请使用微信扫码关注公众号进行投诉</div>
      </div>
    </van-overlay>

    <van-image-preview v-model:show="showImagePreview" :images="previewImages" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, Empty as EmptyImage } from 'vant'
import { getFAQList, type FAQItem } from '@/api/feedback'

const router = useRouter()

const searchQuery = ref('')
const activeCategory = ref('ALL')
const activeNames = ref<number | string>('')
const loading = ref(false)
const showPhoneSheet = ref(false)
const showQRCode = ref(false)
const showImagePreview = ref(false)
const previewImages = ref<string[]>([])

const wechatQRUrl = ref('/qrcode/wechat-complain.png')

const faqList = ref<FAQItem[]>([])
let searchTimer: ReturnType<typeof setTimeout> | null = null

const phoneActions = [
  { name: '投诉热线', subname: '0755-86913333', value: '0755-86913333' },
  { name: '服务监督', subname: '0755-86913366', value: '0755-86913366' }
]

const categoryMap: Record<string, string> = {
  ALL: '',
  MEDICAL_QUALITY: 'MEDICAL_QUALITY',
  SERVICE_ATTITUDE: 'SERVICE_ATTITUDE',
  WAITING_TIME: 'WAITING_TIME',
  ENVIRONMENT: 'ENVIRONMENT',
  CHARGES: 'CHARGES',
  COMMUNICATION: 'COMMUNICATION',
  OTHER: 'OTHER'
}

const filteredList = computed(() => {
  let result = faqList.value

  if (activeCategory.value !== 'ALL') {
    result = result.filter(item => item.category === categoryMap[activeCategory.value])
  }

  if (searchQuery.value.trim()) {
    const keyword = searchQuery.value.toLowerCase()
    result = result.filter(item =>
      item.question.toLowerCase().includes(keyword) ||
      item.answer.toLowerCase().includes(keyword)
    )
  }

  return result
})

const onClickLeft = () => {
  router.back()
}

const onSearch = () => {
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  searchTimer = setTimeout(() => {
    activeNames.value = ''
  }, 500)
}

const onCategoryChange = () => {
  activeNames.value = ''
}

const onCollapseChange = async (name: number) => {
  if (name && typeof name === 'number') {
    const item = faqList.value.find(f => f.id === name)
    if (item && !item.viewed) {
      item.viewed = true
      item.viewCount++
    }
  }
}

const getCategoryTagType = (category: string) => {
  const map: Record<string, string> = {
    MEDICAL_QUALITY: 'danger',
    SERVICE_ATTITUDE: 'warning',
    WAITING_TIME: 'primary',
    ENVIRONMENT: 'success',
    CHARGES: 'orange',
    COMMUNICATION: 'purple',
    OTHER: 'default'
  }
  return map[category] || 'default'
}

const getCategoryText = (category: string) => {
  const map: Record<string, string> = {
    MEDICAL_QUALITY: '医疗质量',
    SERVICE_ATTITUDE: '服务态度',
    WAITING_TIME: '等候时间',
    ENVIRONMENT: '环境卫生',
    CHARGES: '收费问题',
    COMMUNICATION: '医患沟通',
    OTHER: '其他'
  }
  return map[category] || '其他'
}

const goToComplaint = () => {
  router.push('/submit')
}

const makePhoneCall = () => {
  showPhoneSheet.value = true
}

const onPhoneSelect = (action: { value: string }) => {
  showPhoneSheet.value = false
  window.location.href = `tel:${action.value}`
}

const showWechatQR = () => {
  showQRCode.value = true
}

const previewImage = (index: number) => {
  const currentItem = filteredList.value.find(item => item.images && item.images.length > 0)
  if (currentItem?.images) {
    previewImages.value = currentItem.images
    showImagePreview.value = true
  }
}

const fetchFAQList = async () => {
  loading.value = true
  try {
    const res = await getFAQList()
    if (res.code === 200 && res.data) {
      faqList.value = res.data.list.map((item: FAQItem) => ({
        ...item,
        viewed: false,
        viewCount: item.viewCount || 0,
        images: item.images || []
      }))
    }
  } catch (error) {
    console.error('获取FAQ列表失败', error)
    showToast('获取数据失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchFAQList()
})

watch(searchQuery, () => {
  onSearch()
})
</script>

<style scoped lang="scss">
.faq-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 120px;
}

.search-bar {
  background: #fff;
  border-bottom: 1px solid #eee;
}

.category-tabs {
  :deep(.van-tabs__wrap) {
    background: #fff;
  }

  :deep(.van-tabs__nav) {
    padding: 0 8px;
  }

  :deep(.van-tab) {
    padding: 0 12px;
    flex-shrink: 0;
  }
}

.faq-list {
  padding: 12px;
}

:deep(.van-collapse-item) {
  margin-bottom: 10px;
  border-radius: 8px;
  overflow: hidden;

  .van-cell {
    background: #fff;
    font-weight: 500;
  }

  .van-cell--large {
    padding: 14px 16px;
  }
}

.question-icon {
  color: #1989fa;
  margin-right: 8px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
}

.view-count {
  display: flex;
  align-items: center;
  gap: 2px;
  font-size: 12px;
  color: #969799;

  .van-icon {
    font-size: 14px;
  }
}

.answer-content {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.6;
  color: #646566;
  background: #f7f8fa;
  border-radius: 4px;

  :deep(p) {
    margin: 0 0 8px;

    &:last-child {
      margin-bottom: 0;
    }
  }

  :deep(img) {
    max-width: 100%;
    border-radius: 4px;
    margin: 8px 0;
  }

  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 20px;
  }

  :deep(li) {
    margin-bottom: 4px;
  }
}

.answer-images {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;

  :deep(.van-image) {
    width: 80px;
    height: 80px;
  }
}

.empty-state {
  padding: 60px 0;
}

.quick-entry {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #fff;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 8px rgba(0, 0, 0, 0.06);

  :deep(.van-divider) {
    margin: 0 0 12px;
    color: #969799;
    font-size: 12px;
  }

  :deep(.van-grid-item__text) {
    color: #323233;
    font-size: 13px;
  }
}

.qrcode-container {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: #fff;
  border-radius: 12px;
  padding: 24px;
  text-align: center;
  width: 280px;

  .close-icon {
    position: absolute;
    top: 12px;
    right: 12px;
    font-size: 20px;
    color: #969799;
  }

  .qrcode-title {
    font-size: 18px;
    font-weight: 500;
    margin-bottom: 16px;
    color: #323233;
  }

  .qrcode-hint {
    margin-top: 12px;
    font-size: 12px;
    color: #969799;
  }
}
</style>
