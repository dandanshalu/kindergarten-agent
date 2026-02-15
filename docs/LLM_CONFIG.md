# 大模型底座配置说明

本文档说明如何配置项目使用的大模型底座。后端通过 **LangChain4j**（`langchain4j-open-ai`）调用 OpenAI 兼容的 Chat Completions API，可无缝切换多种 LLM 服务。

## 默认配置：DeepSeek

项目默认使用 **DeepSeek** 作为大模型底座：

- **Base URL**：`https://api.deepseek.com`
- **模型**：`deepseek-chat`（对话）或 `deepseek-reasoner`（推理）
- **优势**：中文能力好、性价比高、国内访问稳定

### 获取 DeepSeek API Key

1. 访问 [DeepSeek 开放平台](https://platform.deepseek.com)
2. 注册/登录账号
3. 进入「API Keys」创建密钥
4. 复制密钥并妥善保存

### 配置方式（仅环境变量）

API Key **禁止**写在配置文件或代码中，仅通过环境变量注入：

```bash
export KINDERGARTEN_LLM_API_KEY=sk-xxxxxxxx
```

本地开发可在项目根目录或 `backend/` 下使用 `.env`（需自行 `source .env` 或由 IDE 加载），**.env 不要提交到 Git**，已在 `.gitignore` 中。

未设置时应用启动会报错并提示设置 `KINDERGARTEN_LLM_API_KEY`。

---

## 其他大模型配置

以下服务均提供 OpenAI 兼容接口，只需修改 `base-url`、`api-key`、`model` 即可切换。

### 通义千问（阿里云）

| 配置项 | 值 |
|--------|-----|
| base-url | `https://dashscope.aliyuncs.com/compatible-mode/v1` |
| model | `qwen-turbo` / `qwen-plus` / `qwen-max` |
| api-key | 阿里云 DashScope 控制台获取 |

### 文心一言（百度）

| 配置项 | 值 |
|--------|-----|
| base-url | `https://aip.baidubce.com/rpc/2.0/ai_custom/v1/wenxinworkshop/chat/`（需按百度文档配置） |
| model | 按百度文档填写 |
| api-key | 百度智能云控制台获取 |

**说明**：文心一言接口与 OpenAI 格式略有差异，可能需要额外适配层。

### OpenAI（GPT）

| 配置项 | 值 |
|--------|-----|
| base-url | `https://api.openai.com` |
| model | `gpt-3.5-turbo` / `gpt-4` / `gpt-4-turbo` |
| api-key | OpenAI 平台获取 |

### 其他 OpenAI 兼容服务

如 Ollama（本地）、Moonshot（Kimi）、智谱等，只要提供 `/v1/chat/completions` 接口，均可通过配置切换。

---

## 安全说明：API Key 管理

| 做法 | 说明 |
|------|------|
| ✅ 使用环境变量 | 通过 `KINDERGARTEN_LLM_API_KEY` 注入，不落盘、不进入 Git。 |
| ✅ 生产/CI 使用密钥管理 | K8s Secret、Vault、云厂商密钥服务等，挂载为环境变量。 |
| ❌ 不要写在 application.yml | 配置文件会进仓库，易泄露。当前配置已改为 `${KINDERGARTEN_LLM_API_KEY:}`，无默认值。 |
| ❌ 不要提交 .env | 若用 .env 做本地开发，确保 `.env` 在 `.gitignore` 中且从未提交。 |
| 泄露后 | 立即在对应平台（如 DeepSeek）撤销该 Key 并重新签发。 |

---

## 配置项说明

| 配置项 | 说明 | 示例 |
|--------|------|------|
| kindergarten.llm.base-url | LLM API 地址；LangChain4j 会自动在末尾补 `/v1`（若未带） | `https://api.deepseek.com` |
| kindergarten.llm.api-key | 仅通过环境变量 `${KINDERGARTEN_LLM_API_KEY}` 注入，不在配置中写明文 | — |
| kindergarten.llm.model | 模型名称 | `deepseek-chat` |

**说明**：`LlmConfig` 会据此创建 `OpenAiChatModel` 与 `OpenAiStreamingChatModel`，DeepSeek/通义等已设置 `accumulateToolCallId(false)`。启动时若未设置 API Key 会直接失败并提示。

---

## DeepSeek 模型说明

| 模型 | 说明 | 适用场景 |
|------|------|----------|
| deepseek-chat | 标准对话模式 | 日常文案生成、问答 |
| deepseek-reasoner | 推理模式（Chain-of-Thought） | 复杂逻辑、分步推导 |

幼教文案生成场景推荐使用 `deepseek-chat`，兼顾速度与质量。需要更强推理能力时可切换到 `deepseek-reasoner`。
