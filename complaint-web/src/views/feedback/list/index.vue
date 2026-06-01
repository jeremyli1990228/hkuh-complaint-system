<template>
  <div class="feedback-list-container">
    <el-card class="search-card">
      <el-form :model="queryForm" inline class="search-form">
        <el-form-item label="工单编号">
          <el-input v-model="queryForm.feedbackNo" placeholder="请输入工单编号" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="queryForm.title" placeholder="请输入标题关键字" clearable style="width: 180px" />
        </el-form-item>
        <el-form-item label="反馈类型">
          <el-select v-model="queryForm.feedbackTypeId" placeholder="请选择" clearable style="width: 150px">
            <el-option v-for="item in feedbackTypes" :key="item.id" :label="item.typeName" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="来源">
          <el-select v-model="queryForm.source" placeholder="请选择" clearable style="width: 120px">
            <el-option label="微信" value="WECHAT" />
            <el-option label="电话" value="PHONE" />
            <el-option label="邮件" value="EMAIL" />
            <el-option label="在线" value="ONLINE" />
            <el-option label="信件" value="LETTER" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-select v-model="queryForm.priority" placeholder="请选择" clearable style="width: 120px">
            <el-option label="紧急" value="URGENT" />
            <el-option label="高" value="HIGH" />
            <el-option label="普通" value="NORMAL" />
            <el-option label="低" value="LOW" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryForm.status" placeholder="请选择" clearable style="width: 130px">
            <el-option label="草稿" value="DRAFT" />
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已关闭" value="CLOSED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item label="责任科室">
          <el-tree-select
            v-model="queryForm.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择科室"
            clearable
            check-strictly
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
            v-model="dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch" :loading="loading">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <div class="table-toolbar">
        <div class="toolbar-left">
          <el-button type="primary" @click="handleCreate">
            <el-icon><Plus /></el-icon>
            新建投诉
          </el-button>
          <el-button @click="handleBatchExport" :disabled="selectedRows.length === 0">
            <el-icon><Download /></el-icon>
            批量导出
          </el-button>
        </div>
        <div class="toolbar-right">
          <el-button @click="handleRefresh" :loading="loading">
            <el-icon><Refresh /></el-icon>
          </el-button>
        </div>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        :stripe="true"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
        @selection-change="handleSelectionChange"
        @row-click="handleRowClick"
        class="feedback-table"
      >
        <el-table-column type="selection" width="55" fixed="left" />
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="feedbackNo" label="工单编号" width="180">
          <template #default="{ row }">
            <el-link type="primary" @click.stop="handleViewDetail(row)">{{ row.feedbackNo }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" show-overflow-tooltip min-width="200" />
        <el-table-column prop="feedbackTypeName" label="反馈类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.feedbackTypeName || '-' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="source" label="来源" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.source" size="small" :type="getSourceTagType(row.source)">
              {{ getSourceLabel(row.source) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.priority" size="small" :type="getPriorityTagType(row.priority)">
              {{ getPriorityLabel(row.priority) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.status" size="small" :type="getStatusTagType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="deptName" label="责任科室" width="120" align="center">
          <template #default="{ row }">
            {{ row.deptName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="handlerName" label="处理人" width="100" align="center">
          <template #default="{ row }">
            {{ row.handlerName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="170" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="slaDeadline" label="SLA" width="100" align="center">
          <template #default="{ row }">
            <span v-if="row.slaDeadline" :class="getSlaClass(row.slaDeadline)">
              {{ getSlaText(row.slaDeadline) }}
            </span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link @click.stop="handleViewDetail(row)">详情</el-button>
            <el-button
              v-if="['PENDING', 'PROCESSING'].includes(row.status)"
              type="primary"
              link
              @click.stop="handleProcess(row)"
            >
              处理
            </el-button>
            <el-button
              v-if="hasPermission('feedback:assign')"
              type="primary"
              link
              @click.stop="handleAssign(row)"
            >
              指派
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <el-dialog v-model="exportDialogVisible" title="导出确认" width="400px" append-to-body>
      <div class="export-dialog-content">
        <p>确定要导出选中的 {{ selectedRows.length }} 条记录吗？</p>
        <el-form :model="exportForm" label-width="80px">
          <el-form-item label="导出格式">
            <el-radio-group v-model="exportForm.exportType">
              <el-radio label="EXCEL">Excel</el-radio>
              <el-radio label="PDF">PDF</el-radio>
            </el-radio-group>
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="exportDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmExport" :loading="exporting">确定导出</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignDialogVisible" title="指派处理人" width="500px" append-to-body>
      <el-form ref="assignFormRef" :model="assignForm" :rules="assignRules" label-width="100px">
        <el-form-item label="工单编号">
          <span>{{ currentRow?.feedbackNo }}</span>
        </el-form-item>
        <el-form-item label="工单标题">
          <span>{{ currentRow?.title }}</span>
        </el-form-item>
        <el-form-item label="处理人" prop="handlerId">
          <el-select v-model="assignForm.handlerId" placeholder="请选择处理人" clearable style="width: 100%">
            <el-option label="张三" :value="1" />
            <el-option label="李四" :value="2" />
            <el-option label="王五" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室" prop="deptId">
          <el-tree-select
            v-model="assignForm.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'id', children: 'children' }"
            placeholder="请选择科室"
            clearable
            check-strictly
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="assignForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmAssign" :loading="assignLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Search, RefreshLeft, Plus, Download, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import {
  getFeedbackList,
  getFeedbackTypes,
  getDepts,
  assignFeedback,
  exportFeedback,
  type FeedbackListItem,
  type FeedbackQuery,
  type FeedbackType,
  type Dept
} from '@/api/feedback'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const tableData = ref<FeedbackListItem[]>([])
const total = ref(0)
const selectedRows = ref<FeedbackListItem[]>([])

const dateRange = ref<[string, string] | null>(null)

const queryForm = reactive<FeedbackQuery>({
  pageNum: 1,
  pageSize: 10,
  feedbackNo: '',
  title: '',
  feedbackTypeId: undefined,
  source: '',
  priority: '',
  status: '',
  deptId: undefined,
  handlerId: undefined,
  startDate: '',
  endDate: '',
  keyword: ''
})

const feedbackTypes = ref<FeedbackType[]>([])
const deptTree = ref<Dept[]>([])

const exportDialogVisible = ref(false)
const exportForm = reactive({
  exportType: 'EXCEL'
})
const exporting = ref(false)

const assignDialogVisible = ref(false)
const assignLoading = ref(false)
const currentRow = ref<FeedbackListItem | null>(null)
const assignFormRef = ref<FormInstance>()
const assignForm = reactive({
  handlerId: undefined as number | undefined,
  deptId: undefined as number | undefined,
  remark: ''
})

const assignRules: FormRules = {
  handlerId: [{ required: true, message: '请选择处理人', trigger: 'change' }],
  deptId: [{ required: true, message: '请选择科室', trigger: 'change' }]
}

const hasPermission = (permission: string) => {
  return userStore.hasPermission(permission)
}

const getSourceTagType = (source: string) => {
  const map: Record<string, string> = {
    WECHAT: 'success',
    PHONE: 'primary',
    EMAIL: 'warning',
    ONLINE: 'info',
    LETTER: ''
  }
  return map[source] || ''
}

const getSourceLabel = (source: string) => {
  const map: Record<string, string> = {
    WECHAT: '微信',
    PHONE: '电话',
    EMAIL: '邮件',
    ONLINE: '在线',
    LETTER: '信件'
  }
  return map[source] || source
}

const getPriorityTagType = (priority: string) => {
  const map: Record<string, string> = {
    URGENT: 'danger',
    HIGH: 'warning',
    NORMAL: 'primary',
    LOW: 'info'
  }
  return map[priority] || ''
}

const getPriorityLabel = (priority: string) => {
  const map: Record<string, string> = {
    URGENT: '紧急',
    HIGH: '高',
    NORMAL: '普通',
    LOW: '低'
  }
  return map[priority] || priority
}

const getStatusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    PENDING: 'warning',
    PROCESSING: 'primary',
    REPLIED: 'success',
    CLOSED: 'success',
    REJECTED: 'danger'
  }
  return map[status] || ''
}

const getStatusLabel = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: '草稿',
    PENDING: '待处理',
    PROCESSING: '处理中',
    REPLIED: '已回复',
    CLOSED: '已关闭',
    REJECTED: '已驳回'
  }
  return map[status] || status
}

const formatDateTime = (dateStr: string | undefined) => {
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

const getSlaClass = (deadline: string) => {
  if (!deadline) return ''
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const diff = deadlineDate.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (diff < 0) return 'sla-overdue'
  if (days <= 1) return 'sla-warning'
  return 'sla-normal'
}

const getSlaText = (deadline: string) => {
  if (!deadline) return '-'
  const now = new Date()
  const deadlineDate = new Date(deadline)
  const diff = deadlineDate.getTime() - now.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))

  if (diff < 0) return '已超期'
  if (days === 0) return '今日到期'
  return `剩余${days}天`
}

const fetchList = async () => {
  loading.value = true
  try {
    if (dateRange.value) {
      queryForm.startDate = dateRange.value[0]
      queryForm.endDate = dateRange.value[1]
    } else {
      queryForm.startDate = ''
      queryForm.endDate = ''
    }

    const res = await getFeedbackList(queryForm)
    if (res.code === 200) {
      tableData.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取列表失败')
  } finally {
    loading.value = false
  }
}

const fetchFeedbackTypes = async () => {
  try {
    const res = await getFeedbackTypes()
    if (res.code === 200) {
      feedbackTypes.value = res.data || []
    }
  } catch (error) {
    console.error('获取反馈类型失败', error)
  }
}

const fetchDepts = async () => {
  try {
    const res = await getDepts()
    if (res.code === 200) {
      deptTree.value = res.data || []
    }
  } catch (error) {
    console.error('获取科室失败', error)
    deptTree.value = [
      { id: 1, deptName: '内科' },
      { id: 2, deptName: '外科' },
      { id: 3, deptName: '儿科' },
      { id: 4, deptName: '妇产科' },
      { id: 5, deptName: '急诊科' }
    ]
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  fetchList()
}

const handleReset = () => {
  Object.keys(queryForm).forEach(key => {
    if (key === 'pageNum') queryForm.pageNum = 1
    else if (key === 'pageSize') queryForm.pageSize = 10
    else (queryForm as any)[key] = ''
  })
  dateRange.value = null
  handleSearch()
}

const handleRefresh = () => {
  fetchList()
}

const handleSizeChange = (val: number) => {
  queryForm.pageSize = val
  queryForm.pageNum = 1
  fetchList()
}

const handlePageChange = (val: number) => {
  queryForm.pageNum = val
  fetchList()
}

const handleSelectionChange = (rows: FeedbackListItem[]) => {
  selectedRows.value = rows
}

const handleRowClick = (row: FeedbackListItem) => {
  console.log('Row clicked:', row)
}

const handleCreate = () => {
  router.push('/feedback/create')
}

const handleViewDetail = (row: FeedbackListItem) => {
  router.push(`/feedback/detail/${row.id}`)
}

const handleProcess = (row: FeedbackListItem) => {
  router.push(`/feedback/detail/${row.id}?action=process`)
}

const handleAssign = (row: FeedbackListItem) => {
  currentRow.value = row
  assignForm.handlerId = undefined
  assignForm.deptId = undefined
  assignForm.remark = ''
  assignDialogVisible.value = true
}

const confirmAssign = async () => {
  if (!assignFormRef.value) return

  await assignFormRef.value.validate(async (valid) => {
    if (!valid) return

    assignLoading.value = true
    try {
      const res = await assignFeedback(currentRow.value!.id, assignForm)
      if (res.code === 200) {
        ElMessage.success('指派成功')
        assignDialogVisible.value = false
        fetchList()
      } else {
        ElMessage.error(res.message || '指派失败')
      }
    } catch (error: any) {
      ElMessage.error(error.message || '指派失败')
    } finally {
      assignLoading.value = false
    }
  })
}

const handleBatchExport = () => {
  exportDialogVisible.value = true
}

const confirmExport = async () => {
  exporting.value = true
  try {
    const params = { ...queryForm, exportType: exportForm.exportType }
    const res = await exportFeedback(params)

    const blob = new Blob([res as any], {
      type: exportForm.exportType === 'EXCEL'
        ? 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
        : 'application/pdf'
    })

    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `反馈列表_${Date.now()}.${exportForm.exportType === 'EXCEL' ? 'xlsx' : 'pdf'}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)

    ElMessage.success('导出成功')
    exportDialogVisible.value = false
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  } finally {
    exporting.value = false
  }
}

onMounted(() => {
  fetchList()
  fetchFeedbackTypes()
  fetchDepts()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.feedback-list-container {
  padding: 0;
}

.search-card {
  margin-bottom: 16px;

  :deep(.el-card__body) {
    padding-bottom: 0;
  }
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
    margin-right: 0;
  }
}

.table-card {
  :deep(.el-card__body) {
    padding: 0;
  }
}

.table-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid $border-light;

  .toolbar-left {
    display: flex;
    gap: 12px;
  }

  .toolbar-right {
    display: flex;
    gap: 8px;
  }
}

.feedback-table {
  :deep(.el-table__body) {
    .el-table__row {
      cursor: pointer;

      &:hover {
        background-color: #f5f7fa;
      }
    }
  }

  .sla-overdue {
    color: $danger-color;
    font-weight: 600;
  }

  .sla-warning {
    color: $warning-color;
    font-weight: 600;
  }

  .sla-normal {
    color: $success-color;
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  padding: 16px 20px;
  border-top: 1px solid $border-light;
}

.export-dialog-content {
  p {
    margin-bottom: 16px;
    color: $text-primary;
  }
}

:deep(.el-link) {
  & + & {
    margin-left: 8px;
  }
}
</style>
