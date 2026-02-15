/**
 * 消息类型定义。
 */
export type MessageRole = 'user' | 'assistant';

export interface Message {
  id: string;
  role: MessageRole;
  content: string;
  createdAt?: string;
}

/**
 * 会话类型定义。
 */
export interface Session {
  id: number;
  user_id: number;
  title: string;
  doc_type_id: string;
  created_at: string;
  updated_at: string;
  messages?: Message[];
}
