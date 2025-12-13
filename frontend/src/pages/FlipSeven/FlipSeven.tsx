import React from 'react';
import { Card, Typography, Button, Row, Col } from 'antd';
import { HomeOutlined, PlusOutlined, MinusOutlined } from '@ant-design/icons';
import MainLayout from '../../components/MainLayout';
import ChatWindow from '../../components/Chat/ChatWindow';
import { useFlipSeven } from './useFlipSeven';

const { Title, Text } = Typography;

const FlipSeven: React.FC = () => {
    const {
        gameId,
        user,
        connected,
        messages,
        chatInput,
        setChatInput,
        sendMessage,
        navigate,
        gameState,
        sendGameAction
    } = useFlipSeven();

    if (!gameId) return <div>No Game ID</div>;

    return (
        <MainLayout>
            <div style={{ padding: '20px', height: '100vh', display: 'flex', flexDirection: 'column' }}>
                <Title level={2} style={{ color: 'white' }}>Flip Seven - Game {gameId}</Title>

                <Row gutter={24} style={{ flex: 1 }}>
                    {/* Game Area (Top/Center) */}
                    <Col span={20}>
                        <Card style={{ height: '100%', display: 'flex', justifyContent: 'center', alignItems: 'center', flexDirection: 'column' }}>
                            <Title level={3}>Game Counter</Title>

                            <div style={{ display: 'flex', alignItems: 'center', gap: '20px', margin: '20px 0' }}>
                                <Button
                                    type="primary"
                                    shape="circle"
                                    icon={<MinusOutlined />}
                                    size="large"
                                    onClick={() => sendGameAction('DECREMENT')}
                                    disabled={!connected}
                                />
                                <Text style={{ fontSize: '3rem', fontWeight: 'bold' }}>
                                    {gameState?.counter || 0}
                                </Text>
                                <Button
                                    type="primary"
                                    shape="circle"
                                    icon={<PlusOutlined />}
                                    size="large"
                                    onClick={() => sendGameAction('INCREMENT')}
                                    disabled={!connected}
                                />
                            </div>

                            <Button icon={<HomeOutlined />} onClick={() => navigate('/')} style={{ marginTop: '2rem' }}>
                                Exit Game
                            </Button>
                        </Card>
                    </Col>

                    {/* Chat and Scoreboard Area */}
                    <Col span={4} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                        <div style={{ flex: 1, display: 'flex', flexDirection: 'column', marginBottom: '10px' }}>
                            <ChatWindow
                                messages={messages}
                                chatInput={chatInput}
                                setChatInput={setChatInput}
                                sendMessage={sendMessage}
                                connected={connected}
                                user={user}
                            />
                        </div>
                        <Card title="Scores" size="small" style={{ flex: '0 0 auto' }} bodyStyle={{ padding: '0 10px' }}>
                            {gameState?.scores ? (
                                <ul style={{ listStyle: 'none', padding: 0 }}>
                                    {Object.entries(gameState.scores).map(([player, score]) => (
                                        <li key={player} style={{ display: 'flex', justifyContent: 'space-between', padding: '5px 0', borderBottom: '1px solid #f0f0f0' }}>
                                            <span>{player}</span>
                                            <strong>{String(score)}</strong>
                                        </li>
                                    ))}
                                </ul>
                            ) : <Text>No scores yet</Text>}
                        </Card>
                    </Col>
                </Row>
            </div>
        </MainLayout>
    );
};

export default FlipSeven;
