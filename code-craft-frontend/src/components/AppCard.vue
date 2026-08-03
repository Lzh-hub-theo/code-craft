<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <span class="placeholder-text">craft</span>
      </div>
      <div class="app-overlay">
        <a-space>
          <a-button type="primary" @click="handleViewChat">查看对话</a-button>
          <a-button v-if="app.deployKey" type="default" @click="handleViewWork">查看作品</a-button>
        </a-space>
      </div>
    </div>
    <div class="app-info">
      <div class="app-info-left">
        <a-avatar :src="app.user?.userAvatar" :size="40">
          {{ app.user?.userName?.charAt(0) || 'U' }}
        </a-avatar>
      </div>
      <div class="app-info-right">
        <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
        <p class="app-author">
          {{ app.user?.userName || (featured ? 'NoCode 官方' : '未知用户') }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}
</script>

<style scoped>
.app-card {
  background: var(--bg);
  border-radius: 0;
  overflow: hidden;
  box-shadow: none;
  border: 1px solid var(--line);
  transition:
    transform 0.2s ease,
    box-shadow 0.2s ease,
    border-color 0.2s ease;
  cursor: pointer;
}

.app-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 10px 24px -8px rgba(10, 10, 10, 0.22);
  border-color: var(--ink);
}

.app-preview {
  height: 180px;
  background: var(--recess);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.app-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.placeholder-text {
  font-family: 'Silkscreen', monospace;
  font-size: 28px;
  font-weight: 400;
  letter-spacing: 0.18em;
  text-transform: uppercase;
  color: var(--ink-3);
}

.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.app-info {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-info-left {
  flex-shrink: 0;
}

.app-info-right {
  flex: 1;
  min-width: 0;
}

.app-title {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-size: 16px;
  font-weight: 600;
  margin: 0 0 4px;
  color: var(--ink);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-author {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-size: 14px;
  color: var(--ink-3);
  margin: 0;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  .app-card {
    border-radius: 0;
  }

  .app-card:hover {
    /* 移动端无 hover，去除位移避免误触抖动 */
    transform: none;
    box-shadow: none;
    border-color: var(--line);
  }

  .app-preview {
    height: 150px;
  }

  .placeholder-text {
    font-size: 22px;
  }

  /* 移动端无 hover，操作按钮常显 */
  .app-overlay {
    opacity: 1;
    /* 底部渐变遮罩，避免遮挡预览主体 */
    background: linear-gradient(to top, var(--bg) 0%, rgba(255, 255, 255, 0.6) 60%, transparent 100%);
    align-items: flex-end;
    justify-content: flex-start;
    padding: 10px;
  }

  .app-overlay :deep(.ant-btn) {
    height: 30px;
    padding: 0 12px;
    font-size: 13px;
    border-radius: 0;
  }

  .app-info {
    padding: 12px;
    gap: 10px;
  }

  .app-title {
    font-size: 15px;
  }

  .app-author {
    font-size: 13px;
  }
}
</style>
