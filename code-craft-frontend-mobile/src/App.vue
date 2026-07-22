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

body {
  margin: 0;
  padding: 0;
  font-family:
    -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell',
    'Fira Sans', 'Droid Sans', 'Helvetica Neue', sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
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
</style>
