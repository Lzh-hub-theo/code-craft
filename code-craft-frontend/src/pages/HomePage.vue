<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { addApp, listMyAppVoByPage, listGoodAppVoByPage } from '@/api/appController'
import { getDeployUrl } from '@/config/env'
import AppCard from '@/components/AppCard.vue'

const router = useRouter()
const loginUserStore = useLoginUserStore()

// 用户提示词
const userPrompt = ref('')
const creating = ref(false)

// 我的应用数据
const myApps = ref<API.AppVO[]>([])
const myAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 精选应用数据
const featuredApps = ref<API.AppVO[]>([])
const featuredAppsPage = reactive({
  current: 1,
  pageSize: 6,
  total: 0,
})

// 设置提示词
const setPrompt = (prompt: string) => {
  userPrompt.value = prompt
}

// 优化提示词功能已移除

// 创建应用
const createApp = async () => {
  if (!userPrompt.value.trim()) {
    message.warning('请输入应用描述')
    return
  }

  if (!loginUserStore.loginUser.id) {
    message.warning('请先登录')
    await router.push('/user/login')
    return
  }

  creating.value = true
  try {
    const res = await addApp({
      initPrompt: userPrompt.value.trim(),
    })

    if (res.data.code === 0 && res.data.data) {
      message.success('应用创建成功')
      // 跳转到对话页面，确保ID是字符串类型
      const appId = String(res.data.data)
      await router.push(`/app/chat/${appId}`)
    } else {
      message.error('创建失败：' + res.data.message)
    }
  } catch (error) {
    console.error('创建应用失败：', error)
    message.error('创建失败，请重试')
  } finally {
    creating.value = false
  }
}

// 加载我的应用
const loadMyApps = async () => {
  if (!loginUserStore.loginUser.id) {
    return
  }

  try {
    const res = await listMyAppVoByPage({
      pageNum: myAppsPage.current,
      pageSize: myAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      myApps.value = res.data.data.records || []
      myAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载我的应用失败：', error)
  }
}

// 加载精选应用
const loadFeaturedApps = async () => {
  try {
    const res = await listGoodAppVoByPage({
      pageNum: featuredAppsPage.current,
      pageSize: featuredAppsPage.pageSize,
      sortField: 'createTime',
      sortOrder: 'desc',
    })

    if (res.data.code === 0 && res.data.data) {
      featuredApps.value = res.data.data.records || []
      featuredAppsPage.total = res.data.data.totalRow || 0
    }
  } catch (error) {
    console.error('加载精选应用失败：', error)
  }
}

// 查看对话
const viewChat = (appId: string | number | undefined) => {
  if (appId) {
    router.push(`/app/chat/${appId}?view=1`)
  }
}

// 查看作品
const viewWork = (app: API.AppVO) => {
  if (app.deployKey) {
    const url = getDeployUrl(app.deployKey)
    window.open(url, '_blank')
  }
}

// 格式化时间函数已移除，不再需要显示创建时间

// 页面加载时获取数据
onMounted(() => {
  loadMyApps()
  loadFeaturedApps()

  // 鼠标跟随光效
  const handleMouseMove = (e: MouseEvent) => {
    const { clientX, clientY } = e
    const { innerWidth, innerHeight } = window
    const x = (clientX / innerWidth) * 100
    const y = (clientY / innerHeight) * 100

    document.documentElement.style.setProperty('--mouse-x', `${x}%`)
    document.documentElement.style.setProperty('--mouse-y', `${y}%`)
  }

  document.addEventListener('mousemove', handleMouseMove)

  // 清理事件监听器
  return () => {
    document.removeEventListener('mousemove', handleMouseMove)
  }
})
</script>

<template>
  <div id="homePage">
    <div class="container">
      <!-- 网站标题和描述 -->
      <div class="hero-section">
        <h1 class="hero-title">Code Craft Platform</h1>
        <p class="hero-description">"语落成章，网站即现"</p>
      </div>

      <!-- 用户提示词输入框 -->
      <div class="input-section">
        <a-textarea
          v-model:value="userPrompt"
          placeholder="帮我创建个人博客网站"
          :rows="4"
          :maxlength="1000"
          class="prompt-input"
        />
        <div class="input-actions">
          <a-button type="primary" size="large" @click="createApp" :loading="creating">
            <template #icon>
              <span>↑</span>
            </template>
          </a-button>
        </div>
      </div>

      <!-- 快捷按钮 -->
      <div class="quick-actions">
        <a-button
          type="default"
          @click="
            setPrompt(
              '创建一个现代化的个人博客网站，包含文章列表、详情页、分类标签、搜索功能、评论系统和个人简介页面。采用简洁的设计风格，支持响应式布局，文章支持Markdown格式，首页展示最新文章和热门推荐。',
            )
          "
          >个人博客网站</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '设计一个专业的企业官网，包含公司介绍、产品服务展示、新闻资讯、联系我们等页面。采用商务风格的设计，包含轮播图、产品展示卡片、团队介绍、客户案例展示，支持多语言切换和在线客服功能。',
            )
          "
          >企业官网</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '构建一个功能完整的在线商城，包含商品展示、购物车、用户注册登录、订单管理、支付结算等功能。设计现代化的商品卡片布局，支持商品搜索筛选、用户评价、优惠券系统和会员积分功能。',
            )
          "
          >在线商城</a-button
        >
        <a-button
          type="default"
          @click="
            setPrompt(
              '制作一个精美的作品展示网站，适合设计师、摄影师、艺术家等创作者。包含作品画廊、项目详情页、个人简历、联系方式等模块。采用瀑布流或网格布局展示作品，支持图片放大预览和作品分类筛选。',
            )
          "
          >作品展示网站</a-button
        >
      </div>

      <!-- 我的作品 -->
      <div class="section">
        <h2 class="section-title">我的作品</h2>
        <div class="app-grid">
          <AppCard
            v-for="app in myApps"
            :key="app.id"
            :app="app"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="myAppsPage.current"
            v-model:page-size="myAppsPage.pageSize"
            :total="myAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个应用`"
            @change="loadMyApps"
          />
        </div>
      </div>

      <!-- 精选案例 -->
      <div class="section">
        <h2 class="section-title">精选案例</h2>
        <div class="featured-grid">
          <AppCard
            v-for="app in featuredApps"
            :key="app.id"
            :app="app"
            :featured="true"
            @view-chat="viewChat"
            @view-work="viewWork"
          />
        </div>
        <div class="pagination-wrapper">
          <a-pagination
            v-model:current="featuredAppsPage.current"
            v-model:page-size="featuredAppsPage.pageSize"
            :total="featuredAppsPage.total"
            :show-size-changer="false"
            :show-total="(total: number) => `共 ${total} 个案例`"
            @change="loadFeaturedApps"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
