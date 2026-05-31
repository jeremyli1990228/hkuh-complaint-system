<template>
  <div class="app-breadcrumb">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">
        <el-icon><HomeFilled /></el-icon>
        <span>首页</span>
      </el-breadcrumb-item>
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path">
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

interface BreadcrumbItem {
  title: string
  path?: string
}

const route = useRoute()

const breadcrumbs = computed<BreadcrumbItem[]>(() => {
  const matched = route.matched.filter(item => item.meta && item.meta.title && !item.meta.hidden)

  return matched.map(item => ({
    title: item.meta.title as string,
    path: item.path
  }))
})
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.app-breadcrumb {
  display: flex;
  align-items: center;

  :deep(.el-breadcrumb) {
    font-size: 14px;

    .el-breadcrumb__inner {
      display: flex;
      align-items: center;
      gap: 4px;
      color: $text-secondary;
      transition: color 0.3s;

      &:hover {
        color: $primary-color;
      }

      &.is-link {
        color: $text-primary;
        font-weight: 500;
      }
    }

    .el-breadcrumb__separator {
      color: $text-placeholder;
    }
  }

  :deep(.el-breadcrumb__item:last-child .el-breadcrumb__inner) {
    color: $text-primary;
    font-weight: 500;
  }
}
</style>
