# 幼儿园老师 Agent 网站项目

面向幼儿园教师的专属 AI 文案生成 Agent 网站。本项目当前为最简版：支持聊天式交互与大模型对话。

## 技术栈

- **后端**：Java 21 + Spring Boot 3.2 + LangChain4j（调用 OpenAI 兼容 API，如 DeepSeek/通义）+ Logback
- **网关**：Spring Cloud Gateway + Nacos 注册中心
- **前端**：React 18 + TypeScript + Vite + Ant Design 5

## 快速启动

### 1. 配置 LLM API Key

项目默认使用 DeepSeek，需配置 API Key。详见 [大模型底座配置说明](docs/LLM_CONFIG.md)。

```bash
export KINDERGARTEN_LLM_API_KEY=your-deepseek-api-key
```

### 2. 启动 Nacos（需先运行）

```bash
# Docker 方式（Apple Silicon 需加 --platform linux/amd64）
docker run -d --platform linux/amd64 -p 8848:8848 -p 9848:9848 -p 9849:9849 \
  -e MODE=standalone -e SPRING_DATASOURCE_PLATFORM=derby \
  -v /Users/wxp/database/nacos/logs:/home/nacos/logs \
  -v /Users/wxp/database/nacos/conf:/home/nacos/conf \
  nacos/nacos-server:v2.2.3
```

详见 [网关说明](docs/GATEWAY.md)。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端在 http://localhost:8080 启动，并注册到 Nacos。

### 4. 启动网关

```bash
cd gateway
mvn spring-boot:run
```

网关在 http://localhost:9000 启动，将 `/api` 请求转发至后端。

### 5. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端在 http://localhost:5173 启动，通过 Vite 代理将 `/api` 请求转发到**网关**（9000）。

### 6. 访问

在浏览器打开 http://localhost:5173 ，即可看到聊天页面并与大模型对话。

## 项目结构

```
kindergarten-agent/
├── pom.xml                  # 父 POM（Spring Cloud、Nacos 依赖管理）
├── backend/                 # 后端服务 kindergarten-backend
│   ├── src/main/java/com/kindergarten/
│   │   ├── KindergartenAgentApplication.java
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   └── config/
│   └── src/main/resources/
├── gateway/                 # 网关服务
│   ├── src/main/java/com/kindergarten/gateway/
│   └── src/main/resources/application.yml
├── frontend/                # React 前端
│   └── src/
│       ├── api/
│       ├── components/
│       ├── pages/
│       └── types/
├── docs/
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
| [docs/GATEWAY.md](docs/GATEWAY.md) | 网关与 Nacos 说明 |
| [docs/FRONTEND_SPEC.md](docs/FRONTEND_SPEC.md) | 前端技术规格 |
| [docs/TEST_SPEC.md](docs/TEST_SPEC.md) | 测试规格 |
