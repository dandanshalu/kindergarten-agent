import { useState, useCallback } from 'react';
import { Layout, Typography } from 'antd';
import ChatInput from '../../components/ChatInput/ChatInput';
import MessageList from '../../components/MessageList/MessageList';
import { sendChatMessage } from '../../api/chat';
import type { Message } from '../../types';

const { Header, Content } = Layout;
const { Title } = Typography;

/**
 * 聊天主页面。
 *
 * Agent 前端核心逻辑：
 * 1. 维护 messages 状态（用户消息 + AI 回复）
 * 2. 用户发送 -> 先追加 user 消息 -> 调用 API -> 追加 assistant 消息
 * 3. 中间展示 loading，提升体验
 *
 * React Hooks 小知识：
 * - useState：管理组件内部状态，messages 变化会触发重新渲染
 * - useCallback：避免 onSend 每次渲染都变，减少 ChatInput 的无效重渲染
 */
export default function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(false);

  const handleSend = useCallback(async (content: string) => {
    const userMsg: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
    };
    setMessages((prev) => [...prev, userMsg]);
    setLoading(true);

    try {
      const res = await sendChatMessage({ message: content });
      const assistantMsg: Message = {
        id: `assistant-${Date.now()}`,
        role: 'assistant',
        content: res.message,
      };
      setMessages((prev) => [...prev, assistantMsg]);
    } finally {
      setLoading(false);
    }
  }, []);

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ background: '#fff', padding: '0 24px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
        <Title level={4} style={{ margin: '16px 0' }}>
          幼儿园老师 Agent
        </Title>
      </Header>
      <Content
        style={{
          padding: 24,
          maxWidth: 800,
          margin: '0 auto',
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
        }}
      >
        <div style={{ flex: 1, overflow: 'auto', marginBottom: 16 }}>
          <MessageList messages={messages} loading={loading} />
        </div>
        <div style={{ flexShrink: 0 }}>
          <ChatInput onSend={handleSend} disabled={loading} />
        </div>
      </Content>
    </Layout>
  );
}
