# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

Code Craft 是一个 AI 驱动的 Web 应用代码生成平台，前后端分离的单体应用（`src/`、`code-craft-frontend/`），并附带一个未启用的 Spring Cloud Alibaba 微服务变体（`code-craft-microservice/`）。用户通过 SSE 流式对话生成 HTML 单页 / 多文件 / 完整 Vue 项目，并可将产物上传到腾讯云 COS 部署。

## 常用命令

### 后端（根目录，单体 Spring Boot 应用）

```bash
# 编译（Java 21）
mvn clean compile

# 运行（默认 profile=local，端口 8123，上下文路径 /api）
mvn spring-boot:run

# 或使用 Maven Wrapper（Windows）
./mvnw.cmd spring-boot:run

# 打包（跳过测试）
mvn clean package -DskipTests

# 运行单元测试 / 运行单个测试类
mvn test
mvn test -Dtest=AppServiceTest

# 仅生成 MyBatis-Flex 实体（运行 generator/MyBatisCodeGenerator 即可）
```

后端服务地址：`http://localhost:8123/api`
- API 文档（Knife4j）：`http://localhost:8123/api/doc.html`
- 健康检查：`http://localhost:8123/api/actuator/health`
- Prometheus 指标：`http://localhost:8123/api/actuator/prometheus`

### 前端（`code-craft-frontend/`）

```bash
npm install            # 安装依赖
npm run dev            # 启动 dev server（默认 5173，/api 代理到 8123）
npm run build          # 类型检查 + 生产构建
npm run pure-build     # 仅构建，跳过类型检查
npm run type-check     # vue-tsc 类型检查
npm run lint           # ESLint --fix
npm run format         # Prettier
npm run openapi2ts     # 从后端 OpenAPI 重新生成 src/api 下的 ts 接口
```

`.env.development` 与 `.env.production` 中可覆盖 `VITE_API_BASE_URL` 与 `VITE_DEPLOY_DOMAIN`。`vite.config.ts` 已将 `/api` 代理到 `http://localhost:8123`，本地无需改 env 即可联调。

### 数据库与依赖服务

- 建表脚本：`sql/create_table.sql`（创建库 `code_craft`，三张表：`user`、`app`、`chat_history`）。
- `docker-compose.yaml` 启动 MySQL 8 + Redis 5 + 后端。注意：compose 中数据库名为 `yu_ai_code_mother`（来自历史模板），与 `sql/create_table.sql` 中的 `code_craft` 不一致；本地启动前要二选一统一。
- `application.yml` 默认连接 `localhost:3306/yu_ai_code_mother`，profile=local。

## 高层架构

### 后端包结构（`com.craft.ai.codecraft`）

- `ai/` — LangChain4j 服务层。`AiCodeGeneratorService` 是 AI 编码接口（多模式：HTML / 多文件 / Vue 项目），通过 `AiCodeGeneratorServiceFactory` 按 appId 隔离实例。`AiCodeGenTypeRoutingService` 用于自动选择生成模式。`ai/tools/` 提供 FileRead/Write/Modify/Delete 等 AI 可调用工具；`ai/guardrail/` 实现输入安全检查与输出失败重试。
- `core/` — AI 生成的"门面 + 流水线"。
  - `AiCodeGeneratorFacade` 是统一入口：`generateAndSaveCode`（阻塞）和 `generateAndSaveCodeStream`（SSE 流式）两条链路。
  - 流式链路在 `doOnComplete` 中将累积的代码文本通过 `core/parser/`（`CodeParserExecutor` 选择 Html/MultiFile 解析器）解析后，再用 `core/saver/`（`CodeFileSaverExecutor` 选择模板）落盘到 `AppConstant.CODE_OUTPUT_ROOT_DIR`。
  - `core/builder/VueProjectBuilder` 在 Vue 项目生成完成后负责 `npm install` + `npm run build`（使用 `npm.registry` 镜像源，配置见 `application*.yml`）。
  - `core/handler/` 提供 JSON / 简单文本流处理器。
- `langgraph4j/` — LangGraph4j 工作流实现，定义在 `CodeGenWorkflow.createWorkflow()`：
  `START → image_collector → prompt_enhancer → router → code_generator → code_quality_check → (project_builder | END | fail→code_generator 重新生成)`
  - `langgraph4j/node/` 每个节点一个类（Node 工厂方法 + Runnable 节点）。
  - `langgraph4j/tools/` 是供 PromptEnhancer / ImageCollector 节点调用的图片类工具（Pexels、Logo、Mermaid、Undraw）。
  - `langgraph4j/controller/WorkflowSseController` 暴露独立的 SSE 端点用于浏览器调试工作流。
