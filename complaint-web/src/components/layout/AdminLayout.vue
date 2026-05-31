<template>
  <el-container class="admin-layout">
    <el-aside :width="sidebarWidth" class="layout-aside">
      <AppSidebar :is-collapse="sidebarCollapsed" />
    </el-aside>

    <el-container class="layout-container">
      <el-header height="60px" class="layout-header">
        <AppHeader />
      </el-header>

      <el-main class="layout-main">
        <div class="main-content">
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <keep-alive :include="cachedViews">
                <component :is="Component" :key="route.path" />
              </keep-alive>
            </transition>
          </router-view>
        </div>
      </el-main>

      <el-footer height="40px" class="layout-footer">
        <AppFooter />
      </el-footer>
    </el-container>
  </el-container>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import AppHeader from './AppHeader.vue'
import AppSidebar from './AppSidebar.vue'
import AppFooter from './AppFooter.vue'

const route = useRoute()
const appStore = useAppStore()

const sidebarCollapsed = computed(() => !appStore.sidebar.opened)

const sidebarWidth = computed(() => {
  return sidebarCollapsed.value ? '64px' : '240px'
})

const cachedViews = computed(() => {
  const views: string[] = []
  route.matched.forEach(match => {
    if (match.name && !match.meta.noCache) {
      views.push(match.name as string)
    }
  })
  return views
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.admin-layout {
  height: 100vh;
  width: 100%;
  overflow: hidden;
}

.layout-aside {
  height: 100vh;
  background-color: #304156;
  transition: width 0.3s ease;
  overflow: hidden;
  flex-shrink: 0;
}

.layout-container {
  height: 100vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.layout-header {
  padding: 0;
  background-color: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  flex-shrink: 0;
}

.layout-main {
  flex: 1;
  padding: 0;
  background-color: #f5f7fa;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.main-content {
  flex: 1;
  padding: 16px 20px;
  overflow-y: auto;
  overflow-x: hidden;
}

.layout-footer {
  padding: 0;
  background-color: #fff;
  flex-shrink: 0;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s ease;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style>
