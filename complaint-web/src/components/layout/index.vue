<template>
  <div class="layout-container">
    <el-container>
      <el-aside width="200px">
        <div class="logo">港大医院</div>
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409eff"
        >
          <el-menu-item index="/dashboard">
            <el-icon><Odometer /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/feedback">
            <el-icon><Document /></el-icon>
            <span>反馈管理</span>
          </el-menu-item>
          <el-menu-item index="/satisfaction">
            <el-icon><TrendCharts /></el-icon>
            <span>满意度</span>
          </el-menu-item>
          <el-menu-item index="/system">
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-container>
        <el-header>
          <div class="header-content">
            <span class="title">港大医院投诉管理系统</span>
            <div class="user-info">
              <el-dropdown>
                <span class="el-dropdown-link">
                  <el-avatar :size="32" icon="UserFilled" />
                  <span>管理员</span>
                </span>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item>个人中心</el-dropdown-item>
                    <el-dropdown-item>退出登录</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>
        </el-header>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const activeMenu = computed(() => route.path)
</script>

<style scoped lang="scss">
.layout-container {
  height: 100%;
}

.el-container {
  height: 100%;
}

.el-aside {
  background-color: #304156;
  color: #fff;
  height: 100%;
}

.logo {
  height: 60px;
  line-height: 60px;
  text-align: center;
  font-size: 18px;
  font-weight: bold;
  color: #fff;
  border-bottom: 1px solid #1f2d3d;
}

.el-header {
  background-color: #fff;
  border-bottom: 1px solid #e4e7ed;
  padding: 0 20px;
  display: flex;
  align-items: center;
}

.header-content {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title {
  font-size: 18px;
  font-weight: 500;
  color: #303133;
}

.user-info {
  .el-dropdown-link {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
  }
}

.el-main {
  background-color: #f5f7fa;
  padding: 20px;
}
</style>