#homePage {
  width: 100%;
  margin: 0;
  padding: 0;
  min-height: 100vh;
  background: transparent;
  position: relative;
  overflow: hidden;
}

/* 纯白底 + 极淡像素网格(继承自 #app 的全局底纹)，无需额外背景层 */

.container {
  max-width: var(--maxw);
  margin: 0 auto;
  padding: 0 36px;
  position: relative;
  z-index: 2;
  width: 100%;
  box-sizing: border-box;
}

/* ============ 英雄区域 ============ */
.hero-section {
  text-align: center;
  padding: 96px 0 64px;
  margin-bottom: 32px;
  position: relative;
  overflow: hidden;
}

.hero-section::before {
  /* 顶部钢青像素分隔点（居中） */
  content: '';
  position: absolute;
  top: 60px;
  left: 50%;
  transform: translateX(-50%);
  width: 6px;
  height: 6px;
  background: var(--steel);
}

.hero-title {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-weight: 300;
  font-size: clamp(48px, 8vw, 104px);
  line-height: 1;
  letter-spacing: -0.025em;
  margin: 0 0 22px;
  color: var(--ink);
  position: relative;
  z-index: 2;
  font-variation-settings: 'opsz' 144;
}

.hero-title::first-line {
  font-style: italic;
}

.hero-description {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-style: italic;
  font-weight: 300;
  font-size: clamp(17px, 2vw, 22px);
  margin: 0 auto;
  color: var(--ink-3);
  position: relative;
  z-index: 2;
  max-width: 48ch;
}

/* ============ 输入区域 ============ */
.input-section {
  position: relative;
  margin: 0 auto 24px;
  max-width: 820px;
}

.prompt-input {
  border-radius: 0;
  border: 1px solid var(--line);
  font-size: 16px;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  padding: 22px 64px 22px 22px;
  background: var(--bg);
  box-shadow: 0 10px 30px -18px rgba(10, 10, 10, 0.18);
  color: var(--ink);
  transition:
    border-color 0.2s,
    box-shadow 0.2s,
    transform 0.2s;
}

