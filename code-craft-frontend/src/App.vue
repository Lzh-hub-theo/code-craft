<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
#app {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  min-height: 100vh;
}

/* 全局样式优化 */
* {
  box-sizing: border-box;
}

html,
body {
  margin: 0;
  padding: 0;
  /* 移动端使用动态视口高度，避免地址栏带来的高度抖动 */
  height: 100%;
}

body {
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  /* 防止 iOS 横屏自动放大字体 */
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
  /* 去除移动端点击高亮 */
  -webkit-tap-highlight-color: transparent;
}

html {
  overflow-x: hidden;
}

/* 金黄色按钮 - 全局覆盖 */
.ant-btn-primary {
  background: linear-gradient(135deg, #D4AF37 0%, #B38728 100%) !important;
  border-color: #B38728 !important;
  color: white !important;
  font-weight: 600;
}

.ant-btn-primary:hover {
  background: linear-gradient(135deg, #E5C158 0%, #C9972E 100%) !important;
  border-color: #C9972E !important;
  color: white !important;
}

.ant-btn-primary:active {
  background: linear-gradient(135deg, #B38728 0%, #966810 100%) !important;
  border-color: #966810 !important;
}

/* ===== 全局移动端适配 ===== */
@media (max-width: 768px) {
  /* 弹窗宽度自适应，避免超出屏幕 */
  .ant-modal {
    max-width: calc(100vw - 24px) !important;
    margin: 8px auto !important;
  }

  .ant-modal .ant-modal-body {
    max-height: calc(100dvh - 160px);
    overflow-y: auto;
  }

  /* 表格在移动端紧凑显示 */
  .ant-table {
    font-size: 13px;
  }

  .ant-table-cell {
    padding: 8px !important;
  }

  /* 分页器简化 */
  .ant-pagination-options,
  .ant-pagination-item-ellipsis {
    display: none !important;
  }

  /* 通知/消息字号 */
  .ant-message-notice-content {
    font-size: 14px;
  }

  /* 卡片内边距收紧 */
  .ant-card-body {
    padding: 12px;
  }

  .ant-card-head {
    min-height: 40px;
    padding: 0 12px;
  }

  /* 表单项间距收紧 */
  .ant-form-item {
    margin-bottom: 12px;
  }

  /* 输入框字体不小于 16px，防止 iOS 聚焦放大 */
  .ant-input,
  .ant-input-affix-wrapper,
  textarea.ant-input {
    font-size: 16px;
  }
}
</style>
