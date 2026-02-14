import { useEffect, useRef } from 'react';
import { Card, Typography, Space } from 'antd';
import { UserOutlined, RobotOutlined } from '@ant-design/icons';
import type { Message } from '../../types';

const { Text } = Typography;

interface MessageListProps {
  messages: Message[];
  loading?: boolean;
}

/**
 * 消息列表组件。
 * - 用户消息靠右，AI 消息靠左
 * - 自动滚动到底部，便于查看最新回复
 *
 * React Hooks 小知识：
 * - useRef：保存 DOM 或可变值，变更不触发重渲染
 * - 这里用 ref 拿到容器 DOM，在 messages 变化时执行 scrollIntoView 滚动到底部
 */
export default function MessageList({ messages, loading }: MessageListProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  if (messages.length === 0 && !loading) {
    return (
      <div style={{ textAlign: 'center', color: '#999', padding: 48 }}>
        <Text type="secondary">
          你好，我是幼儿园老师的智能助手。你可以告诉我需要什么类型的文案，例如：
        </Text>
        <ul style={{ textAlign: 'left', display: 'inline-block', marginTop: 16 }}>
          <li>帮我写一份小班家长会发言稿</li>
          <li>设计一个中班科学活动：认识水果</li>
          <li>写一份幼儿园安全教育方案</li>
        </ul>
      </div>
    );
  }

  return (
    <Space direction="vertical" style={{ width: '100%', gap: 16 }}>
      {messages.map((msg) => (
        <div
          key={msg.id}
          style={{
            display: 'flex',
            justifyContent: msg.role === 'user' ? 'flex-end' : 'flex-start',
          }}
        >
          <Card
            size="small"
            style={{
              maxWidth: '80%',
              backgroundColor: msg.role === 'user' ? '#e6f4ff' : '#f5f5f5',
            }}
          >
            <Space align="start">
              {msg.role === 'assistant' ? <RobotOutlined /> : <UserOutlined />}
              <div>
                <Text strong>{msg.role === 'user' ? '你' : 'AI 助手'}</Text>
                <div style={{ whiteSpace: 'pre-wrap', marginTop: 4 }}>{msg.content}</div>
              </div>
            </Space>
          </Card>
        </div>
      ))}
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <Card size="small" style={{ maxWidth: '80%', backgroundColor: '#f5f5f5' }}>
            <Space>
              <RobotOutlined spin />
              <Text type="secondary">正在思考...</Text>
            </Space>
          </Card>
        </div>
      )}
      <div ref={bottomRef} />
    </Space>
  );
}