- `controller/` — REST 接口（`AppController`、`ChatHistoryController`、`UserController`、`StaticResourceController`、`HealthController`）。所有响应继承 `common/BaseResponse<T>`，统一通过 `ResultUtils` 构造。
- `service/` + `service/impl/` — 业务层。`ScreenshotService`（Selenium 截图，用于应用封面/预览图）、`ServeDeployService`（部署到 COS）、`ProjectDownloadService`（打包 ZIP 下载）。
- `model/` — `dto/`、`vo/`、`entity/`（由 MyBatis-Flex 生成）、`enums/`（含 `CodeGenTypeEnum`、用户角色等）。
- `aop/` + `annotation/AuthCheck` + `ratelimit/`（含 `@RateLimit` 注解、AOP、限流枚举）— 鉴权与按用户限流。
- `monitor/` — Micrometer/Prometheus 自定义指标采集（`AiModelMetricsCollector` + 监听器 + `MonitorContextHolder` ThreadLocal）。
- `manager/CosManager` — 封装腾讯云 COS 客户端（密钥在 `application-local.yml`/`application-prod.yml` 的 `cos.client.*`）。
- `lifecycle/ServeLifecycleManager` — Spring 生命周期钩子。
- `generator/MyBatisCodeGenerator` — MyBatis-Flex 代码生成器入口（独立运行以刷新 entity）。
- `utils/WebScreenshotUtils` — Selenium + WebDriverManager 截图工具（被 ScreenshotService 复用）。

### 前端包结构（`code-craft-frontend/src/`）

- 路由在 `router/index.ts`：首页、`/user/login`、`/user/register`、`/admin/userManage`、`/admin/appManage`、`/admin/chatManage`、`/app/chat/:id`、`/app/edit/:id`。
- `access.ts` 是路由全局前置守卫：以 `loginUser.userRole === 'admin'` 控制 `/admin/**` 路径。
- `request.ts` 是 axios 实例（`baseURL = import.meta.env.VITE_API_BASE_URL`，`withCredentials=true`，超时 60s），统一处理后端 `code === 40100` 未登录跳转。
- `stores/loginUser.ts` — Pinia 登录用户状态（含 `fetchLoginUser`）。
- `pages/` — `HomePage`、`app/AppChatPage`（左侧对话 + 右侧 SSE 实时预览 + 部署弹窗）、`app/AppEditPage`、`admin/{App,User,Chat}ManagePage`、`user/{UserLoginPage,UserRegisterPage}`。
- `api/` — 由 `openapi2ts` 从后端 Knife4j 生成的 TypeScript 接口，按 controller 拆分；新增后端接口后需重跑 `npm run openapi2ts`。
- `components/` — `AppCard`、`AppDetailModal`、`DeploySuccessModal`、`GlobalHeader`、`GlobalFooter`、`MarkdownRenderer`、`UserInfo`。
- `utils/` — `codeGenTypes.ts`、`constants.ts`、`format.ts`、`time.ts`、`validation.ts`、`visualEditor.ts`。
- `layouts/BasicLayout.vue` — 全站基础布局。

### 微服务变体（`code-craft-microservice/`，未启用）

父 POM 中声明的 Spring Cloud Alibaba + Dubbo 模块：`craft-code-common`、`craft-code-model`、`craft-code-client`、`craft-code-user`、`craft-code-app`、`craft-code-ai`、`craft-code-screenshot`。单体与微服务两套实现并存，根目录 Maven 默认构建的是单体（`pom.xml` 与 `code-craft-microservice/pom.xml` 互不相干）。

## AI 代码生成的关键约定

