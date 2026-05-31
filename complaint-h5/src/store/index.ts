import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    userInfo: null,
    token: '',
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
  },
  actions: {
    setUserInfo(info: any) {
      this.userInfo = info
    },
    setToken(token: string) {
      this.token = token
    },
    logout() {
      this.userInfo = null
      this.token = ''
    },
  },
})
