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
  background: rgba(22, 33, 62, 0.95);
  backdrop-filter: blur(10px);
  text-align: center;
  padding: 20px;
  margin-top: 40px;
  border-top: 1px solid #2a2a4a;
}

.copyright {
  margin: 0;
  color: #a0a0a0;
  font-size: 14px;
}

.author-link {
  color: #D4AF37;
}
</style>
