<template>
  <div class="app-footer">
    <div class="footer-content">
      <span class="copyright">© 2024 港大医院投诉管理系统 v1.0.0</span>
      <span class="separator">|</span>
      <span class="tech-support">技术支持：信息技术部</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
@import '@/assets/styles/variables.scss';

.app-footer {
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
  border-top: 1px solid $border-light;
  padding: 0 20px;
}

.footer-content {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: $text-secondary;

  .separator {
    color: $border-color;
  }

  .tech-support {
    color: $text-placeholder;
  }
}
</style>
