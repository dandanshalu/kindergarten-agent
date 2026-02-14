import { useState, useCallback, type FormEvent } from 'react';
import { Input, Button, message } from 'antd';
import { SendOutlined } from '@ant-design/icons';

const { TextArea } = Input;

interface ChatInputProps {
  onSend: (content: string) => void;
  disabled?: boolean;
  placeholder?: string;
}

/**
 * 聊天输入组件。
 * - 支持多行输入，Ctrl+Enter 发送
 * - 发送时置灰并 loading
 *
 * React Hooks 小知识：
 * - useCallback：缓存函数引用，避免子组件因父函数引用变化而重渲染
 * - 依赖数组 [onSend, disabled] 为空时，函数在组件生命周期内不变
 */
export default function ChatInput({
  onSend,
  disabled = false,
  placeholder = '输入你的需求，例如：帮我写一份小班家长会发言稿',
}: ChatInputProps) {
  const [value, setValue] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = useCallback(
    async (e?: FormEvent) => {
      e?.preventDefault();
      const trimmed = value.trim();
      if (!trimmed || loading || disabled) return;

      setLoading(true);
      try {
        await onSend(trimmed);
        setValue('');
      } catch (err) {
        message.error(err instanceof Error ? err.message : '发送失败');
      } finally {
        setLoading(false);
      }
    },
    [value, loading, disabled, onSend],
  );

  const handleKeyDown = (e: React.KeyboardEvent) => {
    // Ctrl+Enter 或 Cmd+Enter 发送
    if (e.key === 'Enter' && (e.ctrlKey || e.metaKey)) {
      e.preventDefault();
      handleSubmit();
    }
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'flex', gap: 12, alignItems: 'flex-end' }}>
      <TextArea
        value={value}
        onChange={(e) => setValue(e.target.value)}
        onKeyDown={handleKeyDown}
        placeholder={placeholder}
        autoSize={{ minRows: 2, maxRows: 6 }}
        disabled={disabled || loading}
        style={{ flex: 1, borderRadius: 8 }}
      />
      <Button
        type="primary"
        icon={<SendOutlined />}
        onClick={() => handleSubmit()}
        loading={loading}
        disabled={!value.trim() || disabled}
        style={{ borderRadius: 8, height: 40 }}
      >
        发送
      </Button>
    </form>
  );
}
