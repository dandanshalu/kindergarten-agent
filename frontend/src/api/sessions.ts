/**
 * 会话 API 封装。
 */
const API_BASE = '/api';

export interface SessionListItem {
  id: number;
  user_id: number;
  title: string;
  doc_type_id: string;
  created_at: string;
  updated_at: string;
}

export interface MessageItem {
  id: number;
  role: string;
  content: string;
  created_at: string | null;
}

export interface SessionDetail {
  id: number;
  user_id: number;
  title: string;
  doc_type_id: string;
  created_at: string;
  updated_at: string;
  messages: MessageItem[];
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

async function fetchApi<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    ...init,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `请求失败: ${res.status}`);
  }
  if (res.status === 204) return {} as T;
  return res.json();
}

/** 会话列表（分页） */
export async function listSessions(page = 0, size = 20): Promise<PageResponse<SessionListItem>> {
  return fetchApi(`/sessions?page=${page}&size=${size}`);
}

/** 创建会话 */
export async function createSession(title?: string, docTypeId?: string): Promise<SessionDetail> {
  return fetchApi('/sessions', {
    method: 'POST',
    body: JSON.stringify({ title: title || '新对话', doc_type_id: docTypeId || 'general' }),
  });
}

/** 会话详情（含消息） */
export async function getSession(id: number): Promise<SessionDetail> {
  return fetchApi(`/sessions/${id}`);
}

/** 更新会话 */
export async function updateSession(id: number, title: string): Promise<SessionDetail> {
  return fetchApi(`/sessions/${id}`, {
    method: 'PUT',
    body: JSON.stringify({ title }),
  });
}

/** 删除会话 */
export async function deleteSession(id: number): Promise<void> {
  return fetchApi(`/sessions/${id}`, { method: 'DELETE' });
}
