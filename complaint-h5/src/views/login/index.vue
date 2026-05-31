<template>
  <div class="page-container login-page">
    <van-nav-bar title="登录" />
    <div class="login-form">
      <van-form @submit="onSubmit">
        <van-cell-group inset>
          <van-field
          v-model="formData.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          required
        />
        <van-field
          v-model="formData.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入密码"
          required
        />
      </van-cell-group>
      <div style="margin: 16px;">
        <van-button round block type="primary" native-type="submit">登录</van-button>
      </div>
      <div class="wechat-login" @click="onWechatLogin">
        <van-icon name="wechat" size="32" />
        <span>微信登录</span>
      </div>
      </van-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

const formData = reactive({
  username: '',
  password: ''
})

const onSubmit = () => {
  router.push('/')
}

const onWechatLogin = () => {
  console.log('微信登录')
}
</script>

<style scoped lang="scss">
.login-page {
  background-color: #fff;
  
  .login-form {
    padding: 32px 16px;
  }
  
  .wechat-login {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-top: 32px;
    color: #07c160;
    font-size: 16px;
  }
}
</style>
