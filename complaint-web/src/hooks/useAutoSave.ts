import { ref, watch, onUnmounted, type Ref } from 'vue'

/**
 * @fileoverview 自动保存组合式函数
 * @description 提供自动保存功能，支持定时保存、数据变化防抖保存、手动保存等多种模式
 * @author HKU Hospital Development Team
 * @version 1.0.0
 */

/**
 * 自动保存选项配置
 * @interface UseAutoSaveOptions
 */
export interface UseAutoSaveOptions {
  /**
   * 保存函数
   * @description 执行实际保存操作的异步函数，返回 boolean 表示是否保存成功
   * @returns Promise<boolean> 保存成功返回 true，失败返回 false
   * @example
   * ```typescript
   * saveFn: async () => {
   *   const res = await api.updateDraft(draftData)
   *   return res.code === 200
   * }
   * ```
   */
  saveFn: () => Promise<boolean>

  /**
   * 自动保存间隔时间
   * @description 定时自动保存的间隔（毫秒），默认 120000ms（2分钟）
   * @default 120000
   */
  interval?: number

  /**
   * 防抖等待时间
   * @description 数据变化后等待多久才触发保存（毫秒），默认 3000ms（3秒）
   * @default 3000
   */
  debounceWait?: number

  /**
   * 是否启用自动保存
   * @description 可以是静态布尔值或响应式 Ref，当为 false 时暂停自动保存
   * @default true
   */
  enabled?: Ref<boolean> | boolean

  /**
   * 监听的数据源
   * @description 当此数据源发生变化时，会在 debounceWait 延迟后触发保存
   * @example
   * ```typescript
   * watchSource: toRef(form, 'content')
   * ```
   */
  watchSource?: Ref<any>

  /**
   * 错误重试次数
   * @description 保存失败时的最大重试次数，默认 3 次
   * @default 3
   */
  maxRetries?: number

  /**
   * 重试间隔时间
   * @description 每次重试之间的间隔（毫秒），默认 5000ms（5秒）
   * @default 5000
   */
  retryInterval?: number

  /**
   * 保存成功回调
   * @description 保存成功时调用的回调函数
   */
  onSuccess?: () => void

  /**
   * 保存失败回调
   * @description 保存失败时调用的回调函数
   */
  onError?: (error: Error, retryCount: number) => void

  /**
   * 是否在控制台输出日志
   * @description 启用后会在控制台输出保存相关的调试信息
   * @default false (生产环境) / true (开发环境)
   */
  debug?: boolean
}

/**
 * 自动保存返回值
 * @interface UseAutoSaveReturn
 */
export interface UseAutoSaveReturn {
  /**
   * 是否正在保存
   * @description 保存操作执行期间为 true，可用于显示保存状态指示器
   */
  isSaving: Ref<boolean>

  /**
   * 最后保存时间
   * @description 最近一次成功保存的时间戳，初始为 null
   */
  lastSavedTime: Ref<Date | null>

  /**
   * 累计保存次数
   * @description 自组件挂载以来的成功保存次数统计
   */
  saveCount: Ref<number>

  /**
   * 累计失败次数
   * @description 自组件挂载以来的保存失败次数统计
   */
  errorCount: Ref<number>

  /**
   * 当前重试次数
   * @description 当前正在进行的重试次数，保存成功或未开始时为 0
   */
  currentRetryCount: Ref<number>

  /**
   * 保存状态
   * @description 'idle' | 'saving' | 'saved' | 'error' | 'disabled'
   */
  status: Ref<'idle' | 'saving' | 'saved' | 'error' | 'disabled'>

  /**
   * 手动立即保存
   * @description 立即执行保存操作，忽略防抖和保存状态锁定
   * @returns Promise<void>
   */
  saveNow: () => Promise<void>

  /**
   * 重置自动保存状态
   * @description 重置保存计数、错误计数和最后保存时间，但保持定时器运行
   */
  resetAutoSave: () => void

  /**
   * 销毁自动保存
   * @description 停止所有定时器、移除所有监听器，组件卸载时自动调用
   */
  destroyAutoSave: () => void

  /**
   * 暂停自动保存
   * @description 暂停定时保存和监听保存，状态变为 'disabled'
   */
  pause: () => void

  /**
   * 恢复自动保存
   * @description 恢复定时保存和监听保存
   */
  resume: () => void

  /**
   * 获取保存状态文本
   * @description 返回适合显示在 UI 上的状态描述文本
   */
  statusText: Ref<string>
}

/**
 * 默认配置
 */
const DEFAULT_OPTIONS = {
  interval: 120000,
  debounceWait: 3000,
  enabled: true,
  maxRetries: 3,
  retryInterval: 5000,
  debug: import.meta.env.DEV
}

