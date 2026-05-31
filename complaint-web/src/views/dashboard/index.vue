<template>
  <div class="dashboard-container">
    <h1>首页 - 数据概览</h1>
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">128</div>
            <div class="stat-label">待处理反馈</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">456</div>
            <div class="stat-label">已处理反馈</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">95.8%</div>
            <div class="stat-label">满意度</div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover">
          <div class="stat-item">
            <div class="stat-value">24h</div>
            <div class="stat-label">平均响应时间</div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped lang="scss">
.dashboard-container {
  h1 {
    margin-bottom: 20px;
    color: #303133;
  }
}

.stat-item {
  text-align: center;
  padding: 20px 0;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 14px;
  color: #606266;
}
</style>
