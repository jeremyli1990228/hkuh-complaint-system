<template>
  <div class="login-container">
    <div class="login-left">
      <div class="brand-content">
        <div class="logo-wrapper">
          <img src="@/assets/images/logo.png" alt="HKUH Logo" class="logo" @error="handleLogoError" />
        </div>
        <h1 class="system-title">港大医院投诉管理系统</h1>
        <p class="system-subtitle">HKU Hospital Complaint Management System</p>
        <div class="features">
          <div class="feature-item">
            <el-icon><Connection /></el-icon>
            <span>智能化工单管理</span>
          </div>
          <div class="feature-item">
            <el-icon><Timer /></el-icon>
            <span>SLA实时监控</span>
          </div>
          <div class="feature-item">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据可视化分析</span>
          </div>
        </div>
      </div>
    </div>

    <div class="login-right">
      <div class="login-box">
        <div class="login-header">
          <h2 class="login-title">用户登录</h2>
          <p class="login-subtitle">请输入您的账号信息</p>
        </div>

        <el-form
          ref="formRef"
          :model="loginForm"
          :rules="rules"
          class="login-form"
          @submit.prevent="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="请输入用户名"
              size="large"
              :prefix-icon="User"
              clearable
              autocomplete="username"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              :prefix-icon="Lock"
              :suffix-icon="showPassword ? View : Hide"
              @suffix-icon-click="showPassword = !showPassword"
              clearable
              autocomplete="current-password"
              @keyup.enter="handleLogin"
            />
          </el-form-item>

          <el-form-item prop="captchaCode">
            <el-input
              v-model="loginForm.captchaCode"
              placeholder="请输入验证码"
              size="large"
              :prefix-icon="CircleCheck"
              maxlength="4"
              clearable
              @keyup.enter="handleLogin"
              class="captcha-input"
            >
              <template #append>
                <div class="captcha-wrapper" @click="refreshCaptcha">
                  <img
                    v-if="captchaImage"
                    :src="captchaImage"
                    alt="验证码"
                    class="captcha-image"
                  />
                  <el-icon v-else class="captcha-loading"><Loading /></el-icon>
                </div>
              </template>
            </el-input>
          </el-form-item>

          <el-form-item>
            <div class="form-options">
              <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
              <a href="javascript:void(0)" class="forgot-link">忘记密码？</a>
            </div>
          </el-form-item>

          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handleLogin"
            >
              {{ loading ? '登录中...' : '登 录' }}
            </el-button>
          </el-form-item>
        </el-form>

        <el-alert
          type="info"
          :closable="false"
          class="password-policy"
          show-icon
        >
          <template #title>
            <span class="policy-title">密码策略</span>
            <ul class="policy-list">
              <li>长度8-20位</li>
              <li>必须包含大写字母、小写字母、数字和特殊字符</li>
            </ul>
          </template>
        </el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, FormInstance, FormRules } from 'element-plus'
import { User, Lock, CircleCheck, View, Hide, Connection, Timer, DataAnalysis, Loading } from '@element-plus/icons-vue'
import { getCaptcha, login, type CaptchaVO, type LoginData } from '@/api/auth'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref<FormInstance>()
const loading = ref(false)
const showPassword = ref(false)
const captchaImage = ref('')
const captchaKey = ref('')

const loginForm = reactive<LoginData>({
  username: '',
  password: '',
  captchaKey: '',
  captchaCode: '',
  rememberMe: false
})

const rules = reactive<FormRules>({
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 64, message: '用户名长度在 3 到 64 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, max: 20, message: '密码长度在 8 到 20 个字符', trigger: 'blur' }
  ],
  captchaCode: [
    { required: true, message: '请输入验证码', trigger: 'blur' },
    { min: 4, max: 4, message: '验证码长度为 4 个字符', trigger: 'blur' }
  ]
})

const fetchCaptcha = async () => {
  try {
    const res = await getCaptcha()
    if (res.code === 200 && res.data) {
      captchaImage.value = res.data.captchaImage
      captchaKey.value = res.data.captchaKey
      loginForm.captchaKey = res.data.captchaKey
    }
  } catch (error) {
    ElMessage.error('获取验证码失败')
  }
}

const refreshCaptcha = () => {
  loginForm.captchaCode = ''
  fetchCaptcha()
}

const handleLogin = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(loginForm)

      if (res.code === 200 && res.data) {
        const { accessToken, refreshToken, userInfo } = res.data

        userStore.setToken(accessToken, refreshToken)
        userStore.setUserInfo(userInfo)

        ElMessage.success('登录成功')

        router.push('/')
      } else {
        ElMessage.error(res.message || '登录失败')
        refreshCaptcha()
      }
    } catch (error: any) {
      const errorCode = error?.response?.data?.code
      const errorMsg = error?.response?.data?.message || error.message || '登录失败'

      if (errorCode === 4023) {
        ElMessage.error('账户已锁定，请30分钟后重试')
      } else if (errorMsg.includes('验证码')) {
        ElMessage.error(errorMsg)
        refreshCaptcha()
      } else {
        ElMessage.error(errorMsg)
        refreshCaptcha()
      }
    } finally {
      loading.value = false
    }
  })
}