/**
 * 自动保存组合式函数
 *
 * @description 提供强大的自动保存功能，支持：
 * - 定时自动保存（每 N 毫秒）
 * - 数据变化防抖保存（变化后延迟 N 秒）
 * - 手动立即保存
 * - 保存失败自动重试
 * - 保存状态管理
 *
 * @param options 自动保存配置选项
 * @returns 自动保存相关的响应式状态和方法
 *
 * @example
 * ```typescript
 * // 基础用法
 * const { isSaving, lastSavedTime, saveNow, status } = useAutoSave({
 *   saveFn: async () => {
 *     const res = await api.updateDraft(draftData)
 *     return res.code === 200
 *   }
 * })
 *
 * // 监听表单数据变化
 * const form = reactive({ content: '', title: '' })
 * const { isSaving, status } = useAutoSave({
 *   saveFn: async () => {
 *     return await api.save(form)
 *   },
 *   watchSource: toRef(form, 'content'),
 *   interval: 60000 // 1分钟
 * })
 *
 * // 在 Vue 组件中使用
 * <template>
 *   <div>
 *     <div v-if="isSaving">保存中...</div>
 *     <div v-else-if="status === 'saved'">已保存于 {{ lastSavedTime }}</div>
 *     <el-button @click="saveNow">立即保存</el-button>
 *   </div>
 * </template>
 *
 * @see {@link https://vuejs.org/guide/reusability/composables.html Vue 3 组合式函数}
 */
export function useAutoSave(options: UseAutoSaveOptions): UseAutoSaveReturn {
  const {
    saveFn,
    interval = DEFAULT_OPTIONS.interval,
    debounceWait = DEFAULT_OPTIONS.debounceWait,
    enabled = DEFAULT_OPTIONS.enabled,
    watchSource,
    maxRetries = DEFAULT_OPTIONS.maxRetries,
    retryInterval = DEFAULT_OPTIONS.retryInterval,
    onSuccess,
    onError,
    debug = DEFAULT_OPTIONS.debug
  } = options

  const isSaving = ref(false)
  const lastSavedTime = ref<Date | null>(null)
  const saveCount = ref(0)
  const errorCount = ref(0)
  const currentRetryCount = ref(0)
  const status = ref<'idle' | 'saving' | 'saved' | 'error' | 'disabled'>('idle')

  let intervalTimer: ReturnType<typeof setInterval> | null = null
  let debounceTimer: ReturnType<typeof setTimeout> | null = null
  let retryTimer: ReturnType<typeof setTimeout> | null = null
  let isEnabled = typeof enabled === 'boolean' ? ref(enabled) : enabled

  const log = (message: string, ...args: any[]) => {
    if (debug) {
      console.log(`[useAutoSave] ${message}`, ...args)
    }
  }

  const updateStatus = (newStatus: typeof status.value) => {
    status.value = newStatus
    log(`状态变更: ${newStatus}`)
  }

  /**
   * 执行保存操作
   * @param isManual 是否为手动保存
   */
  const performSave = async (isManual = false) => {
    if (isSaving.value) {
      log('保存操作正在进行中，跳过')
      return
    }

    if (!isEnabled?.value) {
      log('自动保存已暂停')
      return
    }

    isSaving.value = true
    updateStatus('saving')
    log('开始保存...')

    try {
      const success = await saveFn()

      if (success) {
        lastSavedTime.value = new Date()
        saveCount.value++
        currentRetryCount.value = 0
        updateStatus('saved')
        log(`保存成功！累计保存 ${saveCount.value} 次`)

        if (onSuccess) {
          onSuccess()
        }

        setTimeout(() => {
          if (status.value === 'saved') {
            updateStatus('idle')
          }
        }, 3000)

        return true
      } else {
        throw new Error('保存函数返回失败')
      }
    } catch (error) {
      const err = error as Error
      errorCount.value++
      currentRetryCount.value++
      log(`保存失败: ${err.message}`)

      if (currentRetryCount.value < maxRetries && !isManual) {
        log(`将在 ${retryInterval / 1000} 秒后重试 (${currentRetryCount.value}/${maxRetries})`)
        updateStatus('error')

        retryTimer = setTimeout(() => {
          performSave(isManual)
        }, retryInterval)

        if (onError) {
          onError(err, currentRetryCount.value)
        }
      } else {
        log(`已达到最大重试次数 (${maxRetries})，停止重试`)
        updateStatus('error')

        if (onError) {
          onError(err, currentRetryCount.value)
        }
      }

      return false
    } finally {
      if (currentRetryCount.value === 0 || currentRetryCount.value >= maxRetries) {
        isSaving.value = false
      }
    }
  }

  /**
   * 手动立即保存
   * @description 忽略当前保存状态，立即执行保存
   */
  const saveNow = async () => {
    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }

    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }

    await performSave(true)
  }

  /**
   * 启动定时器
   */
  const startIntervalTimer = () => {
    if (intervalTimer) {
      clearInterval(intervalTimer)
    }

    log(`启动定时器，间隔 ${interval / 1000} 秒`)

    intervalTimer = setInterval(() => {
      performSave(false)
    }, interval)
  }

  /**
   * 停止定时器
   */
  const stopIntervalTimer = () => {
    if (intervalTimer) {
      clearInterval(intervalTimer)
      intervalTimer = null
      log('定时器已停止')
    }
  }

  /**
   * 启动防抖监听
   */
  const startDebounceWatch = () => {
    if (!watchSource) {
      return
    }

    log('启动防抖监听')

    watch(
      watchSource,
      () => {
        if (debounceTimer) {
          clearTimeout(debounceTimer)
        }

        debounceTimer = setTimeout(() => {
          log('数据变化，触发保存')
          performSave(false)
        }, debounceWait)
      },
      { deep: true }
    )
  }

  /**
   * 重置自动保存状态
   */
  const resetAutoSave = () => {
    saveCount.value = 0
    errorCount.value = 0
    currentRetryCount.value = 0
    lastSavedTime.value = null
    updateStatus('idle')
    log('状态已重置')
  }

  /**
   * 销毁自动保存
   */
  const destroyAutoSave = () => {
    log('销毁自动保存实例')

    stopIntervalTimer()

    if (debounceTimer) {
      clearTimeout(debounceTimer)
      debounceTimer = null
    }

    if (retryTimer) {
      clearTimeout(retryTimer)
      retryTimer = null
    }

    isSaving.value = false
    updateStatus('idle')
  }

  /**
   * 暂停自动保存
   */
  const pause = () => {
    if (isEnabled?.value) {
      stopIntervalTimer()
      isEnabled.value = false
      updateStatus('disabled')
      log('自动保存已暂停')
    }
  }

  /**
   * 恢复自动保存
   */
  const resume = () => {
    if (!isEnabled?.value) {
      isEnabled.value = true
      startIntervalTimer()
      updateStatus('idle')
      log('自动保存已恢复')
    }
  }

  /**
   * 计算状态文本
   */
  const statusText = ref('')

  const updateStatusText = () => {
    switch (status.value) {
      case 'idle':
        statusText.value = lastSavedTime.value
          ? `已保存于 ${formatTime(lastSavedTime.value)}`
          : '等待保存'
        break
      case 'saving':
        statusText.value = '保存中...'
        break
      case 'saved':
        statusText.value = '保存成功'
        break
      case 'error':
        statusText.value = '保存失败'
        break
      case 'disabled':
        statusText.value = '自动保存已暂停'
        break
    }
  }

  const formatTime = (date: Date) => {
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    const seconds = String(date.getSeconds()).padStart(2, '0')
    return `${hours}:${minutes}:${seconds}`
  }

  watch(status, updateStatusText, { immediate: true })
  watch(lastSavedTime, updateStatusText)

  startIntervalTimer()
  startDebounceWatch()

  onUnmounted(() => {
    destroyAutoSave()
  })

  return {
    isSaving,
    lastSavedTime,
    saveCount,
    errorCount,
    currentRetryCount,
    status,
    saveNow,
    resetAutoSave,
    destroyAutoSave,
    pause,
    resume,
    statusText
  }
}

