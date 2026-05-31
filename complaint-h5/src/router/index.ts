import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
  },
  {
    path: '/submit',
    name: 'Submit',
    component: () => import('@/views/submit/index.vue'),
  },
  {
    path: '/my-list',
    name: 'MyList',
    component: () => import('@/views/my-list/index.vue'),
  },
  {
    path: '/detail/:id',
    name: 'Detail',
    component: () => import('@/views/detail/index.vue'),
  },
  {
    path: '/faq',
    name: 'FAQ',
    component: () => import('@/views/faq/index.vue'),
  },
  {
    path: '/survey',
    name: 'Survey',
    component: () => import('@/views/survey/index.vue'),
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