const handleLogoError = (e: Event) => {
  const target = e.target as HTMLImageElement
  target.style.display = 'none'
}

onMounted(() => {
  fetchCaptcha()
})
</script>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.login-container {
  display: flex;
  min-height: 100vh;
  background: #f0f2f5;
}

.login-left {
  flex: 1;
  background: linear-gradient(135deg, $primary-color 0%, #1a73e8 50%, #005cbf 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -50%;
    width: 200%;
    height: 200%;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.1) 0%, transparent 60%);
    animation: pulse 15s ease-in-out infinite;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 40%;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.1), transparent);
  }
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 0.5;
  }
  50% {
    transform: scale(1.1);
    opacity: 0.8;
  }
}

.brand-content {
  position: relative;
  z-index: 1;
  text-align: center;
  padding: 40px;
  max-width: 500px;
}

.logo-wrapper {
  margin-bottom: 40px;

  .logo {
    width: 120px;
    height: 120px;
    border-radius: 50%;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
    background: white;
    padding: 10px;
  }
}

.system-title {
  font-size: 36px;
  font-weight: 600;
  color: #fff;
  margin-bottom: 12px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.system-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin-bottom: 50px;
  letter-spacing: 1px;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 20px;

  .feature-item {
    display: flex;
    align-items: center;
    gap: 12px;
    color: rgba(255, 255, 255, 0.9);
    font-size: 16px;
    padding: 12px 24px;
    background: rgba(255, 255, 255, 0.1);
    border-radius: 8px;
    backdrop-filter: blur(10px);
    transition: all 0.3s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.2);
      transform: translateX(10px);
    }

    .el-icon {
      font-size: 20px;
    }
  }
}

.login-right {
  width: 520px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: #fff;
}

.login-box {
  width: 100%;
  max-width: 400px;
}

.login-header {
  margin-bottom: 40px;
  text-align: center;
}

.login-title {
  font-size: 28px;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: 8px;
}

.login-subtitle {
  font-size: 14px;
  color: $text-secondary;
}

.login-form {
  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  :deep(.el-input__wrapper) {
    padding: 12px 16px;
    border-radius: 8px;
    box-shadow: 0 0 0 1px $border-color inset;

    &:hover {
      box-shadow: 0 0 0 1px $primary-color inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px rgba($primary-color, 0.2) inset;
    }
  }

  :deep(.el-input__inner) {
    font-size: 15px;

    &::placeholder {
      color: $text-placeholder;
    }
  }
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;

  :deep(.el-checkbox__label) {
    color: $text-regular;
    font-size: 14px;
  }
}

.forgot-link {
  font-size: 14px;
  color: $primary-color;
  text-decoration: none;
  transition: color 0.3s;

  &:hover {
    color: darken($primary-color, 10%);
    text-decoration: underline;
  }
}

.captcha-input {
  :deep(.el-input-group__append) {
    padding: 0;
    background: #f5f7fa;
    border: none;
    border-left: 1px solid $border-color;
  }
}

.captcha-wrapper {
  width: 100px;
  height: 38px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  overflow: hidden;

  &:hover {
    background: #e8e8e8;
  }
}

.captcha-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.captcha-loading {
  font-size: 20px;
  color: $text-secondary;
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.login-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  border-radius: 8px;
  background: linear-gradient(135deg, $primary-color 0%, #1a73e8 100%);
  border: none;
  box-shadow: 0 4px 14px rgba($primary-color, 0.4);
  transition: all 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba($primary-color, 0.5);
  }

  &:active {
    transform: translateY(0);
  }
}

.password-policy {
  margin-top: 24px;
  border-radius: 8px;
  border: 1px solid $border-light;

  :deep(.el-alert__content) {
    padding: 0;
  }

  .policy-title {
    font-weight: 600;
    color: $text-primary;
    margin-bottom: 8px;
    display: block;
  }

  .policy-list {
    margin: 0;
    padding-left: 20px;
    color: $text-secondary;
    font-size: 13px;
    line-height: 1.8;

    li {
      list-style-type: disc;
    }
  }
}

@media (max-width: 1024px) {
  .login-left {
    display: none;
  }

  .login-right {
    width: 100%;
  }
}

@media (max-width: 480px) {
  .login-right {
    padding: 20px;
  }

  .login-box {
    max-width: 100%;
  }

  .login-title {
    font-size: 24px;
  }

  .system-title {
    font-size: 28px;
  }
}
</style>