/**
 * 创建草稿自动保存组合式函数
 *
 * @description 专门用于表单草稿的自动保存，封装了常见的草稿保存逻辑
 *
 * @param draftId 草稿ID
 * @param draftContent 草稿内容（Ref）
 * @param saveApi 保存API函数
 * @param options 额外配置
 *
 * @example
 * ```typescript
 * const { isSaving, statusText } = useDraftAutoSave(
 *   draftId,
 *   toRef(form, 'content'),
 *   async (id, content) => {
 *     return await api.saveDraft({ id, content })
 *   }
 * )
 * ```
 */
export function useDraftAutoSave<T = any>(
  draftId: Ref<T> | T,
  draftContent: Ref<string>,
  saveApi: (id: T, content: string) => Promise<boolean>,
  options?: Partial<Omit<UseAutoSaveOptions, 'saveFn' | 'watchSource'>>
): UseAutoSaveReturn {
  const id = typeof draftId === 'object' && 'value' in draftId ? draftId.value : draftId

  return useAutoSave({
    ...options,
    saveFn: async () => {
      return await saveApi(id, draftContent.value)
    },
    watchSource: draftContent,
    interval: options?.interval ?? 60000,
    debounceWait: options?.debounceWait ?? 5000
  })
}

/**
 * 创建内容变更自动保存组合式函数
 *
 * @description 监听内容变更后自动保存，适用于文档编辑器等场景
 *
 * @param content 内容（Ref）
 * @param saveApi 保存API函数
 * @param options 额外配置
 *
 * @example
 * ```typescript
 * const { isSaving, status } = useContentAutoSave(
 *   editorContent,
 *   async (content) => {
 *     return await api.saveDocument({ content })
 *   }
 * )
 * ```
 */
export function useContentAutoSave(
  content: Ref<string>,
  saveApi: (content: string) => Promise<boolean>,
  options?: Partial<UseAutoSaveOptions>
): UseAutoSaveReturn {
  return useAutoSave({
    ...options,
    saveFn: async () => {
      return await saveApi(content.value)
    },
    watchSource: content,
    interval: options?.interval ?? 30000,
    debounceWait: options?.debounceWait ?? 2000
  })
}
