# 静白重塑 · Code Craft 前端视觉重构

日期：2026-08-03
范围：`code-craft-frontend/` 全站
参考：`code-craft-frontend/reference.html`（静白 NET BAI 风格）

## 目标

把全站视觉从当前的深色 + 金色 + 玻璃拟态，统一到 reference.html 的「静白」视觉语言：纯白底、微灰层次、1px 发丝线、衬线字体、像素标点、单一冷钢青点缀。

## 背景

- 当前项目以深色渐变（#1a1a2e / #16213e / #2a2a4a）、金色按钮渐变（#D4AF37）、玻璃模糊、大圆角、动态光效为主，组件语言来自 ant-design-vue 默认。
- 改造会改变 token、字体、按钮、卡片、表格、表单、弹窗、Tag、分页、AppCard、HomePage、AppChatPage、AppEditPage、登录注册与管理页。

## 决策

- 保留 ant-design-vue 组件库，用 ConfigProvider 主题 + 全局覆盖样式重映射。
- 不新增 UI 组件库，不移除 antd。
- 全部改造走 design tokens（CSS 变量）集中管理。
- 像素装饰构件（sq / pix-rule / corner / label）独立成可复用 utility class。
- 保留现有的路由、Pinia store、API、对话/预览数据流不变。

## 设计 Token

新增 `src/assets/styles/tokens.css`：

```css
:root {
  --bg: #FFFFFF;
  --surface: #FAFAFB;
  --recess: #F4F5F7;
  --line: #E5E7EB;
  --line-soft: #EEF0F3;
  --ink: #0A0A0A;
  --ink-2: #374151;
  --ink-3: #6B7280;
  --ink-4: #9CA3AF;
  --steel: #475569;
  --steel-d: #334155;
  --maxw: 1180px;
  --g: 8px;
}
```

## 字体

`index.html` 引入（与 reference.html 一致）：

- Fraunces
- Noto Serif SC
- Silkscreen
- JetBrains Mono

使用规则：正文 Fraunces / Noto Serif SC；标题 Fraunces 中文 Noto Serif SC；元数据 / 时间 / 数值 JetBrains Mono；标签 / 编号 Silkscreen。系统字体作 fallback。App.vue 重置 body 字体。

## 组件映射

| 组件 | 调整 |
| --- | --- |
| Button | 主：黑底白字衬线 + 像素小方点 + hover 上浮 3px；次：透明 + 1px 墨边；Steel：1px 钢青边；文字：钢青斜体 + 像素方点；禁用：弱灰边 |
| Input / Textarea / Password | 白底 + 1px 发丝边；聚焦时墨色边 + 0 0 0 1px 墨色外环；label 改为 Silkscreen 11px 大写 |
| Card | 白底 + 1px 发丝边，hover 上浮 4px + 阴影；title Silkscreen + 像素点 |
| Table | 表头 Silkscreen 11px + 灰墨底；行 1px 发丝分隔；hover #F4F5F7；关闭斑马纹 |
| Modal | 白底 + 直角 + 1px 发丝边；像素角标；title 衬线斜体 + 像素点；footer 顶 1px 发丝线 |
| Tag | 统一为白底 1px 发丝 + Silkscreen 11px + 钢青小方块 |
| Pagination | 1px 边框 + 衬线数字；hover / active 走 token |
| Alert | 白底 1px 发丝；标题 Silkscreen + 钢青点 |
| Dropdown / Drawer | 白底 + 1px 发丝边 + 衬线菜单项；移动端抽屉同步 |

ConfigProvider 主题 token：

- `colorPrimary` → #0A0A0A
- `colorBgBase` → #FFFFFF
- `colorBorder` → #E5E7EB
- `borderRadius` → 0
- `fontFamily` → Fraunces / Noto Serif SC / serif
- `colorLink` → #475569

## 全局样式

- `assets/styles/reset.css`：body 白底衬线，删除移动端金色高亮与玻璃模糊默认。
- `assets/styles/utilities.css`：`.sq`、`.sq.steel`、`.pix-rule`、`.corner`、`.label`、`.label.steel`、`.serif`、`.cn`、`.pix`、`.mono`。
- `assets/styles/ant-overrides.css`：按上面表格覆盖 antd 组件。
- `assets/styles/index.css`：聚合导入。

