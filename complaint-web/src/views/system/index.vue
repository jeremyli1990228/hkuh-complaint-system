<template>
  <div class="system-container">
    <h1>系统管理</h1>
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="用户管理" name="user">
          <div style="padding: 20px;">用户管理功能区域</div>
        </el-tab-pane>
        <el-tab-pane label="角色管理" name="role">
          <div style="padding: 20px;">角色管理功能区域</div>
        </el-tab-pane>
        <el-tab-pane label="菜单管理" name="menu">
          <div style="padding: 20px;">菜单管理功能区域</div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const activeTab = ref('user')
</script>

<style scoped lang="scss">
.system-container {
  h1 {
    margin-bottom: 20px;
    color: #303133;
  }
}
</style>
