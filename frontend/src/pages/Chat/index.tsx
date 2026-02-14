import { useState, useCallback, useRef } from 'react';
import { Layout, Typography } from 'antd';
import ChatInput from '../../components/ChatInput/ChatInput';
import MessageList from '../../components/MessageList/MessageList';
import { sendChatMessageStream } from '../../api/chat';
import type { Message } from '../../types';

const { Header, Content } = Layout;
const { Title } = Typography;

/**
 * 聊天主页面。
 *
 * 使用流式接口：先追加一条空的 assistant 消息，用 ref 累积内容并驱动 setState，避免快速到达的多个 chunk 因闭包/批处理导致丢失。
 */
export default function Chat() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(false);
  const streamingContentRef = useRef('');
  const streamingIdRef = useRef<string | null>(null);

  const handleSend = useCallback((content: string) => {
    const userMsg: Message = {
      id: `user-${Date.now()}`,
      role: 'user',
      content,
    };
    setMessages((prev) => [...prev, userMsg]);

    const assistantId = `assistant-${Date.now()}`;
    streamingIdRef.current = assistantId;
    streamingContentRef.current = '';
    const assistantMsg: Message = {
      id: assistantId,
      role: 'assistant',
      content: '',
    };
    setMessages((prev) => [...prev, assistantMsg]);
    setLoading(true);

    sendChatMessageStream(
      { message: content },
      (chunk) => {
        streamingContentRef.current += chunk;
        setMessages((prev) =>
          prev.map((m) =>
            m.id === assistantId
              ? { ...m, content: streamingContentRef.current }
              : m
          )
        );
      },
      (error) => {
        setLoading(false);
        streamingIdRef.current = null;
        if (error) {
          setMessages((prev) =>
            prev.map((m) =>
              m.id === assistantId
                ? { ...m, content: m.content || `生成失败：${error.message}` }
                : m
            )
          );
        }
      }
    );
  }, []);

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      <Header style={{ background: '#fff', padding: '0 24px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
        <Title level={4} style={{ margin: '16px 0', color: '#1f1f1f' }}>
          幼儿园老师 Agent
        </Title>
      </Header>
      <Content
        style={{
          padding: '20px 24px 24px',
          maxWidth: 800,
          margin: '0 auto',
          width: '100%',
          display: 'flex',
          flexDirection: 'column',
          minHeight: 'calc(100vh - 64px)',
        }}
      >
        <div
          style={{
            flex: 1,
            minHeight: 280,
            overflow: 'auto',
            marginBottom: 20,
            padding: '8px 0',
          }}
        >
          <MessageList messages={messages} loading={loading} />
        </div>
        <div style={{ flexShrink: 0, background: '#fff', padding: 12, borderRadius: 12, boxShadow: '0 1px 2px rgba(0,0,0,0.06)' }}>
          <ChatInput onSend={handleSend} disabled={loading} />
        </div>
      </Content>
    </Layout>
  );
}
