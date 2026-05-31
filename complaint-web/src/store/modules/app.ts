import { defineStore } from 'pinia'

export const useAppStore = defineStore('app', {
  state: () => ({
    sidebar: {
      opened: true,
      withoutAnimation: false
    },
    device: 'desktop',
    size: 'default',
    language: 'zh-CN'
  }),
  getters: {
    isSidebarOpened: (state) => state.sidebar.opened,
    isMobile: (state) => state.device === 'mobile',
    isDesktop: (state) => state.device === 'desktop'
  },
  actions: {
    toggleSidebar() {
      this.sidebar.opened = !this.sidebar.opened
      this.sidebar.withoutAnimation = false
    },
    closeSidebar(withoutAnimation: boolean = false) {
      this.sidebar.opened = false
      this.sidebar.withoutAnimation = withoutAnimation
    },
    toggleDevice(device: 'mobile' | 'desktop') {
      this.device = device
    },
    setSize(size: 'large' | 'medium' | 'small' | 'default') {
      this.size = size
    },
    setLanguage(language: string) {
      this.language = language
    }
  }
})
