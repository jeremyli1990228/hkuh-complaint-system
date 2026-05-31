<template>
  <div class="survey-page">
    <van-nav-bar
      title="满意度调查"
      left-arrow
      @click-left="onClickLeft"
    />

    <div v-if="!submitted" class="survey-content">
      <div class="survey-header">
        <van-icon name="certificate" size="48" color="#1989fa" />
        <h2>感谢您的参与</h2>
        <p>您的评价将帮助我们持续改进服务质量，所有信息严格保密。</p>
      </div>

      <van-form ref="formRef" @submit="onSubmit" :show-error-message="false">
        <van-cell-group inset class="form-group">
          <div class="group-title">基本信息</div>

          <van-field
            v-model="formData.department"
            is-link
            readonly
            label="就诊科室"
            placeholder="请选择就诊科室"
            @click="showDeptPicker = true"
            :rules="[{ required: true, message: '请选择就诊科室' }]"
          />

          <van-field
            v-model="formData.visitDate"
            is-link
            readonly
            label="就诊日期"
            placeholder="请选择就诊日期"
            @click="showDatePicker = true"
            :rules="[{ required: true, message: '请选择就诊日期' }]"
          />

          <van-field
            v-model="formData.patientName"
            label="姓名"
            placeholder="选填"
            maxlength="20"
          />

          <van-field
            v-model="formData.contactPhone"
            type="tel"
            label="联系电话"
            placeholder="选填"
            maxlength="11"
          />
        </van-cell-group>

        <van-cell-group inset class="form-group">
          <div class="group-title">总体评分</div>

          <div class="overall-rating">
            <div class="rating-label">
              <span>请为本次就医体验打分</span>
              <span class="rating-score">{{ formData.overallScore }}</span>
            </div>

            <div class="rating-slider">
              <span class="slider-min">1</span>
              <van-slider
                v-model="formData.overallScore"
                :min="1"
                :max="10"
                :step="1"
                active-color="#1989fa"
                inactive-color="#ebedf0"
              />
              <span class="slider-max">10</span>
            </div>

            <div class="rating-desc">
              <span
                :class="{ active: formData.overallScore >= 1 && formData.overallScore <= 2 }"
              >非常不满意</span>
              <span
                :class="{ active: formData.overallScore >= 3 && formData.overallScore <= 4 }"
              >不满意</span>
              <span
                :class="{ active: formData.overallScore >= 5 && formData.overallScore <= 6 }"
              >一般</span>
              <span
                :class="{ active: formData.overallScore >= 7 && formData.overallScore <= 8 }"
              >满意</span>
              <span
                :class="{ active: formData.overallScore >= 9 && formData.overallScore <= 10 }"
              >非常满意</span>
            </div>
          </div>
        </van-cell-group>

        <van-cell-group inset class="form-group">
          <div class="group-title">分项评价</div>

          <div
            v-for="(item, index) in dimensionRatings"
            :key="index"
            class="dimension-item"
          >
            <div class="dimension-header">
              <span class="dimension-name">{{ item.name }}</span>
              <van-rate
                v-model="item.score"
                :size="18"
                color="#ffd21e"
                void-icon="star"
                void-color="#eee"
              />
            </div>
            <div v-if="item.comment !== undefined" class="dimension-comment">
              <van-field
                v-model="item.comment"
                type="textarea"
                placeholder="请描述具体情况（选填）"
                rows="2"
                autosize
                maxlength="200"
                show-word-limit
              />
            </div>
          </div>
        </van-cell-group>

        <van-cell-group inset class="form-group">
          <div class="group-title">意见建议</div>

          <van-field
            v-model="formData.suggestions"
            type="textarea"
            label="您的建议"
            placeholder="请留下您的宝贵意见和建议"
            rows="4"
            autosize
            maxlength="1000"
            show-word-limit
          />
        </van-cell-group>

        <div class="submit-section">
          <van-button
            type="primary"
            block
            round
            size="large"
            :loading="submitting"
            loading-text="提交中..."
            @click="onConfirmSubmit"
          >
            提交评价
          </van-button>
        </div>
      </van-form>
    </div>

    <div v-else class="success-page">
      <div class="success-content">
        <van-icon name="checked" size="80" color="#07c160" class="success-icon" />
        <h2>感谢您的评价！</h2>
        <p>您的反馈对我们非常重要</p>
        <p class="success-tip">我们将认真对待您的每一条建议</p>
      </div>

      <van-button
        type="primary"
        block
        round
        size="large"
        @click="goHome"
        class="home-button"
      >
        返回首页
      </van-button>
    </div>

    <van-popup v-model:show="showDeptPicker" position="bottom">
      <van-picker
        :columns="departmentColumns"
        @confirm="onDeptConfirm"
        @cancel="showDeptPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        v-model="currentDate"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <van-dialog
      v-model:show="showConfirmDialog"
      title="确认提交"
      message="请确认您已填写完整的评价信息，提交后将无法修改。"
      show-cancel-button
      confirm-button-text="确认提交"
      cancel-button-text="再看看"
      @confirm="confirmSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast } from 'vant'