## 逐页重写

### App.vue / GlobalHeader / GlobalFooter

- 删除深色背景与金色按钮覆盖。
- body 重置：白底、衬线字体、隐藏默认文本色，文字色用 --ink。
- Header：sticky 白底 + 1px 发丝底边 + Fraunces logo + Silkscreen 编号菜单 + 衬线菜单文字。
- Footer：白底 + 1px 发丝顶边 + Fraunces 衬线大字号 + Silkscreen 标签 + 像素方块印章。

### HomePage

- 容器改白底，删除鼠标光效与多层渐变。
- hero：左侧像素前置 → Silkscreen 大写副标 → Fraunces 大标题（steel 强调） → 衬线说明 → 主/次按钮。
- 快捷按钮：4 个发丝按钮 + 像素方点。
- 我的作品 / 精选案例：使用新 AppCard，section-head 走 reference.html 节奏（编号 + 标题 + 右侧说明）。

### AppCard

- 取消圆角、玻璃模糊、深色底。
- 白底 + 1px 发丝 + cover 衬线 + 像素角标；hover 墨色边 + 像素点变钢青。
- 操作按钮在桌面端 hover 显，移动端常显。

### AppChatPage

- 页面改白底发丝，双栏。
- 顶栏：衬线标题 + Silkscreen 状态 tag。
- 消息：白底 1px 发丝气泡 + 衬线；用户气泡深墨反色。
- 选区信息条：白底 1px 发丝 + Silkscreen 标签。
- 输入框、按钮、预览头、placeholder 全部走新 token。

### AppEditPage

- 页面标题：Silkscreen 编号 + Fraunces 衬线斜体。
- Card 用新样式。
- 描述列表：白底 + 1px 发丝边，label 衬线斜体。

### UserLoginPage / UserRegisterPage

- 容器：白底 + 1px 发丝 + 像素角标 + 衬线斜体标题。
- 输入框、按钮、链接全部走新 token。

### 管理页（User / App / Chat）

- 搜索表单、表格、分页、删除确认弹窗统一改用新 token。
- 表格斑马纹关闭，行 hover #F4F5F7。
- 标签（精选 / 角色）走统一 Tag 样式。
- 关键操作按钮保持功能性：编辑 / 取消精选 / 删除；删除按钮用钢青警示而非红色。

### 弹窗与 MarkdownRenderer

- AppDetailModal、DeploySuccessModal：白底直角 + 衬线标题 + 像素角标 + 发丝 footer。
- MarkdownRenderer：把代码块、引用、链接、列表的颜色映射到新 token，保持行内等宽字体走 JetBrains Mono。

## 响应式

沿用 reference.html 的两档断点：980px、620px。

- ≤980：hero 矩阵装饰隐藏；section-head 单列；卡片 2 列；token 4 列 → 2 列；footer 头 2 列。
- ≤620：导航隐藏编号；hero 缩小；卡片 1 列；分页器简化；移动端聊天页保留 segmented 切换。

## 实施顺序

1. 全局 token、字体、reset、antd 覆盖（assets/styles）。
2. ConfigProvider 主题与公共 utilities。
3. App.vue、GlobalHeader、GlobalFooter、AppCard。
4. HomePage、AppChatPage、AppEditPage、登录注册。
5. 管理页与弹窗。
6. MarkdownRenderer 与全局回归。
7. 移动端响应式回归。

## 风险

- antd v4 与 ConfigProvider token 覆盖范围有限：未覆盖的属性需要写 :deep 覆盖样式。
- ChatPage 移动端已用 segmented 切换，不调整交互。
- 不改后端接口与 SSE 协议，只改样式。
- 现有色值与字体切换后，登录注册、对话页等曾使用蓝色 / 绿色 / 红色作为状态色的位置需要先扫一遍，改为 token 派生色或钢青。

## 非目标

- 不改后端、API、Pinia store、路由表、聊天预览 iframe 通信。
- 不做设计稿截图或视觉走查工具集成。
- 不引入新 UI 库（不替换 antd）。
- 不改动 antd Locale。