.prompt-input:hover {
  border-color: var(--ink-4);
}

.prompt-input:focus {
  background: var(--bg);
  border-color: var(--steel);
  box-shadow: 0 0 0 2px rgba(71, 85, 105, 0.12);
  transform: translateY(-1px);
}

.prompt-input::placeholder {
  color: var(--ink-4);
  font-style: italic;
}

.input-actions {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  gap: 8px;
  align-items: center;
}

/* 发送按钮：墨色方块 */
.input-actions :deep(.ant-btn-primary) {
  width: 44px;
  height: 44px;
  padding: 0;
  border-radius: 0;
  font-size: 18px;
}

/* ============ 快捷按钮 ============ */
.quick-actions {
  display: flex;
  gap: 14px;
  justify-content: center;
  margin-bottom: 80px;
  flex-wrap: wrap;
}

.quick-actions .ant-btn {
  border-radius: 0;
  padding: 10px 20px;
  height: auto;
  background: transparent;
  border: 1px solid var(--ink);
  color: var(--ink);
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-size: 14px;
  transition:
    transform 0.22s cubic-bezier(0.2, 0.7, 0.2, 1),
    box-shadow 0.22s cubic-bezier(0.2, 0.7, 0.2, 1);
  position: relative;
}

.quick-actions .ant-btn::before {
  content: '';
  width: 5px;
  height: 5px;
  background: var(--ink);
  display: inline-block;
  margin-right: 9px;
  vertical-align: middle;
  transition: background 0.2s;
}

.quick-actions .ant-btn:hover {
  background: var(--ink);
  border-color: var(--ink);
  color: var(--bg);
  transform: translateY(-2px);
  box-shadow: 0 10px 24px -8px rgba(10, 10, 10, 0.22);
}

.quick-actions .ant-btn:hover::before {
  background: var(--bg);
}

/* ============ 区段 ============ */
.section {
  margin-bottom: 96px;
  padding-top: 48px;
  border-top: 1px solid var(--line-soft);
}

.section-title {
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-weight: 300;
  font-size: clamp(32px, 4.5vw, 52px);
  line-height: 1;
  letter-spacing: -0.02em;
  margin: 0 0 36px;
  color: var(--ink);
  display: flex;
  align-items: baseline;
  gap: 14px;
}

.section-title::before {
  content: '§';
  font-family: 'Silkscreen', monospace;
  font-size: 12px;
  letter-spacing: 0.12em;
  color: var(--steel);
  align-self: center;
}

/* 我的作品网格 */
.app-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

/* 精选案例网格 */
.featured-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 32px;
}

/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 40px;
  padding-top: 24px;
  border-top: 1px solid var(--line-soft);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .container {
    padding: 0 18px;
  }

  .hero-section {
    padding: 48px 0 36px;
    margin-bottom: 16px;
  }

  .hero-section::before {
    top: 36px;
  }

  .hero-description {
    font-size: 15px;
  }

  /* 输入区域 */
  .input-section {
    margin: 0 auto 16px;
  }

  .prompt-input {
    font-size: 16px;
    padding: 16px 56px 16px 16px;
  }

  .input-actions {
    bottom: 10px;
    right: 10px;
  }

  .input-actions :deep(.ant-btn-primary) {
    width: 40px;
    height: 40px;
    font-size: 16px;
  }

  /* 快捷按钮：移动端两列网格，便于触摸 */
  .quick-actions {
    gap: 10px;
    margin-bottom: 48px;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }

  .quick-actions .ant-btn {
    padding: 10px 12px;
    font-size: 14px;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    justify-content: flex-start;
  }

  /* 区域标题与间距 */
  .section {
    margin-bottom: 56px;
    padding-top: 32px;
  }

  .section-title {
    font-size: 26px;
    margin-bottom: 20px;
  }

  .app-grid,
  .featured-grid {
    grid-template-columns: 1fr;
    gap: 16px;
    margin-bottom: 20px;
  }

  .pagination-wrapper {
    margin-top: 24px;
    padding-top: 16px;
  }
}

/* 超小屏：快捷按钮单列 */
@media (max-width: 380px) {
  .quick-actions {
    grid-template-columns: 1fr;
  }
}
</style>
