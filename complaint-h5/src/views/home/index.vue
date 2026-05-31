<template>
  <div class="page-container home-page">
    <van-nav-bar title="港大医院投诉管理系统" />
    
    <div class="banner">
      <img src="https://via.placeholder.com/375x150" alt="banner" class="banner-img" />
    </div>
    
    <div class="menu-grid">
      <div class="menu-item" @click="goToSubmit">
        <van-icon name="edit" size="48" color="#1989fa" />
        <span>提交投诉</span>
      </div>
      <div class="menu-item" @click="goToMyList">
        <van-icon name="list" size="48" color="#07c160" />
        <span>我的投诉</span>
      </div>
      <div class="menu-item" @click="goToFAQ">
        <van-icon name="question-o" size="48" color="#ff976a" />
        <span>常见问题</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goToSubmit = () => {
  router.push('/submit')
}

const goToMyList = () => {
  router.push('/my-list')
}

const goToFAQ = () => {
  router.push('/faq')
}
</script>

<style scoped lang="scss">
.home-page {
  background-color: #f7f8fa;
  
  .banner {
    padding: 16px;
    
    .banner-img {
      width: 100%;
      border-radius: 8px;
    }
  }
  
  .menu-grid {
    display: flex;
    justify-content: space-around;
    padding: 32px 16px;
    background-color: #fff;
    
    .menu-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
      color: #323233;
      font-size: 14px;
    }
  }
}
</style>
