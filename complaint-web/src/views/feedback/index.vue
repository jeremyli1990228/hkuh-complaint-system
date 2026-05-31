<template>
  <div class="feedback-container">
    <h1>反馈管理</h1>
    <el-card>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" />
        <el-table-column prop="type" label="类型" />
        <el-table-column prop="status" label="状态" />
        <el-table-column prop="createTime" label="创建时间" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button type="primary" size="small">查看</el-button>
            <el-button type="success" size="small">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const tableData = ref([
  {
    id: 1,
    title: '服务态度问题',
    type: '投诉',
    status: '待处理',
    createTime: '2024-01-15 10:30:00'
  }
])
</script>

<style scoped lang="scss">
.feedback-container {
  h1 {
    margin-bottom: 20px;
    color: #303133;
  }
}
</style>
