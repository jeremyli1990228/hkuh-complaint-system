import { defineStore } from 'pinia'
import { UserInfo } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('access_token') || '',
    refreshToken: localStorage.getItem('refresh_token') || '',
    userInfo: JSON.parse(localStorage.getItem('user_info') || 'null') as UserInfo | null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    roles: (state) => state.userInfo?.roles || [],
    permissions: (state) => state.userInfo?.permissions || [],
    hasPermission: (state) => (permission: string) => {
      const permissions = state.userInfo?.permissions || []
      return permissions.includes('*:*:*') || permissions.includes(permission)
    }
  },
  actions: {
    setToken(accessToken: string, refreshToken?: string) {
      this.token = accessToken
      if (accessToken) {
        localStorage.setItem('access_token', accessToken)
      }
      if (refreshToken) {
        this.refreshToken = refreshToken
        localStorage.setItem('refresh_token', refreshToken)
      }
    },
    setUserInfo(info: UserInfo) {
      this.userInfo = info
      if (info) {
        localStorage.setItem('user_info', JSON.stringify(info))
      } else {
        localStorage.removeItem('user_info')
      }
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      localStorage.removeItem('access_token')
      localStorage.removeItem('refresh_token')
      localStorage.removeItem('user_info')
    },
    resetState() {
      this.logout()
    }
  }
})
