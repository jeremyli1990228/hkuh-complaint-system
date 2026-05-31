<template>
  <div class="feedback-detail-container">
    <div class="detail-header">
      <el-button @click="goBack">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
      <div class="header-status">
        <el-tag v-if="feedback.status" :type="getStatusTagType(feedback.status)" size="large">
          {{ getStatusLabel(feedback.status) }}
        </el-tag>
        <el-tag v-if="feedback.priority" :type="getPriorityTagType(feedback.priority)" size="large">
          {{ getPriorityLabel(feedback.priority) }}
        </el-tag>
      </div>
    </div>

    <el-row :gutter="20">
      <el-col :span="16">
        <el-card class="info-card">
          <template #header>
            <div class="card-header">
              <span class="feedback-no">{{ feedback.feedbackNo }}</span>
              <span class="feedback-title">{{ feedback.title }}</span>
            </div>
          </template>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="反馈类型">
              <el-tag size="small">{{ feedback.feedbackTypeName || '-' }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="来源">
              <el-tag size="small" :type="getSourceTagType(feedback.source)">
                {{ getSourceLabel(feedback.source) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="责任科室">{{ feedback.deptName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="处理人">{{ feedback.handlerName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(feedback.createTime) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(feedback.updateTime) }}</el-descriptions-item>
            <el-descriptions-item label="SLA截止时间">
              <span :class="getSlaClass(feedback.slaDeadline)">
                {{ formatDateTime(feedback.slaDeadline) }}
              </span>
            </el-descriptions-item>
            <el-descriptions-item label="是否匿名">
              {{ feedback.isAnonymous === 1 ? '是' : '否' }}
            </el-descriptions-item>
          </el-descriptions>
          <div class="content-section">
            <h4>工单内容</h4>
            <div class="content-text">{{ feedback.content }}</div>
          </div>
        </el-card>

        <el-card v-if="feedback.complainant" class="info-card">
          <template #header>
            <span>投诉人信息</span>
          </template>
          <el-descriptions :column="3" border>
            <el-descriptions-item label="姓名">{{ feedback.complainant.name || '-' }}</el-descriptions-item>
            <el-descriptions-item label="性别">{{ getGenderLabel(feedback.complainant.gender) }}</el-descriptions-item>
            <el-descriptions-item label="年龄">{{ feedback.complainant.age || '-' }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ feedback.complainant.phone || '-' }}</el-descriptions-item>
            <el-descriptions-item label="邮箱">{{ feedback.complainant.email || '-' }}</el-descriptions-item>
            <el-descriptions-item label="患者ID">{{ feedback.complainant.patientId || '-' }}</el-descriptions-item>
            <el-descriptions-item label="地址" :span="3">{{ feedback.complainant.address || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-card>

        <el-card class="info-card">
          <template #header>
            <span>处理流程</span>
          </template>
          <el-timeline v-if="handleRecords.length > 0" :reverse="true">
            <el-timeline-item
              v-for="record in handleRecords"
              :key="record.id"
              :timestamp="formatDateTime(record.createTime)"
              :type="getActionTagType(record.action)"
              :hollow="true"
            >
              <div class="timeline-content">
                <div class="timeline-header">
                  <span class="handler-name">{{ record.handlerName }}</span>
                  <el-tag size="small" :type="getActionTagType(record.action)">
                    {{ getActionLabel(record.action) }}
                  </el-tag>
                  <el-tag v-if="record.result" size="small" type="info">
                    {{ getResultLabel(record.result) }}
                  </el-tag>
                </div>
                <div class="timeline-body">{{ record.content }}</div>
                <div v-if="record.attachments && record.attachments.length > 0" class="timeline-attachments">
                  <el-image
                    v-for="att in record.attachments"
                    :key="att.id"
                    :src="att.filePath"
                    :preview-src-list="record.attachments.map(a => a.filePath)"
                    fit="cover"
                    class="attachment-thumb"
                  />
                </div>
              </div>
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无处理记录" />
        </el-card>

        <el-card v-if="canHandle" class="info-card">
          <template #header>
            <span>处理操作</span>
          </template>
          <el-form :model="handleForm" label-width="100px">
            <el-form-item label="操作类型">
              <el-radio-group v-model="handleForm.action">
                <el-radio label="PROCESS">处理回复</el-radio>
                <el-radio label="TRANSFER">转派</el-radio>
                <el-radio label="CLOSE">关闭</el-radio>
                <el-radio label="REJECT">驳回</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="处理内容" required>
              <el-input
                v-model="handleForm.content"
                type="textarea"
                :rows="4"
                placeholder="请输入处理内容"
                @input="onContentChange"
              />
            </el-form-item>

            <el-form-item v-if="handleForm.action === 'TRANSFER'" label="转派科室">
              <el-tree-select
                v-model="handleForm.transferDept"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'id' }"
                placeholder="请选择转派科室"
                clearable
                style="width: 100%"
                @change="onDeptChange"
              />
            </el-form-item>

            <el-form-item v-if="handleForm.action === 'TRANSFER'" label="转派处理人">
              <el-select
                v-model="handleForm.transferTo"
                placeholder="请选择处理人"
                clearable
                style="width: 100%"
              >
                <el-option
                  v-for="user in deptUsers"
                  :key="user.id"
                  :label="user.realName"
                  :value="user.id"
                />
              </el-select>
            </el-form-item>

            <el-form-item label="处理结果">
              <el-select v-model="handleForm.result" placeholder="请选择" style="width: 100%">
                <el-option label="待处理" value="PENDING" />
                <el-option label="已解决" value="RESOLVED" />
                <el-option label="未解决" value="UNRESOLVED" />
              </el-select>
            </el-form-item>

            <el-form-item label="对投诉人可见">
              <el-switch v-model="handleForm.isVisible" />
            </el-form-item>

            <el-form-item label="附件上传">
              <el-upload
                ref="uploadRef"
                action="/api/upload"
                :headers="{ Authorization: `Bearer ${token}` }"
                :file-list="uploadFileList"
                :on-success="handleUploadSuccess"
                :on-remove="handleUploadRemove"
                :before-upload="beforeUpload"
                multiple
                accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
              >
                <el-button type="primary" plain>
                  <el-icon><Upload /></el-icon>
                  点击上传
                </el-button>
                <template #tip>
                  <div class="upload-tip">支持 JPG、PNG、PDF、DOC 格式，单文件不超过 10MB</div>
                </template>
              </el-upload>
            </el-form-item>

            <el-form-item>
              <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
                提交处理
              </el-button>
            </el-form-item>
          </el-form>

          <div class="auto-save-hint">
            <span v-if="autoSaveStatus === 'saving'" class="saving">
              <el-icon class="is-loading"><Loading /></el-icon>
              保存中...
            </span>
            <span v-else-if="autoSaveStatus === 'saved'" class="saved">
              <el-icon><CircleCheck /></el-icon>
              已自动保存于 {{ lastSavedTime }}
            </span>
            <span v-else-if="autoSaveStatus === 'error'" class="error">
              <el-icon><Warning /></el-icon>
              保存失败
            </span>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <el-card class="action-card">
          <template #header>
            <span>快速操作</span>
          </template>
          <div class="quick-actions">
            <el-button type="primary" plain @click="showAssignDialog = true">
              <el-icon><User /></el-icon>
              指派处理人
            </el-button>
            <el-button type="warning" plain @click="sendReminder">
              <el-icon><Bell /></el-icon>
              发送提醒
            </el-button>
            <el-button type="info" plain @click="printFeedback">
              <el-icon><Printer /></el-icon>
              打印工单
            </el-button>
            <el-button type="success" plain @click="exportPdf">
              <el-icon><Download /></el-icon>
              导出PDF
            </el-button>
          </div>
        </el-card>

        <el-card class="attachment-card">
          <template #header>
            <span>附件列表</span>
          </template>
          <div v-if="attachments.length > 0" class="attachment-list">
            <div v-for="att in attachments" :key="att.id" class="attachment-item">
              <el-image
                v-if="isImage(att.fileType)"
                :src="att.filePath"
                :preview-src-list="attachments.filter(a => isImage(a.fileType)).map(a => a.filePath)"
                fit="cover"
                class="attachment-img"
              />
              <div v-else class="attachment-file">
                <el-icon><Document /></el-icon>
                <span class="file-name">{{ att.fileName }}</span>
              </div>
              <span class="file-size">{{ formatFileSize(att.fileSize) }}</span>
            </div>
          </div>
          <el-empty v-else description="暂无附件" />
        </el-card>

        <el-card v-if="feedback.rating" class="rating-card">
          <template #header>
            <span>工单评价</span>
          </template>
          <div class="rating-content">
            <el-rate v-model="feedback.rating" disabled show-score />
            <p class="rating-comment">{{ feedback.ratingComment || '暂无评价内容' }}</p>
          </div>
        </el-card>

        <el-card class="log-card">
          <template #header>
            <span>操作日志</span>
          </template>
          <el-timeline v-if="operationLogs.length > 0" :reverse="false" size="small">
            <el-timeline-item
              v-for="log in operationLogs"
              :key="log.id"
              :timestamp="formatDateTime(log.createTime)"
              :type="log.type"
              hollow
            >
              {{ log.content }}
            </el-timeline-item>
          </el-timeline>
          <el-empty v-else description="暂无日志" />
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model="showAssignDialog" title="指派处理人" width="500px" append-to-body>
      <el-form :model="assignForm" label-width="100px">
        <el-form-item label="处理人">
          <el-select v-model="assignForm.handlerId" placeholder="请选择处理人" style="width: 100%">
            <el-option label="张三" :value="1" />
            <el-option label="李四" :value="2" />
            <el-option label="王五" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-tree-select
            v-model="assignForm.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id' }"
            placeholder="请选择科室"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAssignDialog = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft,
  Upload,
  User,
  Bell,
  Printer,
  Download,
  Document,
  CircleCheck,
  Warning,
  Loading
} from '@element-plus/icons-vue'
import { getFeedbackDetail, handleFeedback, assignFeedback, type FeedbackDetail } from '@/api/feedback'
import { useUserStore } from '@/store/modules/user'
import { useAutoSave } from '@/composables/useAutoSave'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const token = computed(() => userStore.token)
const feedbackId = computed(() => Number(route.params.id))

const loading = ref(false)
const submitLoading = ref(false)
const feedback = ref<Partial<FeedbackDetail>>({})
const handleRecords = ref<any[]>([])
const attachments = ref<any[]>([])
const operationLogs = ref<any[]>([])
const deptTree = ref<any[]>([])
const deptUsers = ref<any[]>([])

const showAssignDialog = ref(false)
const assignForm = reactive({
  handlerId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  remark: ''
})

const uploadRef = ref()
const uploadFileList = ref<any[]>([])

const handleForm = reactive({
  action: 'PROCESS',
  content: '',
  result: 'PENDING',
  transferTo: undefined as number | undefined,
  transferDept: undefined as number | undefined,
  isVisible: true,
  attachmentIds: [] as number[]
})

const canHandle = computed(() => {
  const status = feedback.value.status
  return ['PENDING', 'PROCESSING'].includes(status || '')
})

const { autoSaveStatus, lastSavedTime, updateContent } = useAutoSave({
  interval: 120000,
  debounce: 2000,
  onSave: async (content: string) => {
    console.log('Auto-saving content:', content.substring(0, 50))
    await new Promise(resolve => setTimeout(resolve, 500))
  }
})

const onContentChange = () => {
  updateContent(handleForm.content)
}

const fetchDetail = async () => {
  if (!feedbackId.value) return

  loading.value = true
  try {
    const res = await getFeedbackDetail(feedbackId.value)
    if (res.code === 200) {
      feedback.value = res.data
      handleRecords.value = res.data.handleRecords || []
      attachments.value = res.data.attachments || []
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取详情失败')
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!handleForm.content.trim()) {
    ElMessage.warning('请输入处理内容')
    return
  }

  if (handleForm.action === 'TRANSFER' && !handleForm.transferTo && !handleForm.transferDept) {
    ElMessage.warning('请选择转派科室或处理人')
    return
  }

  submitLoading.value = true
  try {
    const res = await handleFeedback(feedbackId.value, {
      id: feedbackId.value,
      ...handleForm
    })
    if (res.code === 200) {
      ElMessage.success('处理成功')
      handleForm.content = ''
      fetchDetail()
    } else {
      ElMessage.error(res.message || '处理失败')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '处理失败')
  } finally {
    submitLoading.value = false
  }
}

const confirmAssign = async () => {
  try {
    const res = await assignFeedback(feedbackId.value, assignForm)
    if (res.code === 200) {
      ElMessage.success('指派成功')
      showAssignDialog.value = false
      fetchDetail()
    }
  } catch (error: any) {
    ElMessage.error(error.message || '指派失败')
  }
}

const sendReminder = () => {
  ElMessage.success('提醒已发送')
}

const printFeedback = () => {
  window.print()
}

const exportPdf = () => {
  ElMessage.info('PDF导出功能开发中')
}

const handleUploadSuccess = (response: any, file: any) => {
  if (response.code === 200) {
    handleForm.attachmentIds.push(response.data.id)
    ElMessage.success('上传成功')
  }
}

const handleUploadRemove = (file: any) => {
  const index = handleForm.attachmentIds.indexOf(file.id)
  if (index > -1) {
    handleForm.attachmentIds.splice(index, 1)
  }
}

const beforeUpload = (file: any) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB')
  }
  return isLt10M
}

const onDeptChange = () => {
  deptUsers.value = [
    { id: 1, realName: '张三' },
    { id: 2, realName: '李四' },
    { id: 3, realName: '王五' }
  ]
}

const goBack = () => {
  router.push('/feedback/list')
}

const getSourceTagType = (source?: string) => {
  const map: Record<string, string> = { WECHAT: 'success', PHONE: 'primary', EMAIL: 'warning', ONLINE: 'info', LETTER: '' }
  return map[source || ''] || ''
}

const getSourceLabel = (source?: string) => {
  const map: Record<string, string> = { WECHAT: '微信', PHONE: '电话', EMAIL: '邮件', ONLINE: '在线', LETTER: '信件' }
  return map[source || ''] || source || '-'
}

const getPriorityTagType = (priority?: string) => {
  const map: Record<string, string> = { URGENT: 'danger', HIGH: 'warning', NORMAL: 'primary', LOW: 'info' }
  return map[priority || ''] || ''
}

const getPriorityLabel = (priority?: string) => {
  const map: Record<string, string> = { URGENT: '紧急', HIGH: '高', NORMAL: '普通', LOW: '低' }
  return map[priority || ''] || priority || '-'
}

const getStatusTagType = (status?: string) => {
  const map: Record<string, string> = { DRAFT: 'info', PENDING: 'warning', PROCESSING: 'primary', REPLIED: 'success', CLOSED: 'success', REJECTED: 'danger' }
  return map[status || ''] || ''
}

const getStatusLabel = (status?: string) => {
  const map: Record<string, string> = { DRAFT: '草稿', PENDING: '待处理', PROCESSING: '处理中', REPLIED: '已回复', CLOSED: '已关闭', REJECTED: '已驳回' }
  return map[status || ''] || status || '-'
}

const getGenderLabel = (gender?: string) => {
  const map: Record<string, string> = { M: '男', F: '女' }
  return map[gender || ''] || gender || '-'
}

const getActionTagType = (action?: string) => {
  const map: Record<string, string> = { ASSIGN: 'primary', PROCESS: 'success', REPLY: 'info', TRANSFER: 'warning', CLOSE: 'success', REJECT: 'danger' }
  return map[action || ''] || ''
}

const getActionLabel = (action?: string) => {
  const map: Record<string, string> = { ASSIGN: '已指派', PROCESS: '处理中', REPLY: '已回复', TRANSFER: '已转派', CLOSE: '已关闭', REJECT: '已驳回' }
  return map[action || ''] || action || '-'
}

const getResultLabel = (result?: string) => {
  const map: Record<string, string> = { PENDING: '待处理', RESOLVED: '已解决', UNRESOLVED: '未解决' }
  return map[result || ''] || result || '-'
}

const getSlaClass = (deadline?: string) => {
  if (!deadline) return ''
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const diff = deadlineDate.getTime() - now.getTime()
  if (diff < 0) return 'sla-overdue'
  if (diff < 24 * 60 * 60 * 1000) return 'sla-warning'
  return ''
}

const formatDateTime = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

const formatFileSize = (size?: number) => {
  if (!size) return '-'
  if (size < 1024) return size + 'B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + 'KB'
  return (size / 1024 / 1024).toFixed(1) + 'MB'
}

const isImage = (type?: string) => {
  if (!type) return false
  return ['image/jpeg', 'image/jpg', 'image/png', 'image/gif'].includes(type.toLowerCase())
}

onMounted(() => {
  fetchDetail()
  deptTree.value = [
    { id: 1, deptName: '内科' },
    { id: 2, deptName: '外科' },
    { id: 3, deptName: '儿科' }
  ]
  operationLogs.value = [
    { id: 1, createTime: '2024-01-15 10:30:00', content: '创建工单', type: 'primary' },
    { id: 2, createTime: '2024-01-15 11:00:00', content: '分配给张三处理', type: 'info' }
  ]
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.feedback-detail-container {
  padding: 0;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

  .header-status {
    display: flex;
    gap: 8px;
  }
}

.info-card {
  margin-bottom: 16px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 12px;

    .feedback-no {
      font-weight: 600;
      color: $primary-color;
    }

    .feedback-title {
      font-weight: 500;
      color: $text-primary;
    }
  }
}

.content-section {
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid $border-light;

  h4 {
    margin: 0 0 12px;
    font-size: 14px;
    color: $text-primary;
  }

  .content-text {
    line-height: 1.8;
    color: $text-regular;
    white-space: pre-wrap;
    word-break: break-word;
  }
}

.timeline-content {
  .timeline-header {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 8px;

    .handler-name {
      font-weight: 500;
      color: $text-primary;
    }
  }

  .timeline-body {
    color: $text-regular;
    line-height: 1.6;
    margin-bottom: 8px;
  }

  .timeline-attachments {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;

    .attachment-thumb {
      width: 60px;
      height: 60px;
      border-radius: 4px;
      cursor: pointer;
    }
  }
}

.auto-save-hint {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px dashed $border-light;
  font-size: 12px;
  color: $text-secondary;

  .saving {
    color: $primary-color;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .saved {
    color: $success-color;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .error {
    color: $danger-color;
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.action-card {
  .quick-actions {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .el-button {
      width: 100%;
      justify-content: flex-start;
    }
  }
}

.attachment-card {
  .attachment-list {
    display: flex;
    flex-direction: column;
    gap: 12px;

    .attachment-item {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 8px;
      background: $bg-color;
      border-radius: 4px;

      .attachment-img {
        width: 48px;
        height: 48px;
        border-radius: 4px;
      }

      .attachment-file {
        display: flex;
        align-items: center;
        gap: 8px;
        flex: 1;

        .file-name {
          flex: 1;
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      .file-size {
        color: $text-secondary;
        font-size: 12px;
      }
    }
  }
}

.rating-card {
  .rating-content {
    text-align: center;

    .el-rate {
      margin-bottom: 12px;
    }

    .rating-comment {
      color: $text-regular;
      line-height: 1.6;
    }
  }
}

.log-card {
  :deep(.el-timeline-item__timestamp) {
    font-size: 12px;
  }
}

.upload-tip {
  font-size: 12px;
  color: $text-secondary;
  margin-top: 8px;
}

.sla-overdue {
  color: $danger-color;
  font-weight: 600;
}

.sla-warning {
  color: $warning-color;
  font-weight: 600;
}

:deep(.el-card__header) {
  padding: 12px 20px;
  background: #fafafa;
  font-weight: 600;
}
</style>
