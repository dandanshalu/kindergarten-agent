/**
 * 消息类型定义。
 *
 * TypeScript 小知识：
 * - type 用于定义类型别名或联合类型
 * - interface 用于定义对象结构，二者在此场景可互换
 */
export type MessageRole = 'user' | 'assistant';

export interface Message {
  id: string;
  role: MessageRole;
  content: string;
  createdAt?: string;
}
