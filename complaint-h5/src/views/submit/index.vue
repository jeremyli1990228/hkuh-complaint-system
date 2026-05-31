<template>
  <div class="submit-page">
    <van-nav-bar
      title="提交投诉/建议"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />

    <van-form ref="formRef" @submit="onSubmit" :show-error-message="false">
      <van-cell-group inset class="form-group">
        <van-field
          label="反馈类型"
          placeholder="请选择反馈类型"
          readonly
          :model-value="getFeedbackTypeText"
          is-link
          @click="showTypePopup = true"
          :rules="[{ required: true, message: '请选择反馈类型' }]"
        />
      </van-cell-group>

      <van-cell-group inset class="form-group">
        <van-field
          v-model="formData.title"
          label="标题"
          placeholder="请简要描述您的问题"
          maxlength="200"
          show-word-limit
          :rules="[{ required: true, message: '请输入标题' }]"
        />
        <van-field
          v-model="formData.content"
          type="textarea"
          label="详细描述"
          placeholder="请详细描述您遇到的问题，以便我们更好地处理"
          rows="5"
          autosize
          maxlength="2000"
          show-word-limit
          :rules="[{ required: true, message: '请输入详细描述' }]"
        />
      </van-cell-group>

      <van-cell-group inset class="form-group">
        <van-field label="图片上传" label-width="100">
          <template #input>
            <van-uploader
              v-model="fileList"
              :max-count="9"
              :max-size="5 * 1024 * 1024"
              :before-read="beforeRead"
              :after-read="afterRead"
              @oversize="onOversize"
              multiple
              accept="image/jpeg,image/png"
              preview-size="80"
            >
              <div class="upload-trigger">
                <van-icon name="plus" size="24" />
                <span>{{ fileList.length }}/9</span>
              </div>
            </van-uploader>
          </template>
        </van-field>
      </van-cell-group>

      <van-cell-group inset class="form-group">
        <van-field label="匿名提交">
          <template #input>
            <van-switch v-model="formData.isAnonymous" size="20" />
          </template>
        </van-field>
        <van-field
          v-if="!formData.isAnonymous"
          v-model="formData.contactPhone"
          type="tel"
          label="联系电话"
          placeholder="方便我们联系您"
          :rules="[{ required: !formData.isAnonymous, message: '请输入联系电话' }]"
        />
        <van-field
          v-if="!formData.isAnonymous"
          v-model="formData.contactEmail"
          label="联系邮箱"
          placeholder="选填"
        />
      </van-cell-group>

      <van-cell-group inset class="form-group">
        <van-field
          label="来院科室"
          placeholder="请选择科室"
          readonly
          :model-value="getDeptText"
          is-link
          @click="showDeptPicker = true"
        />
        <van-field
          label="来院时间"
          placeholder="请选择日期"
          readonly
          :model-value="formData.visitDate"
          is-link
          @click="showDatePicker = true"
        />
      </van-cell-group>

      <div class="submit-section">
        <van-button
          type="primary"
          block
          round
          :loading="submitting"
          :disabled="!canSubmit"
          loading-text="提交中..."
          @click="handleSubmit"
        >
          提交投诉/建议
        </van-button>
        <p class="submit-hint">
          提交即表示您同意我们的
          <span class="link" @click="showPrivacy = true">隐私政策</span>
        </p>
      </div>
    </van-form>

    <van-popup v-model:show="showTypePopup" position="bottom">
      <van-picker
        title="选择反馈类型"
        :columns="feedbackTypeColumns"
        @confirm="onTypeConfirm"
        @cancel="showTypePopup = false"
      />
    </van-popup>

    <van-popup v-model:show="showDeptPicker" position="bottom">
      <van-picker
        title="选择科室"
        :columns="deptColumns"
        @confirm="onDeptConfirm"
        @cancel="showDeptPicker = false"
      />
    </van-popup>

    <van-popup v-model:show="showDatePicker" position="bottom">
      <van-date-picker
        title="选择日期"
        :min-date="minDate"
        :max-date="maxDate"
        @confirm="onDateConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>

    <van-dialog
      v-model:show="showSuccessDialog"
      title="提交成功"
      :show-confirm-button="false"
      close-on-click-overlay
    >
      <div class="success-content">
        <van-icon name="checked" class="success-icon" />
        <p class="success-text">您的投诉/建议已提交成功！</p>
        <p class="feedback-no">
          工单编号：<span class="no">{{ submittedFeedbackNo }}</span>
        </p>
        <p class="success-hint">我们将尽快处理您的反馈，感谢您的支持！</p>
        <div class="success-actions">
          <van-button size="small" @click="goToMyList">查看我的投诉</van-button>
          <van-button size="small" type="primary" plain @click="continueSubmit">继续提交</van-button>
        </div>
      </div>
    </van-dialog>

    <van-dialog
      v-model:show="showPrivacy"
      title="隐私政策"
      :show-confirm-button="true"
      confirm-text="我已知晓"
    >
      <div class="privacy-content">
        <p>我们非常重视您的隐私保护。在提交投诉/建议时：</p>
        <ul>
          <li>您的个人信息仅用于处理投诉和联系您</li>
          <li>我们不会将您的信息泄露给第三方</li>
          <li>匿名提交时，您的个人信息将被隐藏</li>
          <li>投诉内容仅用于改善医疗服务</li>
        </ul>
      </div>
    </van-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showFailToast, type UploaderFileListItem } from 'vant'
