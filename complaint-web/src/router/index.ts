import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true }
  },
  {
    path: '/',
    name: 'Layout',
    redirect: '/dashboard',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' }
      },
      {
        path: 'feedback',
        name: 'Feedback',
        redirect: '/feedback/list',
        meta: { title: '投诉管理', icon: 'Document' },
        children: [
          {
            path: 'list',
            name: 'FeedbackList',
            component: () => import('@/views/feedback/list/index.vue'),
            meta: { title: '工单列表', icon: 'List' }
          },
          {
            path: 'detail/:id',
            name: 'FeedbackDetail',
            component: () => import('@/views/feedback/detail/index.vue'),
            meta: { title: '工单详情', icon: 'View', hidden: true }
          },
          {
            path: 'create',
            name: 'FeedbackCreate',
            component: () => import('@/views/feedback/index.vue'),
            meta: { title: '创建工单', icon: 'Plus', hidden: true }
          },
          {
            path: 'statistics',
            name: 'FeedbackStatistics',
            component: () => import('@/views/dashboard/index.vue'),
            meta: { title: '统计分析', icon: 'DataAnalysis' }
          }
        ]
      },
      {
        path: 'satisfaction',
        name: 'Satisfaction',
        redirect: '/satisfaction/survey',
        meta: { title: '满意度管理', icon: 'TrendCharts' },
        children: [
          {
            path: 'survey',
            name: 'SurveyList',
            component: () => import('@/views/satisfaction/index.vue'),
            meta: { title: '调查管理', icon: 'DocumentCopy' }
          },
          {
            path: 'dashboard',
            name: 'SatisfactionDashboard',
            component: () => import('@/views/satisfaction/dashboard/index.vue'),
            meta: { title: '驾驶舱', icon: 'Monitor' }
          }
        ]
      },
      {
        path: 'system',
        name: 'System',
        redirect: '/system/user',
        meta: { title: '系统管理', icon: 'Setting' },
        children: [
          {
            path: 'user',
            name: 'UserManagement',
            component: () => import('@/views/system/index.vue'),
            meta: { title: '用户管理', icon: 'User' }
          },
          {
            path: 'role',
            name: 'RoleManagement',
            component: () => import('@/views/system/index.vue'),
            meta: { title: '角色管理', icon: 'Key' }
          },
          {
            path: 'dept',
            name: 'DeptManagement',
            component: () => import('@/views/system/index.vue'),
            meta: { title: '科室管理', icon: 'OfficeBuilding' }
          },
          {
            path: 'dict',
            name: 'DictManagement',
            component: () => import('@/views/system/index.vue'),
            meta: { title: '字典管理', icon: 'DocumentCopy' }
          },
          {
            path: 'log',
            name: 'LogManagement',
            component: () => import('@/views/system/index.vue'),
            meta: { title: '日志管理', icon: 'Monitor' }
          }
        ]
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '个人中心', icon: 'User', hidden: true }
      },
      {
        path: '404',
        name: 'NotFound',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '404', hidden: true }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404',
    meta: { hidden: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  if (to.meta.requiresAuth !== false && !userStore.isLoggedIn) {
    if (to.path !== '/login') {
      next({ path: '/login', query: { redirect: to.fullPath } })
      return
    }
  }

  if (to.path === '/login' && userStore.isLoggedIn) {
    next({ path: '/' })
    return
  }

  next()
})

export default router
