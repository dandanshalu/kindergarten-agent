/**
 * 聊天 API 封装。
 *
 * Agent 前端职责：
 * 1. 收集用户输入
 * 2. 发送 POST /api/chat 请求
 * 3. 将返回内容展示为 assistant 消息
 *
 * 使用 Vite 代理时，/api 会自动转发到 localhost:8080
 */
const API_BASE = '/api';

export interface ChatRequest {
  message: string;
  doc_type_id?: string;
}

export interface ChatResponse {
  message: string;
}

export async function sendChatMessage(request: ChatRequest): Promise<ChatResponse> {
  const res = await fetch(`${API_BASE}/chat`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `请求失败: ${res.status}`);
  }

  return res.json();
}