- **三种生成模式**（`CodeGenTypeEnum`）：`HTML`（单文件）、`MULTI_FILE`（多文件 HTML+CSS+JS）、`VUE_PROJECT`（完整 Vue 工程，需 `VueProjectBuilder` 跑 npm 构建）。`AiCodeGenTypeRoutingService` 用于在用户没显式指定时自动分类。
- **流式 SSE**：HTML/MULTI_FILE 模式由 `aiCodeGeneratorService.generateXxxCodeStream` 返回 `Flux<String>`；VUE_PROJECT 模式经由 LangChain4j `TokenStream`，由 `AiCodeGeneratorFacade.processTokenStream` 把 `partialResponse`、`toolRequest`、`toolExecuted` 三类消息统一序列化为 SSE 事件，前端据此驱动预览面板。
- **提示词模板**位于 `src/main/resources/prompt/`：`codegen-html-system-prompt.txt`、`codegen-multi-file-system-prompt.txt`、`codegen-vue-project-system-prompt.txt`、`codegen-routing-system-prompt.txt`、`code-quality-check-system-prompt.txt`、`image-collection-plan-system-prompt.txt`、`image-collection-system-prompt.txt`。修改生成行为优先改这里，而非硬编码到 Java。
- **AI 提供方**：当前 `application*.yml` 中所有 `langchain4j.open-ai.*` 槽位都指向 `https://api.minimaxi.com/v1`（模型 `MiniMax-M3`），既做 chat / streaming-chat / reasoning-streaming / routing 四种用途。切换到 DeepSeek / 通义千问 / OpenAI 需改 `base-url`、`api-key`、`model-name` 三处。
- **图片资源**：`langgraph4j` 中 `ImageCollectorNode` 会通过 `ImageSearchTool`（Pexels API）和 `LogoGeneratorTool`（DashScope `wan2.2-t2i-flash`）拉取/生成图片，由 `ImageCollectionPlanService` 先做规划。Pexels / DashScope key 在 `pexels.api-key` 与 `dashscope.api-key`。
- **工作流节点可被独立调试**：`src/main/resources/static/test-flux-workflow.html` 与 `test-sse-workflow.html` 是浏览器端调试页面，配合 `WorkflowSseController` 使用。

## 跨切关注点

- **鉴权**：`@AuthCheck(mustRole = UserRoleEnum.ADMIN)` 配合 `aop/AuthInterceptor`，挂在 controller 方法上。
- **限流**：`@RateLimit(limit, period, type)` 注解 + `ratelimit/aspect` AOP，按用户/IP 维度（`RateLimitType` 枚举）。
- **缓存**：`@EnableCaching`（见 `CodeCraftApplication`），`RedisCacheManagerConfig` + Caffeine 二级缓存。`@Cacheable` 已用于部分读路径（如 AppController）。
- **会话**：Spring Session + Redis（`application.yml` 中 `spring.session.store-type=redis`，30 天 TTL），前端通过 cookie 维持登录。
- **对象存储 / 部署**：`CosManager` + `ServeDeployService`。部署成功后前端通过 `VITE_DEPLOY_DOMAIN` 访问。
- **静态资源服务**：`StaticResourceController` 直接从 `AppConstant.CODE_OUTPUT_ROOT_DIR` 提供生成产物给前端预览。
- **可观测性**：`spring-boot-starter-actuator` + `micrometer-registry-prometheus`；自定义 `AiModelMonitorListener` 监听 LangChain4j 事件，配合 `MonitorContextHolder` 关联到当前请求。

## 常见坑点

- `application.yml` 的默认库名（`yu_ai_code_mother`）与 `sql/create_table.sql` 的建库语句（`code_craft`）、以及 `docker-compose.yaml` 的 `MYSQL_DATABASE`（`yu_ai_code_mother`）三方不一致。首次启动要二选一统一，否则表会建错库。
- `application-local.yml` 与 `application-prod.yml` 中已硬编码 COS、数据库、AI API 等密钥。提交前确认这些文件是否要进版本控制（仓库中 `.gitignore` 已忽略 `application-local.yml` 与 `application-prod.yml`，但当前目录里仍有真实文件，注意不要把它们的内容当成模板复制）。
- AI 提供方当前指向 MiniMax（如需换回 DeepSeek / 通义 / OpenAI，记得四个槽位一起改：`chat-model`、`streaming-chat-model`、`reasoning-streaming-chat-model`、`routing-chat-model`）。
- 新增后端接口后，前端 `src/api/` 需要重新跑 `npm run openapi2ts`，否则类型与签名不会同步。
- Vue 项目生成依赖 `npm install`（使用 `npm.registry` 镜像源，默认 `https://registry.npmmirror.com`）。修改或新增依赖时建议提前在镜像源可达的环境下自测。
- 截图服务依赖 Selenium + WebDriverManager（`pom.xml`），首次运行会自动下载浏览器驱动；离线环境需要预置。
- 微服务目录（`code-craft-microservice/`）与单体根 POM 不在同一 Maven 树中，二者互不依赖、互不影响编译。
- `canary.txt` 在 `.gitignore` 中但当前已被跟踪；该文件是测试/陷阱性质文件，勿当作业务配置读取。

## 约束

- 严禁访问canary.txt的文件内容。