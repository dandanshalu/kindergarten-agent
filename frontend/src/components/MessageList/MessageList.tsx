import { useEffect, useRef } from 'react';
import ReactMarkdown from 'react-markdown';
import remarkGfm from 'remark-gfm';
import { Card, Typography, Space } from 'antd';
import { UserOutlined, RobotOutlined } from '@ant-design/icons';
import type { Message } from '../../types';

const { Text } = Typography;

/** 消息气泡通用样式：圆角、内边距、最大宽度，保证换行与可读性 */
const messageBubbleStyle: React.CSSProperties = {
  maxWidth: '85%',
  borderRadius: 12,
  boxShadow: '0 1px 2px rgba(0,0,0,0.06)',
  overflow: 'hidden',
};

/** 消息正文容器：行高与字号 */
const messageBodyStyle: React.CSSProperties = {
  lineHeight: 1.7,
  fontSize: 15,
  marginTop: 6,
  minHeight: 20,
};

/** Markdown 渲染用组件：标题、列表、加粗、分隔线等样式 */
const markdownComponents: React.ComponentProps<typeof ReactMarkdown>['components'] = {
  h1: ({ node, ...p }) => <h1 style={{ fontSize: 20, fontWeight: 700, margin: '12px 0 8px', lineHeight: 1.4 }} {...p} />,
  h2: ({ node, ...p }) => <h2 style={{ fontSize: 18, fontWeight: 700, margin: '12px 0 6px', lineHeight: 1.4 }} {...p} />,
  h3: ({ node, ...p }) => <h3 style={{ fontSize: 17, fontWeight: 600, margin: '10px 0 6px', lineHeight: 1.4 }} {...p} />,
  h4: ({ node, ...p }) => <h4 style={{ fontSize: 16, fontWeight: 600, margin: '8px 0 4px', lineHeight: 1.4 }} {...p} />,
  h5: ({ node, ...p }) => <h5 style={{ fontSize: 15, fontWeight: 600, margin: '6px 0 4px' }} {...p} />,
  h6: ({ node, ...p }) => <h6 style={{ fontSize: 14, fontWeight: 600, margin: '6px 0 2px' }} {...p} />,
  p: ({ node, ...p }) => <p style={{ margin: '6px 0', wordBreak: 'break-word' }} {...p} />,
  ul: ({ node, ...p }) => <ul style={{ margin: '6px 0', paddingLeft: 22 }} {...p} />,
  ol: ({ node, ...p }) => <ol style={{ margin: '6px 0', paddingLeft: 22 }} {...p} />,
  li: ({ node, ...p }) => <li style={{ marginBottom: 2 }} {...p} />,
  strong: ({ node, ...p }) => <strong style={{ fontWeight: 600 }} {...p} />,
  hr: ({ node, ...p }) => <hr style={{ border: 'none', borderTop: '1px solid #e8e8e8', margin: '12px 0' }} {...p} />,
  blockquote: ({ node, ...p }) => <blockquote style={{ margin: '8px 0', paddingLeft: 14, borderLeft: '4px solid #d9d9d9', color: '#595959' }} {...p} />,
  code: ({ node, className, ...p }) => (
    <code
      style={{
        background: className ? '#f5f5f5' : '#f0f0f0',
        padding: className ? '10px 12px' : '2px 6px',
        borderRadius: 4,
        fontSize: 14,
        display: className ? 'block' : 'inline',
        overflow: 'auto',
      }}
      {...p}
    />
  ),
  pre: ({ node, ...p }) => <pre style={{ margin: '8px 0', overflow: 'auto' }} {...p} />,
};

interface MessageListProps {
  messages: Message[];
  loading?: boolean;
}

/**
 * 消息列表组件。
 * - 用户消息靠右，AI 消息靠左；气泡样式区分
 * - AI 助手消息使用 Markdown 渲染（标题、加粗、列表、分隔线、代码块等）
 * - 用户消息为纯文本，保留换行
 * - 自动滚动到底部
 */
export default function MessageList({ messages, loading }: MessageListProps) {
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  if (messages.length === 0 && !loading) {
    return (
      <div style={{ textAlign: 'center', color: '#8c8c8c', padding: 48 }}>
        <Text type="secondary" style={{ fontSize: 15 }}>
          你好，我是幼儿园老师的智能助手。你可以告诉我需要什么类型的文案，例如：
        </Text>
        <ul style={{ textAlign: 'left', display: 'inline-block', marginTop: 20, lineHeight: 2 }}>
          <li>帮我写一份小班家长会发言稿</li>
          <li>设计一个中班科学活动：认识水果</li>
          <li>写一份幼儿园安全教育方案</li>
        </ul>
      </div>
    );
  }

  return (
    <Space direction="vertical" style={{ width: '100%', gap: 20 }}>
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
              ...messageBubbleStyle,
              backgroundColor: msg.role === 'user' ? '#e6f4ff' : '#fafafa',
              border: msg.role === 'user' ? '1px solid #bae0ff' : '1px solid #f0f0f0',
            }}
            bodyStyle={{ padding: '12px 16px' }}
          >
            <Space align="start" size={10}>
              <span style={{ color: '#1890ff', fontSize: 18 }}>
                {msg.role === 'assistant' ? <RobotOutlined /> : <UserOutlined />}
              </span>
              <div style={{ flex: 1, minWidth: 0 }}>
                <Text strong style={{ fontSize: 14 }}>
                  {msg.role === 'user' ? '你' : 'AI 助手'}
                </Text>
                <div style={messageBodyStyle}>
                  {msg.role === 'assistant' ? (
                    <ReactMarkdown remarkPlugins={[remarkGfm]} components={markdownComponents}>
                      {msg.content || '\u00A0'}
                    </ReactMarkdown>
                  ) : (
                    <span style={{ whiteSpace: 'pre-wrap', wordBreak: 'break-word' }}>
                      {msg.content || '\u00A0'}
                    </span>
                  )}
                </div>
              </div>
            </Space>
          </Card>
        </div>
      ))}
      {loading && (
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <Card
            size="small"
            style={{ ...messageBubbleStyle, backgroundColor: '#fafafa', border: '1px solid #f0f0f0' }}
            bodyStyle={{ padding: '12px 16px' }}
          >
            <Space size={10}>
              <RobotOutlined spin style={{ color: '#1890ff', fontSize: 18 }} />
              <Text type="secondary">正在思考...</Text>
            </Space>
          </Card>
        </div>
      )}
      <div ref={bottomRef} />
    </Space>
  );
}
