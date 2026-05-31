<template>
  <div class="app-header">
    <div class="header-left">
      <el-icon class="collapse-icon" @click="toggleSidebar">
        <Fold v-if="!sidebarOpened" />
        <Expand v-else />
      </el-icon>
      <img src="@/assets/images/logo.png" alt="Logo" class="header-logo" @error="handleLogoError" />
      <span class="header-title">港大医院投诉管理系统</span>
    </div>

    <div class="header-center">
      <AppBreadcrumb />
    </div>

    <div class="header-right">
      <el-tooltip content="全屏" placement="bottom">
        <el-icon class="header-icon" @click="toggleFullScreen">
          <FullScreen v-if="!isFullScreen" />
          <Close v-else />
        </el-icon>
      </el-tooltip>

      <el-tooltip content="消息通知" placement="bottom">
        <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99" class="badge-item">
          <el-icon class="header-icon" @click="showNotification">
            <Bell />
          </el-icon>
        </el-badge>
      </el-tooltip>

      <el-dropdown trigger="click" @command="handleCommand">
        <div class="user-dropdown">
          <el-avatar :size="32" :src="userAvatar" class="user-avatar">
            {{ userStore.userInfo?.realName?.charAt(0) || 'A' }}
          </el-avatar>
          <span class="user-name">{{ userStore.userInfo?.realName || '管理员' }}</span>
          <el-icon class="user-arrow"><ArrowDown /></el-icon>
        </div>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item command="profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-dropdown-item>
            <el-dropdown-item command="password">
              <el-icon><Lock /></el-icon>
              <span>修改密码</span>
            </el-dropdown-item>
            <el-dropdown-item divided command="logout">
              <el-icon><SwitchButton /></el-icon>
              <span>退出登录</span>
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>

    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="500px" append-to-body>
      <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" show-password />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="passwordLoading" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { Fold, Expand, FullScreen, Close, Bell, ArrowDown, User, Lock, SwitchButton } from '@element-plus/icons-vue'
import { useAppStore } from '@/store/modules/app'
import { useUserStore } from '@/store/modules/user'
import { logout as logoutApi, changePassword as changePasswordApi } from '@/api/auth'
import AppBreadcrumb from './AppBreadcrumb.vue'

const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const sidebarOpened = computed(() => appStore.sidebar.opened)
const isFullScreen = ref(false)
const unreadCount = ref(3)
const userAvatar = ref('')

const passwordDialogVisible = ref(false)
const passwordLoading = ref(false)
const passwordFormRef = ref<FormInstance>()

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: string, callback: any) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度在 8 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const toggleSidebar = () => {
  appStore.toggleSidebar()
}

const toggleFullScreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
    isFullScreen.value = true
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen()
    }
    isFullScreen.value = false
  }
}

const showNotification = () => {
  ElMessage.info('消息通知功能开发中...')
}

const handleCommand = async (command: string) => {
  switch (command) {
    case 'profile':
      ElMessage.info('个人中心功能开发中...')
      break
    case 'password':
      passwordDialogVisible.value = true
      break
    case 'logout':
      try {
        await logoutApi()
      } catch (error) {
        console.error('Logout API error:', error)
      }
      userStore.logout()
      ElMessage.success('退出登录成功')
      router.push('/login')
      break
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (!valid) return

    passwordLoading.value = true
    try {
      await changePasswordApi(passwordForm.oldPassword, passwordForm.newPassword)
      ElMessage.success('密码修改成功，请重新登录')
      passwordDialogVisible.value = false
      passwordFormRef.value?.resetFields()
      userStore.logout()
      router.push('/login')
    } catch (error: any) {
      ElMessage.error(error.message || '密码修改失败')
    } finally {
      passwordLoading.value = false
    }
  })
}

const handleLogoError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.style.display = 'none'
}
</script>

<style scoped lang="scss">
@use '@/assets/styles/variables' as *;

.app-header {
  height: 60px;
  display: flex;
  align-items: center;
  background: #fff;
  border-bottom: 1px solid $border-light;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;

  .collapse-icon {
    font-size: 20px;
    cursor: pointer;
    color: $text-regular;
    transition: color 0.3s;

    &:hover {
      color: $primary-color;
    }
  }

  .header-logo {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    object-fit: cover;
  }

  .header-title {
    font-size: 18px;
    font-weight: 600;
    color: $text-primary;
    white-space: nowrap;
  }
}

.header-center {
  flex: 1;
  display: flex;
  justify-content: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;

  .header-icon {
    font-size: 20px;
    cursor: pointer;
    color: $text-regular;
    transition: color 0.3s;

    &:hover {
      color: $primary-color;
    }
  }

  .badge-item {
    :deep(.el-badge__content) {
      top: -2px;
      right: 2px;
    }
  }
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.3s;

  &:hover {
    background-color: $bg-color;
  }

  .user-avatar {
    background: $primary-color;
    color: #fff;
    font-size: 14px;
  }

  .user-name {
    font-size: 14px;
    color: $text-primary;
  }

  .user-arrow {
    font-size: 12px;
    color: $text-secondary;
  }
}

:deep(.el-dropdown-menu__item) {
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    font-size: 16px;
  }
}
</style>
