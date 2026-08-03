<template>
  <a-layout-header class="header">
    <div class="header-inner">
      <!-- 左侧：Logo和标题 -->
      <div class="header-left">
        <RouterLink to="/">
          <div class="header-left">
            <img class="logo" src="@/assets/logo.png" alt="Logo" />
            <h1 class="site-title">Code Craft</h1>
          </div>
        </RouterLink>
      </div>

      <!-- 中间：桌面端导航菜单 -->
      <div class="header-menu-desktop">
        <a-menu
          v-model:selectedKeys="selectedKeys"
          mode="horizontal"
          :items="menuItems"
          @click="handleMenuClick"
        />
      </div>

      <!-- 右侧：用户操作区域 -->
      <div class="header-right">
        <div class="user-login-status">
          <div v-if="loginUserStore.loginUser.id">
            <a-dropdown>
              <a-space>
                <a-avatar :src="loginUserStore.loginUser.userAvatar" />
                <span class="user-name-text">{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
              </a-space>
              <template #overlay>
                <a-menu>
                  <a-menu-item @click="doLogout">
                    <LogoutOutlined />
                    退出登录
                  </a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </div>
          <div v-else>
            <a-button type="primary" href="/user/login">登录</a-button>
          </div>
        </div>
        <!-- 移动端汉堡菜单按钮 -->
        <a-button class="header-menu-toggle" type="text" @click="mobileMenuVisible = true">
          <MenuOutlined />
        </a-button>
      </div>
    </div>

    <!-- 移动端抽屉菜单 -->
    <a-drawer
      v-model:open="mobileMenuVisible"
      placement="right"
      title="菜单"
      width="260px"
      class="mobile-menu-drawer"
    >
      <a-menu
        v-model:selectedKeys="selectedKeys"
        mode="inline"
        :items="menuItems"
        @click="handleMobileMenuClick"
      />
      <div class="mobile-user-area">
        <template v-if="loginUserStore.loginUser.id">
          <div class="mobile-user-info">
            <a-avatar :src="loginUserStore.loginUser.userAvatar" />
            <span>{{ loginUserStore.loginUser.userName ?? '无名' }}</span>
          </div>
          <a-button block @click="doLogout">
            <LogoutOutlined />
            退出登录
          </a-button>
        </template>
        <template v-else>
          <a-button type="primary" block href="/user/login">登录</a-button>
        </template>
      </div>
    </a-drawer>
  </a-layout-header>
</template>

<script setup lang="ts">
import { computed, h, ref } from 'vue'
import { useRouter } from 'vue-router'
import { type MenuProps, message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser.ts'
import { userLogout } from '@/api/userController.ts'
import { LogoutOutlined, HomeOutlined, MenuOutlined } from '@ant-design/icons-vue'

const loginUserStore = useLoginUserStore()
const router = useRouter()
// 当前选中菜单
const selectedKeys = ref<string[]>(['/'])
// 移动端抽屉菜单显隐
const mobileMenuVisible = ref(false)
// 监听路由变化，更新当前选中菜单
router.afterEach((to) => {
  selectedKeys.value = [to.path]
})

// 菜单配置项
const originItems = [
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: '主页',
    title: '主页',
  },
  {
    key: '/admin/userManage',
    label: '用户管理',
    title: '用户管理',
  },
  {
    key: '/admin/appManage',
    label: '应用管理',
    title: '应用管理',
  }
]

// 过滤菜单项
const filterMenus = (menus = [] as MenuProps['items']) => {
  return menus?.filter((menu) => {
    const menuKey = menu?.key as string
    if (menuKey?.startsWith('/admin')) {
      const loginUser = loginUserStore.loginUser
      if (!loginUser || loginUser.userRole !== 'admin') {
        return false
      }
    }
    return true
  })
}

// 展示在菜单的路由数组
const menuItems = computed<MenuProps['items']>(() => filterMenus(originItems))

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  // 跳转到对应页面
  if (key.startsWith('/')) {
    router.push(key)
  }
}

