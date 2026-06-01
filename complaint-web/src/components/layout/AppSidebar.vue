<template>
  <div class="app-sidebar" :class="{ 'is-collapse': isCollapse }">
    <div class="sidebar-logo" :class="{ 'logo-collapse': isCollapse }">
      <img v-if="!isCollapse" src="@/assets/images/logo.png" alt="Logo" class="logo-img" @error="handleLogoError" />
      <span v-if="!isCollapse" class="logo-text">投诉管理</span>
      <span v-else class="logo-collapse-text">投</span>
    </div>

    <el-scrollbar class="sidebar-scrollbar">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in menus" :key="menu.path">
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.path">
            <template #title>
              <el-icon>
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.title }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.path"
              :index="resolvePath(menu.path, child.path)"
              :hidden="child.hidden"
            >
              <el-icon>
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.title }}</span>
            </el-menu-item>
          </el-sub-menu>

          <el-menu-item v-else :index="menu.path" :hidden="menu.hidden">
            <el-icon>
              <component :is="menu.icon" />
            </el-icon>
            <template #title>
              <span>{{ menu.title }}</span>
            </template>
          </el-menu-item>
        </template>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/modules/app'
import {
  Odometer,
  Document,
  List,
  DataAnalysis,
  TrendCharts,
  Setting,
  User,
  Key,
  Grid,
  OfficeBuilding,
  DocumentCopy,
  Monitor
} from '@element-plus/icons-vue'

interface MenuItem {
  path: string
  title: string
  icon: string
  hidden?: boolean
  children?: MenuItem[]
}

interface Props {
  isCollapse?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  isCollapse: false
})

const route = useRoute()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)

const menus: MenuItem[] = [
  {
    path: '/dashboard',
    title: '工作台',
    icon: 'Odometer'
  },
  {
    path: '/feedback',
    title: '投诉管理',
    icon: 'Document',
    children: [
      { path: 'list', title: '工单列表', icon: 'List', hidden: false },
      { path: 'statistics', title: '统计分析', icon: 'DataAnalysis', hidden: false }
    ]
  },
  {
    path: '/satisfaction',
    title: '满意度管理',
    icon: 'TrendCharts',
    children: [
      { path: 'survey', title: '调查管理', icon: 'DocumentCopy', hidden: false },
      { path: 'dashboard', title: '驾驶舱', icon: 'Monitor', hidden: false }
    ]
  },
  {
    path: '/system',
    title: '系统管理',
    icon: 'Setting',
    children: [
      { path: 'user', title: '用户管理', icon: 'User', hidden: false },
      { path: 'role', title: '角色管理', icon: 'Key', hidden: false },
      { path: 'menu', title: '菜单管理', icon: 'Menu', hidden: true },
      { path: 'dept', title: '科室管理', icon: 'OfficeBuilding', hidden: false },
      { path: 'dict', title: '字典管理', icon: 'DocumentCopy', hidden: false },
      { path: 'log', title: '日志管理', icon: 'Monitor', hidden: false }
    ]
  }
]

const iconMap: Record<string, any> = {
  Odometer,
  Document,
  List,
  DataAnalysis,
  TrendCharts,
  Setting,
  User,
  Key,
  Grid,
  OfficeBuilding,
  DocumentCopy,
  Monitor
}

const resolvePath = (parentPath: string, childPath: string): string => {
  if (childPath.startsWith('/')) {
    return childPath
  }
  return `/${parentPath}/${childPath}`.replace(/\/+/g, '/')
}

const handleLogoError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.style.display = 'none'
}
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.app-sidebar {
  width: 240px;
  height: 100%;
  background-color: #304156;
  transition: width 0.3s ease;
  display: flex;
  flex-direction: column;
  overflow: hidden;

  &.is-collapse {
    width: 64px;
  }
}

.sidebar-logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  background: #2b3a4a;
  border-bottom: 1px solid #1f2d3d;
  transition: all 0.3s ease;

  &.logo-collapse {
    padding: 0;
  }

  .logo-img {
    width: 32px;
    height: 32px;
    border-radius: 50%;
    object-fit: cover;
    flex-shrink: 0;
  }

  .logo-text {
    font-size: 16px;
    font-weight: 600;
    color: #fff;
    white-space: nowrap;
    overflow: hidden;
  }

  .logo-collapse-text {
    font-size: 20px;
    font-weight: 700;
    color: #fff;
  }
}

.sidebar-scrollbar {
  flex: 1;
  height: calc(100% - 60px);

  :deep(.el-scrollbar__wrap) {
    overflow-x: hidden;
  }

  :deep(.el-scrollbar__bar.is-vertical) {
    width: 6px;
  }

  :deep(.el-scrollbar__bar.is-horizontal) {
    height: 6px;
  }
}

.sidebar-menu {
  border-right: none;

  &:not(.el-menu--collapse) {
    width: 240px;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    height: 50px;
    line-height: 50px;
    transition: all 0.3s ease;

    &:hover {
      background-color: #263445 !important;
    }

    .el-icon {
      font-size: 18px;
      margin-right: 10px;
      flex-shrink: 0;
    }

    span {
      white-space: nowrap;
    }
  }

  :deep(.el-menu-item.is-active) {
    background-color: $primary-color !important;
    color: #fff !important;

    &::before {
      content: '';
      position: absolute;
      left: 0;
      top: 0;
      bottom: 0;
      width: 3px;
      background: #fff;
    }
  }

  :deep(.el-sub-menu) {
    .el-menu-item {
      padding-left: 52px !important;
      height: 46px;
      line-height: 46px;
      font-size: 14px;
    }

    .el-sub-menu__title {
      &:hover {
        background-color: #263445 !important;
      }
    }

    &.is-active > .el-sub-menu__title {
      color: #409eff !important;
    }
  }

  :deep(.el-menu--inline) {
    background-color: #1f2d3d;
  }

  :deep(.el-sub-menu__icon-arrow) {
    right: 15px;
    transition: transform 0.3s;
  }
}

.is-collapse {
  .sidebar-logo {
    padding: 0;
    justify-content: center;
  }

  :deep(.el-menu-item),
  :deep(.el-sub-menu__title) {
    padding-left: 0 !important;
    padding-right: 0 !important;
    justify-content: center;

    .el-icon {
      margin-right: 0;
    }

    span {
      display: none;
    }
  }

  :deep(.el-sub-menu__title) {
    .el-sub-menu__icon-arrow {
      display: none;
    }
  }

  :deep(.el-menu-item) {
    padding-left: 0 !important;
  }
}
</style>