import { submitSurvey, type SurveyFormData } from '@/api/survey'
import { getDepts } from '@/api/common'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const submitted = ref(false)

const showDeptPicker = ref(false)
const showDatePicker = ref(false)
const showConfirmDialog = ref(false)

const minDate = new Date(2020, 0, 1)
const maxDate = new Date()

const currentDate = reactive({
  year: new Date().getFullYear().toString(),
  month: (new Date().getMonth() + 1).toString().padStart(2, '0'),
  day: new Date().getDate().toString().padStart(2, '0')
})

const formData = reactive({
  department: '',
  departmentId: 0,
  visitDate: '',
  patientName: '',
  contactPhone: '',
  overallScore: 8,
  suggestions: ''
})

interface DimensionItem {
  name: string
  key: string
  score: number
  comment?: string
}

const dimensionRatings = reactive<DimensionItem[]>([
  { name: '医生服务态度', key: 'doctor_service', score: 5 },
  { name: '护理服务态度', key: 'nurse_service', score: 5 },
  { name: '医疗技术水平', key: 'medical_skill', score: 5, comment: '' },
  { name: '就诊环境', key: 'environment', score: 5, comment: '' },
  { name: '等候时间', key: 'waiting_time', score: 5, comment: '' },
  { name: '收费透明度', key: 'fee_transparency', score: 5, comment: '' },
  { name: '沟通解释', key: 'communication', score: 5, comment: '' }
])

const departmentColumns = ref([
  { text: '内科', value: '1' },
  { text: '外科', value: '2' },
  { text: '儿科', value: '3' },
  { text: '妇产科', value: '4' },
  { text: '骨科', value: '5' },
  { text: '眼科', value: '6' },
  { text: '口腔科', value: '7' },
  { text: '皮肤科', value: '8' },
  { text: '神经科', value: '9' },
  { text: '肿瘤科', value: '10' }
])

const onClickLeft = () => {
  router.back()
}

const onDeptConfirm = ({ selectedOptions }: { selectedOptions: { text: string; value: string }[] }) => {
  formData.department = selectedOptions[0]?.text || ''
  formData.departmentId = parseInt(selectedOptions[0]?.value || '0')
  showDeptPicker.value = false
}

const onDateConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  formData.visitDate = selectedValues.join('-')
  showDatePicker.value = false
}

const onConfirmSubmit = () => {
  if (!formData.department) {
    showToast('请选择就诊科室')
    return
  }
  if (!formData.visitDate) {
    showToast('请选择就诊日期')
    return
  }
  showConfirmDialog.value = true
}