// 移动端菜单点击：跳转后关闭抽屉
const handleMobileMenuClick: MenuProps['onClick'] = (e) => {
  handleMenuClick(e)
  mobileMenuVisible.value = false
}

// 用户注销
const doLogout = async () => {
  const res = await userLogout()
  if (res.data.code === 0) {
    loginUserStore.setLoginUser({
      userName: '未登录',
    })
    mobileMenuVisible.value = false
    message.success('退出登录成功')
    await router.push('/user/login')
  } else {
    message.error('退出登录失败，' + res.data.message)
  }
}
</script>

<style scoped>
.header {
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  padding: 0 36px;
  border-bottom: 1px solid var(--line-soft);
  height: 64px;
  line-height: 64px;
  position: sticky;
  top: 0;
  z-index: 50;
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: 16px;
  max-width: var(--maxw);
  margin: 0 auto;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-shrink: 0;
}

.logo {
  height: 28px;
  width: 28px;
  /* 像素方框标记：内嵌实心方块 */
  border: 1px solid var(--ink);
  padding: 4px;
  object-fit: contain;
  background: var(--bg);
}

.site-title {
  margin: 0;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
  font-style: italic;
  font-weight: 500;
  font-size: 18px;
  letter-spacing: -0.01em;
  color: var(--ink);
  white-space: nowrap;
}

.header-menu-desktop {
  flex: 1;
  min-width: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

/* 汉堡按钮仅移动端显示 */
.header-menu-toggle {
  display: none;
  color: var(--ink);
  font-size: 20px;
  align-items: center;
  justify-content: center;
}

.user-login-status {
  color: var(--ink-2);
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

.user-login-status .ant-avatar {
  margin-right: 4px;
}

/* 菜单栏：透明白底、墨色字 */
:deep(.ant-menu) {
  background: transparent;
  color: var(--ink-2);
}

:deep(.ant-menu-horizontal) {
  border-bottom: none !important;
  background: transparent;
  line-height: 62px;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

:deep(.ant-menu-item),
:deep(.ant-menu-submenu) {
  color: var(--ink-2) !important;
  transition: color 0.2s;
}

:deep(.ant-menu-item:hover),
:deep(.ant-menu-submenu:hover) {
  background: transparent !important;
  color: var(--ink) !important;
}

:deep(.ant-menu-item:hover::after),
:deep(.ant-menu-submenu:hover::after) {
  border-bottom-color: var(--steel) !important;
}

:deep(.ant-menu-item-selected) {
  background: transparent !important;
  color: var(--ink) !important;
}

:deep(.ant-menu-item-selected::after) {
  border-bottom-color: var(--ink) !important;
}

/* 抽屉内菜单样式 */
.mobile-menu-drawer :deep(.ant-menu-inline) {
  background: transparent;
  border-right: none;
}

.mobile-user-area {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--line);
}

.mobile-user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--ink-2);
  margin-bottom: 12px;
  padding: 4px 0;
  font-family: 'Fraunces', 'Noto Serif SC', serif;
}

/* ===== 移动端 ===== */
@media (max-width: 768px) {
  .header {
    padding: 0 16px;
    height: 56px;
    line-height: 56px;
  }

  .header-inner {
    gap: 8px;
  }

  .logo {
    height: 24px;
    width: 24px;
    padding: 3px;
  }

  .site-title {
    font-size: 16px;
  }

  /* 隐藏桌面横向菜单 */
  .header-menu-desktop {
    display: none;
  }

  /* 右侧区域（头像 + 三条杠）整体靠右，三条杠位于最右 */
  .header-right {
    margin-left: auto;
  }

  /* 显示汉堡按钮 */
  .header-menu-toggle {
    display: inline-flex;
  }

  /* 移动端隐藏用户名，仅保留头像，节省空间 */
  .user-name-text {
    display: none;
  }

  :deep(.ant-menu-horizontal) {
    line-height: 54px;
  }
}
</style>
