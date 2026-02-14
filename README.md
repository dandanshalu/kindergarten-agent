# 幼儿园老师 Agent 网站项目

面向幼儿园教师的专属 AI 文案生成 Agent 网站。本项目当前为最简版：支持聊天式交互与大模型对话。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.2 + WebFlux（调用 OpenAI 兼容 API）
- **前端**：React 18 + TypeScript + Vite + Ant Design 5

## 快速启动

### 1. 配置 LLM API Key

项目默认使用 DeepSeek，需配置 API Key。详见 [大模型底座配置说明](docs/LLM_CONFIG.md)。

```bash
# 设置环境变量（推荐）
export KINDERGARTEN_LLM_API_KEY=your-deepseek-api-key

# 或在 backend/src/main/resources/application.yml 中修改
kindergarten:
  llm:
    api-key: your-deepseek-api-key
```

### 2. 启动后端

```bash
cd backend
# 使用 JDK 21
./mvnw spring-boot:run
```

后端默认在 http://localhost:8080 启动。

### 3. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认在 http://localhost:5173 启动，通过 Vite 代理将 `/api` 请求转发到后端。

### 4. 访问

在浏览器打开 http://localhost:5173 ，即可看到聊天页面并与大模型对话。

## 项目结构

```
kindergarten-agent/
├── backend/                 # Java 后端
│   ├── src/main/java/com/kindergarten/
│   │   ├── KindergartenAgentApplication.java
│   │   ├── controller/      # API 控制器
│   │   ├── dto/             # 请求/响应 DTO（使用 Java Record）
│   │   ├── service/         # LLM 调用服务
│   │   └── config/          # CORS、WebClient 等配置
│   └── src/main/resources/
│       └── application.yml
├── frontend/                # React 前端
│   └── src/
│       ├── api/             # API 封装
│       ├── components/      # ChatInput、MessageList
│       ├── pages/           # Chat 页面
│       └── types/
├── docs/                    # 产品、前后端、测试文档
└── README.md
```

## 代码中的学习注释

- **Java**：JDK 9+ 新特性（Record、var、文本块、模式匹配等）和 Agent/LLM 相关概念均有注释说明
- **前端**：React Hooks、Agent 前端职责等有简要说明

## 文档索引

| 文档 | 说明 |
|------|------|
| [docs/PRD.md](docs/PRD.md) | 产品需求文档 |
| [docs/LLM_CONFIG.md](docs/LLM_CONFIG.md) | 大模型底座配置说明 |
| [docs/BACKEND_SPEC.md](docs/BACKEND_SPEC.md) | 后端技术规格 |
| [docs/FRONTEND_SPEC.md](docs/FRONTEND_SPEC.md) | 前端技术规格 |
| [docs/TEST_SPEC.md](docs/TEST_SPEC.md) | 测试规格 |