import { submitFeedback, uploadFile } from '@/api/feedback'
import { getDepts, getFeedbackTypes } from '@/api/common'

const router = useRouter()

const formRef = ref()
const submitting = ref(false)
const showTypePopup = ref(false)
const showDeptPicker = ref(false)
const showDatePicker = ref(false)
const showSuccessDialog = ref(false)
const showPrivacy = ref(false)
const submittedFeedbackNo = ref('')

const fileList = ref<UploaderFileListItem[]>([])
const uploadedFiles = ref<string[]>([])
const uploadedAttachmentIds = ref<number[]>([])

const feedbackTypes = ref<{ id: number; name: string }[]>([
  { id: 1, name: '医疗质量' },
  { id: 2, name: '服务态度' },
  { id: 3, name: '等候时间' },
  { id: 4, name: '环境卫生' },
  { id: 5, name: '收费问题' },
  { id: 6, name: '医患沟通' },
  { id: 7, name: '其他' }
])

const deptList = ref<{ id: number; name: string; children?: { id: number; name: string }[] }[]>([
  { id: 1, name: '内科' },
  { id: 2, name: '外科' },
  { id: 3, name: '儿科' },
  { id: 4, name: '妇产科' },
  { id: 5, name: '急诊科' }
])

const formData = reactive({
  feedbackTypeId: null as number | null,
  feedbackTypeName: '',
  title: '',
  content: '',
  isAnonymous: false,
  contactPhone: '',
  contactEmail: '',
  deptId: null as number | null,
  deptName: '',
  visitDate: ''
})

const minDate = new Date(new Date().getFullYear() - 1, 0, 1)
const maxDate = new Date()

const feedbackTypeColumns = computed(() =>
  feedbackTypes.value.map(item => ({ text: item.name, value: item.id }))
)

const deptColumns = computed(() => [
  {
    values: deptList.value.map(item => ({ text: item.name, value: item.id })),
    className: 'dept-column'
  }
])

const getFeedbackTypeText = computed(() => {
  if (!formData.feedbackTypeId) return ''
  const type = feedbackTypes.value.find(t => t.id === formData.feedbackTypeId)
  return type?.name || ''
})

const getDeptText = computed(() => {
  if (!formData.deptId) return ''
  const dept = deptList.value.find(d => d.id === formData.deptId)
  return dept?.name || ''
})

const canSubmit = computed(() => {
  return (
    formData.feedbackTypeId !== null &&
    formData.title.trim() !== '' &&
    formData.content.trim() !== ''
  )
})

const onClickLeft = () => {
  if (formData.title || formData.content) {
    if (confirm('您有未提交的内容，确定要返回吗？')) {
      router.back()
    }
  } else {
    router.back()
  }
}

const onTypeConfirm = ({ selectedOptions }: any) => {
  if (selectedOptions && selectedOptions[0]) {
    formData.feedbackTypeId = selectedOptions[0].value
    formData.feedbackTypeName = selectedOptions[0].text
  }
  showTypePopup.value = false
}

const onDeptConfirm = ({ selectedOptions }: any) => {
  if (selectedOptions && selectedOptions[0]) {
    formData.deptId = selectedOptions[0].value
    formData.deptName = selectedOptions[0].text
  }
  showDeptPicker.value = false
}

const onDateConfirm = ({ selectedValues }: { selectedValues: string[] }) => {
  formData.visitDate = selectedValues.join('-')
  showDatePicker.value = false
}

const beforeRead = (file: File) => {
  if (file.size > 5 * 1024 * 1024) {
    showFailToast('图片大小不能超过5MB')
    return false
  }

  const allowedTypes = ['image/jpeg', 'image/png', 'image/jpg']
  if (!allowedTypes.includes(file.type)) {
    showFailToast('仅支持 JPG/PNG 格式')
    return false
  }

  return true
}

