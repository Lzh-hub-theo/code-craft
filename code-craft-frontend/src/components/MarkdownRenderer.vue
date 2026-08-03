<template>
  <div class="markdown-content" v-html="renderedMarkdown"></div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'

// 引入代码高亮样式
import 'highlight.js/styles/github.css'

interface Props {
  content: string
}

const props = defineProps<Props>()

// 配置 markdown-it 实例
const md: MarkdownIt = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  highlight: function (str: string, lang: string): string {
    if (lang && hljs.getLanguage(lang)) {
      try {
        return (
          '<pre class="hljs"><code>' +
          hljs.highlight(str, { language: lang, ignoreIllegals: true }).value +
          '</code></pre>'
        )
      } catch {
        // 忽略错误，使用默认处理
      }
    }

    return '<pre class="hljs"><code>' + md.utils.escapeHtml(str) + '</code></pre>'
  },
})

// 计算渲染后的 Markdown
const renderedMarkdown = computed(() => {
  return md.render(props.content)
})
</script>

<style scoped>
.markdown-content {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  line-height: 1.7;
  color: var(--ink);
  word-wrap: break-word;
}

/* 全局样式，影响 v-html 内容 */
.markdown-content :deep(h1),
.markdown-content :deep(h2),
.markdown-content :deep(h3),
.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  margin: 1.5em 0 0.5em 0;
  font-weight: 600;
  line-height: 1.25;
  color: var(--ink);
}

.markdown-content :deep(h1) {
  font-size: 1.5em;
  border-bottom: 1px solid var(--line);
  padding-bottom: 0.3em;
  color: var(--ink);
}

.markdown-content :deep(h2) {
  font-size: 1.3em;
  border-bottom: 1px solid var(--line);
  padding-bottom: 0.3em;
  color: var(--ink);
}

.markdown-content :deep(h3) {
  font-size: 1.1em;
  color: var(--ink);
}

.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  color: var(--ink);
}

.markdown-content :deep(p) {
  margin: 0.8em 0;
  color: var(--ink);
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  margin: 0.8em 0;
  padding-left: 1.5em;
  color: var(--ink);
}

.markdown-content :deep(li) {
  margin: 0.3em 0;
  color: var(--ink);
}

.markdown-content :deep(blockquote) {
  margin: 1em 0;
  padding: 0.5em 1em;
  border-left: 4px solid var(--steel);
  background-color: var(--surface);
  color: var(--ink-2);
}

.markdown-content :deep(code) {
  background-color: var(--recess);
  padding: 0.2em 0.4em;
  border-radius: 0;
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.9em;
  color: var(--ink);
}

.markdown-content :deep(pre) {
  background-color: var(--recess);
  border: 1px solid var(--line);
  border-radius: 0;
  padding: 1em;
  overflow-x: auto;
  margin: 1em 0;
}

/* 代码块滚动条样式 */
.markdown-content :deep(pre)::-webkit-scrollbar {
  height: 8px;
}

.markdown-content :deep(pre)::-webkit-scrollbar-track {
  background: var(--recess);
  border-radius: 0;
}

.markdown-content :deep(pre)::-webkit-scrollbar-thumb {
  background: var(--ink-4);
  border-radius: 0;
}

.markdown-content :deep(pre)::-webkit-scrollbar-thumb:hover {
  background: var(--steel);
}

.markdown-content :deep(pre code) {
  background-color: transparent;
  padding: 0;
  border-radius: 0;
  font-size: 0.9em;
  line-height: 1.4;
  color: var(--ink);
}

.markdown-content :deep(table) {
  border-collapse: collapse;
  margin: 1em 0;
  width: 100%;
}

.markdown-content :deep(table th),
.markdown-content :deep(table td) {
  border: 1px solid var(--line);
  padding: 0.5em 0.8em;
  text-align: left;
  color: var(--ink);
}

.markdown-content :deep(table th) {
  background-color: var(--surface);
  font-weight: 600;
  color: var(--ink);
}

.markdown-content :deep(table tr:nth-child(even)) {
  background-color: var(--surface);
}

.markdown-content :deep(a) {
  color: var(--steel);
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  color: var(--steel-d);
  text-decoration: underline;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 0;
  margin: 0.5em 0;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid var(--line);
  margin: 1.5em 0;
}

/* 代码高亮样式优化 */
.markdown-content :deep(.hljs) {
  background-color: transparent !important;
  border-radius: 0;
  font-family: 'JetBrains Mono', monospace;
  font-size: 0.9em;
  line-height: 1.4;
  color: var(--ink);
}

/* 特定语言的代码块样式（白底柔和版） */
.markdown-content :deep(.hljs-keyword) {
  color: #475569;
  font-weight: 600;
}

.markdown-content :deep(.hljs-string) {
  color: #0a0a0a;
}

.markdown-content :deep(.hljs-comment) {
  color: var(--ink-4);
  font-style: italic;
}

.markdown-content :deep(.hljs-number) {
  color: #334155;
}

.markdown-content :deep(.hljs-function) {
  color: #0a0a0a;
}

.markdown-content :deep(.hljs-tag) {
  color: #475569;
}

.markdown-content :deep(.hljs-attr) {
  color: #334155;
}

.markdown-content :deep(.hljs-title) {
  color: #0a0a0a;
  font-weight: 600;
}
</style>
