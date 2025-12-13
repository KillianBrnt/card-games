import React, { useRef, useEffect } from 'react';
import { Card, List, Input, Button, theme } from 'antd';
import { SendOutlined } from '@ant-design/icons';
import type { User } from '../../types/auth'; // Assuming User type is here or similar

interface ChatMessage {
    sender: string;
    content?: string;
    type: string;
    payload?: any;
}

interface ChatWindowProps {
    messages: ChatMessage[];
    chatInput: string;
    setChatInput: (val: string) => void;
    sendMessage: () => void;
    connected: boolean;
    user: User | null;
    style?: React.CSSProperties;
}

const ChatWindow: React.FC<ChatWindowProps> = ({ messages, chatInput, setChatInput, sendMessage, connected, user, style }) => {
    const messagesEndRef = useRef<HTMLDivElement>(null);
    const { token } = theme.useToken();

    const scrollToBottom = () => {
        messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
    };

    useEffect(() => {
        scrollToBottom();
    }, [messages]);

    return (
        <Card className="lobby-card-antd" bordered={false} title="Chat" style={{ height: '600px', display: 'flex', flexDirection: 'column', ...style }} bodyStyle={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
            <div style={{ flex: 1, overflowY: 'auto', marginBottom: '1rem', paddingRight: '10px' }}>
                <List
                    dataSource={messages}
                    renderItem={(msg) => (
                        <div style={{
                            marginBottom: '10px',
                            textAlign: msg.sender === user?.username ? 'right' : 'left'
                        }}>
                            <div style={{ fontWeight: 'bold', fontSize: '0.8rem', color: token.colorTextSecondary }}>{msg.sender}</div>
                            <div style={{
                                display: 'inline-block',
                                padding: '8px 12px',
                                borderRadius: '12px',
                                background: msg.sender === user?.username ? '#1890ff' : '#f0f2f5',
                                color: msg.sender === user?.username ? 'white' : 'black',
                                maxWidth: '80%'
                            }}>
                                {msg.content}
                            </div>
                        </div>
                    )}
                />
                <div ref={messagesEndRef} />
            </div>
            <div style={{ display: 'flex', gap: '8px' }}>
                <Input
                    placeholder="Type a message..."
                    value={chatInput}
                    onChange={(e) => setChatInput(e.target.value)}
                    onPressEnter={sendMessage}
                    disabled={!connected}
                />
                <Button type="primary" icon={<SendOutlined />} onClick={sendMessage} disabled={!connected} />
            </div>
        </Card>
    );
};

export default ChatWindow;
