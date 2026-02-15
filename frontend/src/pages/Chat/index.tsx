import { useState, useCallback, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { Layout, Typography, List, Button, Popconfirm, message as antMessage } from 'antd';
import { PlusOutlined, MessageOutlined, DeleteOutlined } from '@ant-design/icons';
import ChatInput from '../../components/ChatInput/ChatInput';
import MessageList from '../../components/MessageList/MessageList';
import { sendChatMessageStream } from '../../api/chat';
import {
  listSessions,
  createSession,
  getSession,
  deleteSession,
  type SessionListItem,
} from '../../api/sessions';
import type { Message } from '../../types';

const { Header, Content, Sider } = Layout;
const { Title, Text } = Typography;

/** 将 API 消息转为前端 Message 类型 */
function toMessage(m: { id: number; role: string; content: string }): Message {
  return {
    id: String(m.id),
    role: m.role as 'user' | 'assistant',
    content: m.content,
  };
}

/**
 * 聊天主页面。
 * 支持会话列表、新建/切换/删除会话、加载历史消息。
 */
export default function Chat() {
  const { sessionId: paramSessionId } = useParams<{ sessionId: string }>();
  const navigate = useNavigate();

  const [sessions, setSessions] = useState<SessionListItem[]>([]);
  const [currentSessionId, setCurrentSessionId] = useState<number | null>(
    paramSessionId ? Number(paramSessionId) : null
  );
  const [messages, setMessages] = useState<Message[]>([]);
  const [loading, setLoading] = useState(false);
  const [sessionsLoading, setSessionsLoading] = useState(false);
  const streamingContentRef = useRef('');
  const streamingIdRef = useRef<string | null>(null);

  // 拉取会话列表
  const fetchSessions = useCallback(async () => {
    setSessionsLoading(true);
    try {
      const res = await listSessions(0, 50);
      setSessions(res.content || []);
    } catch (e) {
      antMessage.error('加载会话列表失败');
    } finally {
      setSessionsLoading(false);
    }
  }, []);

  // 根据 URL 或选中会话加载消息
  const loadMessages = useCallback(async (sid: number | null) => {
    if (!sid) {
      setMessages([]);
      return;
    }
    try {
      const detail = await getSession(sid);
      const msgs = (detail.messages || []).map(toMessage);
      setMessages(msgs);
    } catch {
      antMessage.error('加载消息失败');
    }
  }, []);

  useEffect(() => {
    fetchSessions();
  }, [fetchSessions]);

  useEffect(() => {
    const sid = paramSessionId ? Number(paramSessionId) : null;
    setCurrentSessionId(sid);
    loadMessages(sid);
  }, [paramSessionId, loadMessages]);

  const handleNewChat = useCallback(async () => {
    try {
      const s = await createSession('新对话');
      setSessions((prev) => [s, ...prev]);
      setCurrentSessionId(s.id);
      setMessages([]);
      navigate(`/chat/${s.id}`);
    } catch {
      antMessage.error('创建会话失败');
    }
  }, [navigate]);

  const handleSelectSession = useCallback(
    (id: number) => {
      setCurrentSessionId(id);
      navigate(`/chat/${id}`);
      loadMessages(id);
    },
    [navigate, loadMessages]
  );

  const handleDeleteSession = useCallback(
    async (id: number) => {
      try {
        await deleteSession(id);
        setSessions((prev) => prev.filter((s) => s.id !== id));
        if (currentSessionId === id) {
          setCurrentSessionId(null);
          setMessages([]);
          navigate('/chat');
        }
      } catch {
        antMessage.error('删除失败');
      }
    },
    [currentSessionId, navigate]
  );

  const handleSend = useCallback(
    (content: string) => {
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
        {
          message: content,
          session_id: currentSessionId ?? undefined,
          doc_type_id: 'general',
        },
        (chunk) => {
          streamingContentRef.current += chunk;
          setMessages((prev) =>
            prev.map((m) =>
              m.id === assistantId ? { ...m, content: streamingContentRef.current } : m
            )
          );
        },
        (error) => {
          setLoading(false);
          streamingIdRef.current = null;
          if (error) {
            setMessages((prev) =>
              prev.map((m) =>
                m.id === assistantId ? { ...m, content: m.content || `生成失败：${error.message}` } : m
              )
            );
          } else {
            fetchSessions();
          }
        },
        (sessionId) => {
          setCurrentSessionId(sessionId);
          navigate(`/chat/${sessionId}`);
          fetchSessions();
        }
      );
    },
    [currentSessionId, navigate, fetchSessions]
  );

  return (
    <Layout style={{ minHeight: '100vh', background: '#f5f5f5' }}>
      <Sider
        width={240}
        style={{
          background: '#fff',
          boxShadow: '1px 0 4px rgba(0,0,0,0.06)',
          overflow: 'auto',
        }}
      >
        <div style={{ padding: 16, borderBottom: '1px solid #f0f0f0' }}>
          <Button
            type="primary"
            icon={<PlusOutlined />}
            block
            onClick={handleNewChat}
            style={{ borderRadius: 8 }}
          >
            新建对话
          </Button>
        </div>
        <List
          loading={sessionsLoading}
          dataSource={sessions}
          style={{ padding: '8px 0' }}
          renderItem={(s) => (
            <List.Item
              key={s.id}
              style={{
                padding: '10px 16px',
                cursor: 'pointer',
                background: currentSessionId === s.id ? '#e6f4ff' : 'transparent',
                margin: '0 8px',
                borderRadius: 8,
              }}
              actions={[
                <Popconfirm
                  key="del"
                  title="确定删除此会话？"
                  onConfirm={(e) => {
                    e?.stopPropagation();
                    handleDeleteSession(s.id);
                  }}
                  onCancel={(e) => e?.stopPropagation()}
                >
                  <Button
                    type="text"
                    size="small"
                    icon={<DeleteOutlined />}
                    onClick={(e) => e.stopPropagation()}
                  />
                </Popconfirm>,
              ]}
              onClick={() => handleSelectSession(s.id)}
            >
              <List.Item.Meta
                avatar={<MessageOutlined style={{ color: '#1890ff' }} />}
                title={
                  <Text ellipsis style={{ maxWidth: 140 }}>
                    {s.title || '新对话'}
                  </Text>
                }
                description={
                  <Text type="secondary" style={{ fontSize: 12 }}>
                    {new Date(s.updated_at).toLocaleDateString()}
                  </Text>
                }
              />
            </List.Item>
          )}
        />
      </Sider>
      <Layout>
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
          <div
            style={{
              flexShrink: 0,
              background: '#fff',
              padding: 12,
              borderRadius: 12,
              boxShadow: '0 1px 2px rgba(0,0,0,0.06)',
            }}
          >
            <ChatInput onSend={handleSend} disabled={loading} />
          </div>
        </Content>
      </Layout>
    </Layout>
  );
}