const confirmSubmit = async () => {
  showConfirmDialog.value = false

  const surveyData: SurveyFormData = {
    departmentId: formData.departmentId,
    visitDate: formData.visitDate,
    patientName: formData.patientName,
    contactPhone: formData.contactPhone,
    overallScore: formData.overallScore,
    dimensionScores: dimensionRatings.map(item => ({
      dimension: item.key,
      score: item.score,
      comment: item.comment || ''
    })),
    suggestions: formData.suggestions
  }

  submitting.value = true

  try {
    const res = await submitSurvey(surveyData)

    if (res.code === 200) {
      submitted.value = true
      showToast('提交成功')
    } else {
      showFailToast(res.message || '提交失败，请稍后重试')
    }
  } catch (error: any) {
    showFailToast(error.message || '网络错误，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const goHome = () => {
  router.replace('/')
}

const fetchDepartments = async () => {
  try {
    const res = await getDepts()
    if (res.code === 200 && res.data) {
      departmentColumns.value = res.data.map((dept: { id: number; name: string }) => ({
        text: dept.name,
        value: dept.id.toString()
      }))
    }
  } catch (error) {
    console.error('获取科室列表失败', error)
  }
}

onMounted(() => {
  fetchDepartments()
})
</script>

<style scoped lang="scss">
.survey-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

.survey-content {
  padding-top: 12px;
}

.survey-header {
  background: linear-gradient(135deg, #1989fa 0%, #0c7cd5 100%);
  color: #fff;
  padding: 32px 20px;
  text-align: center;

  h2 {
    margin: 16px 0 8px;
    font-size: 22px;
    font-weight: 600;
  }

  p {
    margin: 0;
    font-size: 14px;
    opacity: 0.9;
    line-height: 1.5;
  }
}

.form-group {
  margin: 12px 0;

  .group-title {
    padding: 12px 16px 8px;
    font-size: 14px;
    font-weight: 600;
    color: #323233;
  }
}

.overall-rating {
  padding: 16px;

  .rating-label {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;

    span:first-child {
      font-size: 15px;
      color: #323233;
    }

    .rating-score {
      font-size: 24px;
      font-weight: 600;
      color: #1989fa;
    }
  }

  .rating-slider {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 12px;

    .van-slider {
      flex: 1;
    }

    .slider-min,
    .slider-max {
      font-size: 12px;
      color: #969799;
      min-width: 16px;
    }
  }

  .rating-desc {
    display: flex;
    justify-content: space-between;
    font-size: 11px;
    color: #969799;

    span {
      transition: color 0.3s;
    }

    span.active {
      color: #1989fa;
      font-weight: 500;
    }
  }
}

.dimension-item {
  padding: 12px 16px;
  border-bottom: 1px solid #f5f5f5;

  &:last-child {
    border-bottom: none;
  }

  .dimension-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .dimension-name {
      font-size: 14px;
      color: #323233;
    }
  }

  .dimension-comment {
    :deep(.van-field) {
      padding: 8px 0;
      background: #f7f8fa;
      border-radius: 4px;

      .van-field__body {
        background: transparent;
      }
    }
  }
}

.submit-section {
  padding: 20px 16px;
}

.success-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  background: #fff;
  padding: 40px 20px;
}

.success-content {
  text-align: center;
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;

  .success-icon {
    animation: scaleIn 0.5s ease-out;
  }

  h2 {
    margin: 20px 0 8px;
    font-size: 24px;
    font-weight: 600;
    color: #323233;
  }

  p {
    margin: 0;
    font-size: 16px;
    color: #646566;
    line-height: 1.6;
  }

  .success-tip {
    margin-top: 12px;
    font-size: 14px;
    color: #969799;
  }
}

.home-button {
  width: 100%;
  margin-top: 40px;
}

@keyframes scaleIn {
  0% {
    transform: scale(0);
    opacity: 0;
  }
  50% {
    transform: scale(1.2);
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

:deep(.van-cell-group--inset) {
  margin: 0 12px;
}

:deep(.van-field__label) {
  width: 80px;
}
</style>