const afterRead = async (item: UploaderFileListItem) => {
  if (!item.file) return

  try {
    const formData = new FormData()
    formData.append('file', item.file as File)
    formData.append('businessType', 'FEEDBACK')

    const res = await uploadFile(formData)

    if (res.code === 200) {
      uploadedFiles.value.push(res.data.url)
      uploadedAttachmentIds.value.push(res.data.id)
      item.status = 'done'
    } else {
      item.status = 'failed'
      showFailToast('图片上传失败')
    }
  } catch (error) {
    item.status = 'failed'
    showFailToast('图片上传失败')
  }
}

const onOversize = () => {
  showFailToast('图片大小不能超过5MB')
}

const handleSubmit = async () => {
  try {
    await formRef.value?.validate()
  } catch {
    return
  }

  submitting.value = true

  try {
    const submitData = {
      feedbackTypeId: formData.feedbackTypeId,
      title: formData.title,
      content: formData.content,
      source: 'ONLINE',
      priority: 'NORMAL',
      isAnonymous: formData.isAnonymous,
      contactPhone: formData.isAnonymous ? undefined : formData.contactPhone,
      contactEmail: formData.isAnonymous ? undefined : formData.contactEmail,
      attachmentIds: uploadedAttachmentIds.value,
      complainant: formData.isAnonymous
        ? undefined
        : {
            phone: formData.contactPhone,
            email: formData.contactEmail
          }
    }

    const res = await submitFeedback(submitData)

    if (res.code === 200) {
      submittedFeedbackNo.value = res.data?.feedbackNo || 'FB' + Date.now()
      showSuccessDialog.value = true
      resetForm()
    } else {
      showFailToast(res.message || '提交失败')
    }
  } catch (error: any) {
    showFailToast(error.message || '提交失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

const onSubmit = () => {
  handleSubmit()
}

const resetForm = () => {
  formData.feedbackTypeId = null
  formData.feedbackTypeName = ''
  formData.title = ''
  formData.content = ''
  formData.isAnonymous = false
  formData.contactPhone = ''
  formData.contactEmail = ''
  formData.deptId = null
  formData.deptName = ''
  formData.visitDate = ''
  fileList.value = []
  uploadedFiles.value = []
  uploadedAttachmentIds.value = []
}

const goToMyList = () => {
  showSuccessDialog.value = false
  router.push('/my-list')
}

const continueSubmit = () => {
  showSuccessDialog.value = false
  resetForm()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const fetchInitData = async () => {
  try {
    const [typesRes, deptsRes] = await Promise.all([
      getFeedbackTypes(),
      getDepts()
    ])

    if (typesRes.code === 200 && typesRes.data) {
      feedbackTypes.value = typesRes.data
    }

    if (deptsRes.code === 200 && deptsRes.data) {
      deptList.value = deptsRes.data
    }
  } catch (error) {
    console.error('获取初始化数据失败', error)
  }
}

onMounted(() => {
  fetchInitData()
})
</script>

<style scoped lang="scss">
.submit-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

.form-group {
  margin: 12px 0;

  :deep(.van-cell) {
    padding: 14px 16px;
  }

  :deep(.van-field__label) {
    width: 80px;
    color: #323233;
    font-weight: 500;
  }
}

.upload-trigger {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 80px;
  height: 80px;
  background: #f7f8fa;
  border: 1px dashed #dcdee0;
  border-radius: 8px;
  color: #969799;
  font-size: 12px;

  span {
    margin-top: 4px;
  }
}

.submit-section {
  padding: 24px 16px;

  .submit-hint {
    margin-top: 12px;
    text-align: center;
    font-size: 12px;
    color: #969799;

    .link {
      color: #1989fa;
    }
  }
}

.success-content {
  padding: 24px 16px;
  text-align: center;

  .success-icon {
    font-size: 64px;
    color: #07c160;
    margin-bottom: 16px;
  }

  .success-text {
    font-size: 16px;
    color: #323233;
    margin-bottom: 16px;
  }

  .feedback-no {
    background: #f7f8fa;
    padding: 12px;
    border-radius: 8px;
    margin-bottom: 16px;
    font-size: 14px;
    color: #646566;

    .no {
      color: #1989fa;
      font-weight: 600;
    }
  }

  .success-hint {
    font-size: 13px;
    color: #969799;
    margin-bottom: 24px;
  }

  .success-actions {
    display: flex;
    gap: 12px;
    justify-content: center;

    .van-button {
      flex: 1;
    }
  }
}

.privacy-content {
  padding: 16px;
  font-size: 14px;
  line-height: 1.8;
  color: #646566;

  p {
    margin-bottom: 12px;
  }

  ul {
    padding-left: 20px;

    li {
      margin-bottom: 8px;
    }
  }
}

:deep(.van-uploader__preview) {
  margin-right: 8px;
  margin-bottom: 8px;
}

:deep(.van-uploader__upload) {
  margin-right: 8px;
  margin-bottom: 8px;
}
</style>
