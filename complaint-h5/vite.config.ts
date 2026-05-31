import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import AutoImport from 'unplugin-auto-import/vite'
import Components from 'unplugin-vue-components/vite'
import { VantResolver } from '@vant/auto-import-resolver'
import path from 'path'
import pxtovw from 'postcss-px-to-viewport'

// 配置px转vw
const pxtovwConfig = {
  viewportWidth: 375, // 视口宽度，对应设计稿宽度
  unitPrecision: 3, // 指定px转换为视口单位值的小数位数
  viewportUnit: 'vw', // 指定需要转换成的视口单位，建议使用vw
  selectorBlackList: ['.ignore', '.hairlines'], // 指定不转换为视口单位的类
  minPixelValue: 1, // 小于或等于1px不转换
  mediaQuery: false, // 允许在媒体查询中转换px
}

export default defineConfig({
  plugins: [
    vue(),
    AutoImport({
      imports: ['vue', 'vue-router', 'pinia'],
      resolvers: [VantResolver()],
    }),
    Components({
      resolvers: [VantResolver()],
    }),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
  css: {
    postcss: {
      plugins: [pxtovw(pxtovwConfig)],
    },
  },
})
