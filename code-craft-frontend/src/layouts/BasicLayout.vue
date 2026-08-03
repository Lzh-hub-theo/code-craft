<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />

    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>

    <!-- 底部版权信息：全屏对话页隐藏，避免多余滚动 -->
    <GlobalFooter v-if="!isFullScreenRoute" />
  </a-layout>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'

const route = useRoute()
// 对话页为全屏应用视图，隐藏页脚
const isFullScreenRoute = computed(() => route.path.startsWith('/app/chat'))
</script>

<style scoped>
.basic-layout {
  background: transparent;
  min-height: 100vh;
  position: relative;
  z-index: 1;
}

.main-content {
  width: 100%;
  padding: 0;
  background: transparent;
  margin: 0;
  position: relative;
  z-index: 1;
}
</style>
