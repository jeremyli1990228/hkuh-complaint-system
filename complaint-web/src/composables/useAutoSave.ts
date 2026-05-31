import { ref, watch, onUnmounted } from 'vue'

interface UseAutoSaveOptions {
  interval?: number
  debounce?: number
  onSave: (content: string) => Promise<void>
}

export function useAutoSave(options: UseAutoSaveOptions) {
  const { interval = 120000, debounce = 2000, onSave } = options

  const autoSaveStatus = ref<'idle' | 'saving' | 'saved' | 'error'>('idle')
  const lastSavedTime = ref<string>('')
  const content = ref('')
  const timer = ref<number | null>(null)
  const debounceTimer = ref<number | null>(null)

  const save = async () => {
    if (!content.value) return

    autoSaveStatus.value = 'saving'
    try {
      await onSave(content.value)
      autoSaveStatus.value = 'saved'
      lastSavedTime.value = new Date().toLocaleTimeString('zh-CN', {
        hour: '2-digit',
        minute: '2-digit',
        second: '2-digit'
      })
    } catch (error) {
      autoSaveStatus.value = 'error'
      console.error('Auto-save failed:', error)
    }
  }

  const debouncedSave = () => {
    if (debounceTimer.value) {
      clearTimeout(debounceTimer.value)
    }
    debounceTimer.value = window.setTimeout(() => {
      save()
    }, debounce)
  }

  const startTimer = () => {
    if (timer.value) {
      clearInterval(timer.value)
    }
    timer.value = window.setInterval(() => {
      if (content.value) {
        save()
      }
    }, interval)
  }

  const stopTimer = () => {
    if (timer.value) {
      clearInterval(timer.value)
      timer.value = null
    }
    if (debounceTimer.value) {
      clearTimeout(debounceTimer.value)
      debounceTimer.value = null
    }
  }

  const updateContent = (newContent: string) => {
    content.value = newContent
    debouncedSave()
  }

  watch(
    () => content.value,
    (newVal, oldVal) => {
      if (newVal !== oldVal && newVal) {
        debouncedSave()
      }
    }
  )

  onUnmounted(() => {
    stopTimer()
  })

  return {
    autoSaveStatus,
    lastSavedTime,
    content,
    updateContent,
    save,
    startTimer,
    stopTimer
  }
}
