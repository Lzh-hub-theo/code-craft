<template>
  <a-layout-footer class="footer">
    <div class="footer-content">
      <p class="copyright">
        <a
          href="https://www.codefather.cn"
          target="_blank"
          rel="noopener noreferrer"
          class="author-link"
        >
          Code Craft 智能应用生成系统
        </a>
      </p>
    </div>
  </a-layout-footer>
</template>

<script setup lang="ts">
// 无需额外的响应式数据
</script>

<style scoped>
.footer {
  background: transparent;
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px 36px;
  margin-top: 40px;
  border-top: 1px solid var(--line-soft);
}

.copyright {
  margin: 0;
  color: var(--ink-3);
  font-size: 12px;
  font-family: 'JetBrains Mono', monospace;
  letter-spacing: 0.04em;
}

.author-link {
  color: var(--steel);
  transition: color 0.2s;
}

.author-link:hover {
  color: var(--steel-d);
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  .footer {
    padding: 14px 12px;
    margin-top: 24px;
  }

  .copyright {
    font-size: 11px;
    line-height: 1.5;
  }
}
</style>
