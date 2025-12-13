import React from 'react';
import { Card, Typography, Spin, Button, Tag, List, Avatar, Row, Col } from 'antd';
import { CopyOutlined, HomeOutlined, UserOutlined } from '@ant-design/icons';
import MainLayout from '../../components/MainLayout';
import './Lobby.css';
import ChatWindow from '../../components/Chat/ChatWindow';
import { useLobby } from './useLobby';

const { Title, Text } = Typography;

const Lobby: React.FC = () => {
    const {
        game,
        loading,
        error,
        connected,
        messages,
        chatInput,
        setChatInput,
        players,
        sendMessage,
        handleLaunchGame,
        copyCode,
        user,
        navigate
    } = useLobby();

    if (loading) {
        return (
            <MainLayout>
                <div className="lobby-container-content">
                    <Spin size="large" tip="Loading Lobby..." />
                </div>
            </MainLayout>
        );
    }

    if (error) {
        return (
            <MainLayout>
                <div className="lobby-container-content">
                    <Card className="lobby-card-antd" bordered={false}>
                        <Title level={2} type="danger">Error</Title>
                        <Text style={{ fontSize: '1.2rem', display: 'block', marginBottom: '2rem' }}>
                            {error}
                        </Text>
                        <Button type="primary" icon={<HomeOutlined />} onClick={() => navigate('/')}>
                            Return Home
                        </Button>
                    </Card>
                </div>
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <div className="lobby-container-content" style={{ maxWidth: '1200px' }}>
                <Row gutter={24} style={{ width: '100%' }}>
                    {/* Left Column: Game Info & Players */}
                    <Col xs={24} md={12}>
                        <Card className="lobby-card-antd" bordered={false} style={{ height: '100%' }}>
                            <Title level={1} style={{ marginBottom: '0.5rem' }}>Lobby</Title>
                            <Tag color="blue" style={{ marginBottom: '2rem' }}>{game?.status}</Tag>

                            <div style={{ marginBottom: '2rem' }}>
                                <Text type="secondary" style={{ fontSize: '1.2rem', display: 'block' }}>
                                    GAME CODE
                                </Text>
                                <div className="game-code-display" onClick={copyCode} style={{ cursor: 'pointer', textAlign: 'center' }}>
                                    <Title level={1} style={{ color: '#ffd700', margin: 0, letterSpacing: '5px' }}>
                                        {game?.gameCode} <CopyOutlined style={{ fontSize: '20px', verticalAlign: 'middle' }} />
                                    </Title>
                                </div>
                            </div>

                            <div className="players-section">
                                <Title level={3}>Players ({players.length})</Title>
                                <List
                                    itemLayout="horizontal"
                                    dataSource={players}
                                    renderItem={(player) => (
                                        <List.Item>
                                            <List.Item.Meta
                                                avatar={<Avatar icon={<UserOutlined />} style={{ backgroundColor: '#87d068' }} />}
                                                title={player}
                                                description={player === user?.username ? "(You)" : "Ready"}
                                            />
                                        </List.Item>
                                    )}
                                />
                                {players.length === 0 && (
                                    <Text type="secondary" style={{ fontStyle: 'italic' }}>
                                        Waiting for players to join...
                                    </Text>
                                )}
                            </div>

                            {/* Launch Game Button - Host Only state check */}
                            {game?.hostUserId === user?.id && (
                                <div style={{ marginTop: '2rem', textAlign: 'center' }}>
                                    <Button
                                        type="primary"
                                        size="large"
                                        onClick={handleLaunchGame}
                                        style={{ width: '100%', height: '50px', fontSize: '1.2rem' }}
                                    >
                                        Launch Game
                                    </Button>
                                    <Text type="secondary" style={{ display: 'block', marginTop: '10px' }}>
                                        Only you can start the game.
                                    </Text>
                                </div>
                            )}
                        </Card>
                    </Col>

                    {/* Right Column: Chat */}
                    <Col xs={24} md={12}>
                        <ChatWindow
                            messages={messages}
                            chatInput={chatInput}
                            setChatInput={setChatInput}
                            sendMessage={sendMessage}
                            connected={connected}
                            user={user}
                        />
                    </Col>
                </Row>
            </div>
        </MainLayout>
    );
};

export default Lobby;
