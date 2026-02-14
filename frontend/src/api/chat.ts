/**
 * 聊天 API 封装。
 *
 * Agent 前端职责：
 * 1. 收集用户输入
 * 2. 发送 POST /api/chat（同步）或 /api/chat/stream（流式）
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

/**
 * 解析 SSE 行 "data: xxx"，返回内容。
 * 后端为保留换行会将 chunk 按 JSON 字符串发送（如 data: "你\n好"），
 * 此处若为双引号包裹则 JSON.parse 还原，否则按原样返回。
 */
function parseSseDataLine(line: string): string | null {
  const trimmed = line.trim();
  if (!trimmed || !trimmed.startsWith('data:')) return null;
  let data = trimmed.slice(5).trim();
  if (!data) return null;
  if (data.startsWith('"')) {
    try {
      return JSON.parse(data) as string;
    } catch {
      // 非合法 JSON 则当普通字符串用
    }
  }
  return data;
}

/**
 * 流式聊天：POST /api/chat/stream，通过 SSE 逐段接收内容。
 * @param request 请求体
 * @param onChunk 每收到一段内容时调用（一般为追加到当前回复）
 * @param onDone 流结束或出错时调用
 */
export function sendChatMessageStream(
  request: ChatRequest,
  onChunk: (chunk: string) => void,
  onDone: (error?: Error) => void
): void {
  fetch(`${API_BASE}/chat/stream`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request),
  })
    .then(async (res) => {
      if (!res.ok) {
        const text = await res.text();
        onDone(new Error(text || `请求失败: ${res.status}`));
        return;
      }
      const reader = res.body?.getReader();
      if (!reader) {
        onDone(new Error('无响应体'));
        return;
      }
      const decoder = new TextDecoder();
      let buffer = '';
      try {
        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          buffer += decoder.decode(value, { stream: true });
          const lines = buffer.split('\n');
          buffer = lines.pop() ?? '';
          for (const line of lines) {
            const data = parseSseDataLine(line);
            if (data !== null) onChunk(data);
          }
        }
        const data = parseSseDataLine(buffer);
        if (data !== null) onChunk(data);
      } finally {
        reader.releaseLock();
      }
      onDone();
    })
    .catch((err) => {
      onDone(err instanceof Error ? err : new Error(String(err)));
    });
}
