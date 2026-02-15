# 幼儿园老师 Agent 前端技术规格

## 1. 技术选型

| 类别 | 技术选型 | 版本 | 理由 |
|------|----------|------|------|
| 框架 | React | 18.x | 生态成熟、适合 SPA、便于 Agent 开发 |
| 语言 | TypeScript | 5.x | 类型安全、IDE 友好 |
| 构建 | Vite | 5.x | 快速构建、HMR 体验好 |
| UI 库 | Ant Design | 5.x | 组件全、中文文档好、适合企业级 |
| 状态管理 | Zustand | 4.x | 轻量、易用、适合中小型应用 |
| 请求 | TanStack Query (React Query) | 5.x | 缓存、重试、SSE 支持 |
| 路由 | React Router | 6.x | 官方推荐、嵌套路由 |
| 样式 | CSS Modules / Tailwind | - | 按需选择，建议先 CSS Modules |

---

## 2. 目录结构

```
kindergarten-agent/
├── frontend/
│   ├── public/
│   ├── src/
│   │   ├── api/              # API 封装
│   │   │   ├── auth.ts
│   │   │   ├── sessions.ts
│   │   │   ├── generate.ts
│   │   │   ├── knowledge.ts
│   │   │   └── subscriptions.ts
│   │   ├── components/       # 公共组件
│   │   │   ├── layout/
│   │   │   ├── ChatInput/
│   │   │   ├── MessageList/
│   │   │   ├── DocTypeSelector/
│   │   │   └── KnowledgeUpload/
│   │   ├── pages/
│   │   │   ├── Login/
│   │   │   ├── Home/         # 工作台
│   │   │   ├── Chat/         # 文案生成
│   │   │   ├── Knowledge/    # 知识库管理
│   │   │   ├── Subscription/
│   │   │   └── Profile/
│   │   ├── stores/           # Zustand stores
│   │   │   ├── auth.ts
│   │   │   ├── session.ts
│   │   │   └── subscription.ts
│   │   ├── hooks/
│   │   ├── types/
│   │   ├── utils/
│   │   ├── App.tsx
│   │   └── main.tsx
│   ├── index.html
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
```

---

## 3. 页面与路由

### 3.1 路由设计

| 路径 | 页面 | 说明 |
|------|------|------|
| /login | Login | 登录/注册 |
| / | Home | 首页/工作台（重定向到 /chat 或工作台） |
| /chat | Chat | 主聊天与文案生成 |
| /chat/:sessionId | Chat | 指定会话 |
| /knowledge | Knowledge | 知识库管理 |
| /subscription | Subscription | 订阅中心 |
| /profile | Profile | 个人中心 |

### 3.2 页面说明

**Login**  
- 手机号/邮箱 + 验证码 或 密码  
- 支持第三方登录入口（预留）  
- 注册入口  
- 未登录访问需登录页面时跳转至此  

**Home**  
- 快捷入口：新建对话、最近会话、知识库、订阅  
- 可展示使用统计、推荐文案类型  

**Chat**  
- 当前最简版：单栏布局，MessageList（消息气泡，支持换行与流式展示）+ ChatInput；消息正文使用 `pre-wrap` 保留换行、行高与字号便于阅读。  
- 远期：左侧/顶部文案类型选择（DocTypeSelector）、中央 MessageList + ChatInput、右侧/折叠参数配置面板（年龄段、风格、字数、是否使用知识库）；新建/切换/删除会话。  

**Knowledge**  
- 上传区域（KnowledgeUpload）  
- 文件列表：文件名、大小、状态、上传时间、操作（删除）  
- 解析中状态轮询或 WebSocket 更新  

**Subscription**  
- 套餐列表与对比  
- 当前订阅状态、用量统计  
- 订单列表  

**Profile**  
- 头像、昵称、账号修改  
- 修改密码  
- 退出登录  

---

## 4. 核心组件

### 4.1 ChatInput

**职责**：输入用户需求、发送请求。

**Props**：
- `onSend: (content: string, params?: GenerateParams) => void`
- `disabled?: boolean`
- `placeholder?: string`

**行为**：
- 多行输入，支持 Ctrl+Enter 发送
- 发送前校验（非空、长度限制）
- 发送时置灰、loading 状态

### 4.2 MessageList

**职责**：展示对话历史、流式输出。

**Props**：
- `messages: Message[]`
- `loading?: boolean`
- `streamingContent?: string | null`   // 当前流式内容（可选，远期）
- `onCopy?: (content: string) => void`
- `onRegenerate?: (messageId: number) => void`

