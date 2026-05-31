<template>
  <div class="page-container detail-page">
    <van-nav-bar title="投诉详情" left-arrow @click-left="onClickLeft" />
    
    <div class="detail-card">
      <div class="detail-header">
        <h2 class="detail-title">{{ detail.title }}</h2>
        <van-tag :type="getStatusType(detail.status)" size="medium">{{ detail.status }}</van-tag>
      </div>
      
      <div class="detail-info">
        <div class="info-item">
          <span class="info-label">投诉类型：</span>
          <span class="info-value">{{ detail.type }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">提交时间：</span>
          <span class="info-value">{{ detail.time }}</span>
        </div>
        <div class="info-item">
          <span class="info-label">联系方式：</span>
          <span class="info-value">{{ detail.contact }}</span>
        </div>
      </div>
      
      <div class="detail-content">
        <h3>详细内容</h3>
        <p>{{ detail.content }}</p>
      </div>
      
      <div v-if="detail.images && detail.images.length > 0" class="detail-images">
        <h3>附件图片</h3>
        <div class="image-grid">
          <img v-for="(img, index) in detail.images" :key="index" :src="img" class="image-item" />
        </div>
      </div>
      
      <div v-if="detail.reply" class="detail-reply">
        <h3>回复内容</h3>
        <div class="reply-box">
          <p>{{ detail.reply.content }}</p>
          <span class="reply-time">{{ detail.reply.time }}</span>
        </div>
      </div>
      
      <div v-if="detail.status === '已完成' && !detail.surveyed" class="survey-btn">
        <van-button type="primary" round block @click="goToSurvey">填写满意度调查</van-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const detail = ref({
  id: 0,
  title: '',
  type: '',
  content: '',
  status: '',
  time: '',
  contact: '',
  images: [],
  reply: null,
  surveyed: false
})

const onClickLeft = () => {
  router.back()
}

const getStatusType = (status: string) => {
  const map: Record<string, any> = {
    '待处理': 'danger',
    '处理中': 'warning',
    '已完成': 'success'
  }
  return map[status] || 'default'
}

const goToSurvey = () => {
  router.push(`/survey?id=${detail.value.id}`)
}

onMounted(() => {
  // 模拟数据
  detail.value = {
    id: Number(route.params.id),
    title: '服务态度问题',
    type: '服务态度',
    content: '医生态度不好，希望改进，希望医院能加强对医护人员的服务培训，提升服务质量。',
    status: '已完成',
    time: '2024-01-15 10:30',
    contact: '138****0000',
    images: [
      'https://via.placeholder.com/100x100',
      'https://via.placeholder.com/100x100'
    ],
    reply: {
      content: '非常感谢您的反馈，我们已对相关人员进行培训，欢迎继续监督！',
      time: '2024-01-16 09:00'
    },
    surveyed: false
  }
})
</script>

<style scoped lang="scss">
.detail-page {
  background-color: #f7f8fa;
  padding: 16px;
  
  .detail-card {
    background-color: #fff;
    border-radius: 8px;
    padding: 16px;
    
    .detail-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 16px;
      
      .detail-title {
        font-size: 18px;
        font-weight: 500;
        color: #323233;
        flex: 1;
        margin-right: 12px;
      }
    }
    
    .detail-info {
      padding-bottom: 16px;
      border-bottom: 1px solid #ebedf0;
      
      .info-item {
        margin-bottom: 8px;
        font-size: 14px;
        
        .info-label {
          color: #969799;
        }
        
        .info-value {
          color: #323233;
        }
      }
    }
    
    .detail-content,
    .detail-images,
    .detail-reply {
      margin-top: 16px;
      padding-top: 16px;
      border-top: 1px solid #ebedf0;
      
      h3 {
        font-size: 16px;
        color: #323233;
        margin-bottom: 8px;
      }
      
      p {
        font-size: 14px;
        color: #646566;
        line-height: 1.6;
      }
    }
    
    .detail-images {
      .image-grid {
        display: flex;
        gap: 8px;
        flex-wrap: wrap;
        
        .image-item {
          width: 100px;
          height: 100px;
          border-radius: 4px;
          object-fit: cover;
        }
      }
    }
    
    .detail-reply {
      .reply-box {
        background-color: #f7f8fa;
        padding: 12px;
        border-radius: 4px;
        
        .reply-time {
          display: block;
          margin-top: 8px;
          font-size: 12px;
          color: #969799;
          text-align: right;
        }
      }
    }
    
    .survey-btn {
      margin-top: 24px;
    }
  }
}
</style>
