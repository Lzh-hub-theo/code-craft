<script setup lang="ts">
import BasicLayout from '@/layouts/BasicLayout.vue'
</script>

<template>
  <BasicLayout />
</template>

<style>
/* ============ 净白像素 · 设计令牌 ============ */
:root {
  --bg: #ffffff; /* 纯净白 · 主底 */
  --surface: #fafafb; /* 微凹白 · 卡片 */
  --recess: #f4f5f7; /* 凹陷灰白 */
  --line: #e5e7eb; /* 发丝线 */
  --line-soft: #eef0f3; /* 淡发丝 */
  --ink: #0a0a0a; /* 浓墨 */
  --ink-2: #374151; /* 次墨(冷灰) */
  --ink-3: #6b7280; /* 灰墨 */
  --ink-4: #9ca3af; /* 弱灰 */
  --steel: #475569; /* 钢灰青 · 单点缀色 */
  --steel-d: #334155; /* 深钢 */
  --maxw: 1180px;
  --g: 8px;
}

#app {
  background: var(--bg);
  min-height: 100vh;
  position: relative;
}

/* 极淡像素网格 —— 中心可见，边缘消散 */
#app::before {
  content: '';
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
  background-image:
    linear-gradient(var(--line-soft) 1px, transparent 1px),
    linear-gradient(90deg, var(--line-soft) 1px, transparent 1px);
  background-size: var(--g) var(--g);
  opacity: 0.5;
  -webkit-mask-image: radial-gradient(ellipse at 50% 30%, #000 18%, transparent 70%);
  mask-image: radial-gradient(ellipse at 50% 30%, #000 18%, transparent 70%);
}

/* 全局样式优化 */
* {
  box-sizing: border-box;
}

html,
body {
  margin: 0;
  padding: 0;
  height: 100%;
}

body {
  background: var(--bg);
  color: var(--ink);
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-weight: 400;
  line-height: 1.65;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  overflow-x: hidden;
  -webkit-text-size-adjust: 100%;
  text-size-adjust: 100%;
  -webkit-tap-highlight-color: transparent;
}

html {
  overflow-x: hidden;
  scroll-behavior: smooth;
}

a {
  color: inherit;
  text-decoration: none;
}

/* ============ Antd 全局净白像素覆盖 ============ */

/* 主按钮：墨底白字，前置方点，悬停浮起 */
.ant-btn-primary {
  background: var(--ink) !important;
  border-color: var(--ink) !important;
  color: var(--bg) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-weight: 500;
  transition:
    transform 0.22s cubic-bezier(0.2, 0.7, 0.2, 1),
    box-shadow 0.22s cubic-bezier(0.2, 0.7, 0.2, 1) !important;
}

.ant-btn-primary:hover {
  background: var(--ink) !important;
  border-color: var(--ink) !important;
  color: var(--bg) !important;
  transform: translateY(-2px);
  box-shadow: 0 12px 28px -8px rgba(10, 10, 10, 0.35) !important;
}

.ant-btn-primary:active {
  background: var(--steel-d) !important;
  border-color: var(--steel-d) !important;
  transform: none;
}

/* 次按钮：透明底墨边，前置方点 */
.ant-btn-default {
  background: transparent !important;
  border-color: var(--ink) !important;
  color: var(--ink) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  transition:
    transform 0.22s cubic-bezier(0.2, 0.7, 0.2, 1),
    box-shadow 0.22s cubic-bezier(0.2, 0.7, 0.2, 1) !important;
}

.ant-btn-default:hover {
  background: transparent !important;
  border-color: var(--ink) !important;
  color: var(--ink) !important;
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -8px rgba(10, 10, 10, 0.22) !important;
}

/* 链接按钮：钢青色 */
.ant-btn-link {
  color: var(--steel) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.ant-btn-link:hover {
  color: var(--steel-d) !important;
}

/* 危险按钮：保留语义红，但克制 —— 仅描边 */
.ant-btn-dangerous.ant-btn-primary {
  background: #b91c1c !important;
  border-color: #b91c1c !important;
}

.ant-btn-dangerous {
  border-color: #b91c1c !important;
  color: #b91c1c !important;
}

.ant-btn-dangerous:hover {
  background: #b91c1c !important;
  border-color: #b91c1c !important;
  color: #fff !important;
}

/* 输入框：白底发丝线 */
.ant-input,
.ant-input-affix-wrapper,
textarea.ant-input {
  background: var(--bg) !important;
  color: var(--ink) !important;
  border-color: var(--line) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  transition:
    border-color 0.2s,
    box-shadow 0.2s !important;
}

.ant-input::placeholder,
.ant-input-affix-wrapper input::placeholder {
  color: var(--ink-4) !important;
}

.ant-input:hover,
.ant-input-affix-wrapper:hover {
  border-color: var(--ink-4) !important;
}

.ant-input:focus,
.ant-input-affix-wrapper-focused {
  border-color: var(--steel) !important;
  box-shadow: 0 0 0 2px rgba(71, 85, 105, 0.12) !important;
}

/* 下拉选择器 */
.ant-select .ant-select-selector {
  background: var(--bg) !important;
  color: var(--ink) !important;
  border-color: var(--line) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.ant-select:hover .ant-select-selector {
  border-color: var(--ink-4) !important;
}

.ant-select-focused .ant-select-selector {
  border-color: var(--steel) !important;
  box-shadow: 0 0 0 2px rgba(71, 85, 105, 0.12) !important;
}

.ant-select-selection-placeholder {
  color: var(--ink-4) !important;
}

/* 卡片：白底发丝线 */
.ant-card {
  background: var(--bg) !important;
  border-color: var(--line) !important;
  color: var(--ink) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.ant-card-head {
  border-bottom-color: var(--line) !important;
  color: var(--ink) !important;
}

/* 表格：白底发丝线 */
.ant-table {
  background: var(--bg) !important;
  color: var(--ink) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.ant-table-thead > tr > th {
  background: var(--surface) !important;
  color: var(--ink-2) !important;
  border-bottom: 1px solid var(--line) !important;
  font-family: 'Silkscreen', monospace;
  font-size: 11px;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.ant-table-tbody > tr > td {
  background: var(--bg) !important;
  border-bottom: 1px solid var(--line-soft) !important;
  color: var(--ink-2) !important;
  transition: background 0.2s !important;
}

.ant-table-tbody > tr:hover > td {
  background: var(--surface) !important;
}

/* 分页器 */
.ant-pagination-item {
  background: var(--bg) !important;
  border-color: var(--line) !important;
}

.ant-pagination-item a {
  color: var(--ink-3) !important;
  font-family: 'JetBrains Mono', monospace;
}

.ant-pagination-item:hover {
  border-color: var(--ink) !important;
}

.ant-pagination-item:hover a {
  color: var(--ink) !important;
}

.ant-pagination-item-active {
  background: var(--ink) !important;
  border-color: var(--ink) !important;
}

.ant-pagination-item-active a {
  color: var(--bg) !important;
}

.ant-pagination-item-active:hover {
  border-color: var(--ink) !important;
}

.ant-pagination-prev .ant-pagination-item-link,
.ant-pagination-next .ant-pagination-item-link {
  background: var(--bg) !important;
  border-color: var(--line) !important;
  color: var(--ink-3) !important;
}

/* 分割线 */
.ant-divider {
  border-color: var(--line-soft) !important;
}

/* 标签 */
.ant-tag {
  font-family: 'Silkscreen', monospace;
  font-size: 10px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  border-radius: 0;
  padding: 1px 8px;
}

/* 表单标签 */
.ant-form-item-label > label {
  color: var(--ink-2) !important;
  font-family: 'Silkscreen', monospace;
  font-size: 11px;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

/* 模态框 */
.ant-modal-content {
  background: var(--bg) !important;
  border: 1px solid var(--line) !important;
  border-radius: 0 !important;
  box-shadow: 0 24px 60px -20px rgba(10, 10, 10, 0.22) !important;
}

.ant-modal-header {
  background: var(--bg) !important;
  border-bottom: 1px solid var(--line) !important;
}

.ant-modal-title {
  color: var(--ink) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.ant-modal-close-icon {
  color: var(--ink-3) !important;
}

/* 抽屉 */
.ant-drawer-content {
  background: var(--bg) !important;
}

.ant-drawer-header {
  border-bottom-color: var(--line) !important;
}

.ant-drawer-title {
  color: var(--ink) !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

/* 头像 */
.ant-avatar {
  font-family: 'Silkscreen', monospace;
}

/* 顶部菜单（横排）字号与色 */
.ant-menu-horizontal {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

/* 消息提示 */
.ant-message-notice-content {
  background: var(--ink) !important;
  color: var(--bg) !important;
  border-radius: 0 !important;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

/* Spin */
.ant-spin-dot-item {
  background-color: var(--steel) !important;
}

/* ============ 全局移动端适配 ============ */
@media (max-width: 768px) {
  .ant-modal {
    max-width: calc(100vw - 24px) !important;
    margin: 8px auto !important;
  }

  .ant-modal .ant-modal-body {
    max-height: calc(100dvh - 160px);
    overflow-y: auto;
  }

  .ant-table {
    font-size: 13px;
  }

  .ant-table-cell {
    padding: 8px !important;
  }

  .ant-pagination-options,
  .ant-pagination-item-ellipsis {
    display: none !important;
  }

  .ant-message-notice-content {
    font-size: 14px;
  }

  .ant-card-body {
    padding: 12px;
  }

  .ant-card-head {
    min-height: 40px;
    padding: 0 12px;
  }

  .ant-form-item {
    margin-bottom: 12px;
  }

  .ant-input,
  .ant-input-affix-wrapper,
  textarea.ant-input {
    font-size: 16px;
  }
}

@media (prefers-reduced-motion: reduce) {
  * {
    animation: none !important;
    transition: none !important;
  }
}
</style>