**行为**：
- 用户消息与助手消息区分展示（气泡样式、左右对齐）
- **助手消息正文按 Markdown 渲染**（`react-markdown` + `remark-gfm`）：标题、加粗、列表、分隔线、引用、代码块等；用户消息为纯文本并保留换行（`white-space: pre-wrap`）
- 助手消息支持复制、重新生成（远期）
- 流式输出时展示打字机效果（当前实现：流式内容追加到最后一条 assistant 消息的 content）
- 引用来源以折叠/脚注展示（远期）

### 4.3 DocTypeSelector

**职责**：选择文案类型。

**Props**：
- `docTypes: DocType[]`   // 树形结构
- `selectedId?: string`
- `onSelect: (id: string) => void`

**行为**：
- 树形或分组展示
- 搜索过滤
- 选中高亮

### 4.4 KnowledgeUpload

**职责**：上传知识库文件。

**Props**：
- `onUpload: (file: File) => Promise<void>`
- `maxSize?: number`
- `accept?: string`

**行为**：
- 拖拽或点击上传
- 校验类型（PDF、Word、TXT）与大小
- 上传进度与状态反馈

---

## 5. 状态管理

### 5.1 Auth Store（auth.ts）

```typescript
interface AuthState {
  user: User | null;
  token: string | null;
  isLoading: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
  refreshUser: () => Promise<void>;
}
```

### 5.2 Session Store（session.ts）

```typescript
interface SessionState {
  sessions: Session[];
  currentSession: Session | null;
  messages: Message[];
  streamingContent: string | null;
  fetchSessions: () => Promise<void>;
  selectSession: (id: number) => Promise<void>;
  createSession: () => Promise<Session>;
  deleteSession: (id: number) => Promise<void>;
  sendMessage: (content: string, params: GenerateParams) => Promise<void>;
}
```

### 5.3 Subscription Store（subscription.ts）

```typescript
interface SubscriptionState {
  plan: Plan | null;
  usage: Usage | null;
  fetchSubscription: () => Promise<void>;
  fetchUsage: () => Promise<void>;
}
```

---

## 6. API 调用封装与错误处理

### 6.1 请求封装

- 使用 `fetch` 或 `axios`，统一 baseURL（开发时 Vite 代理 `/api` 到网关 9000，见 [GATEWAY.md](GATEWAY.md)）
- 请求头自动注入 `Authorization: Bearer ${token}`
- 401 时清空登录态并跳转登录页

### 6.2 SSE 流式调用

```typescript
// 示例：调用 POST /api/generate/stream
async function* generateStream(
  sessionId: number,
  docTypeId: string,
  userInput: string,
  params: GenerateParams
): AsyncGenerator<string> {
  const res = await fetch('/api/generate/stream', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: JSON.stringify({ sessionId, docTypeId, userInput, params }),
  });
  const reader = res.body!.getReader();
  const decoder = new TextDecoder();
  while (true) {
    const { done, value } = await reader.read();
    if (done) break;
    yield decoder.decode(value, { stream: true });
  }
}
```

### 6.3 错误处理

- 4xx/5xx 统一解析错误信息，通过 Toast 或 Message 展示
- 网络错误、超时给予友好提示
- 关键操作失败提供重试入口

---

## 7. 响应式与无障碍

### 7.1 响应式

- 断点：768px、1024px
- 移动端：侧边栏折叠、参数面板抽屉
- 触摸优化：按钮尺寸 ≥ 44px、适当间距

### 7.2 无障碍

- 关键交互支持键盘操作（Tab、Enter）
- 图片、图标提供 `alt` 或 `aria-label`
- 表单字段有 `label` 或 `aria-labelledby`
- 加载与错误状态通过 `aria-live` 告知屏幕阅读器

---

## 8. 构建与部署

### 8.1 构建

```bash
npm install
npm run build
```

- 输出到 `dist/`
- 支持环境变量：`VITE_API_BASE_URL` 等

### 8.2 部署

- 静态资源托管至 CDN 或 Nginx
- SPA 路由：所有路径回退到 `index.html`
- API 反向代理到后端服务

### 8.3 Nginx 示例

```nginx
location / {
  root /var/www/kindergarten-agent;
  try_files $uri $uri/ /index.html;
}

location /api {
  proxy_pass http://gateway:9000;
  proxy_http_version 1.1;
  proxy_set_header Upgrade $http_upgrade;
  proxy_set_header Connection 'upgrade';
  proxy_set_header Host $host;
}
```
