import React, { useEffect, useState } from 'react';
import { Typography, Button, Row, Col, Card, Avatar, Tag, Divider, Modal } from 'antd';
import { HomeOutlined, UserOutlined, HeartFilled } from '@ant-design/icons';
import MainLayout from '../../components/MainLayout';
import ChatWindow from '../../components/Chat/ChatWindow';
import { useFlipSeven } from './useFlipSeven';
import type { FlipSevenGameState, FlipSevenPlayer } from '../../types/flipSeven';
import CardComponent from '../../components/FlipSeven/CardComponent';

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

    const [game, setGame] = useState<FlipSevenGameState | null>(null);

    useEffect(() => {
        if (gameState && gameState.gameState) {
            setGame(gameState.gameState as FlipSevenGameState);
        }
    }, [gameState]);

    if (!gameId) return <div>No Game ID</div>;

    const getMyPlayer = () => {
        return game?.players.find(p => p.username === user?.username);
    };

    const isMyTurn = () => {
        if (!game || !user) return false;
        const currentPlayer = game.players[game.currentPlayerIndex];
        return currentPlayer && currentPlayer.username === user.username;
    };

    const myPlayer = getMyPlayer();

    const handlePlayerClick = (targetUsername: string) => {
        if (game?.pendingActionType && game.pendingActionInitiator === user?.username) {
            sendGameAction('SELECT_TARGET', { target: targetUsername });
        }
    };

    const isSelectingTarget = !!(game?.pendingActionType && game.pendingActionInitiator === user?.username);

    const isReady = game?.readyPlayers?.includes(user?.username || '');

    const handleReady = () => {
        sendGameAction('PLAYER_READY', {});
    };

    return (
        <MainLayout>
            <div style={{ padding: '20px', height: 'calc(100vh - 64px)', display: 'flex', flexDirection: 'column' }}>
                <Row gutter={24} style={{ flex: 1, overflow: 'hidden' }}>

                    {/* LEFT: Game Board */}
                    <Col xs={24} md={18} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>

                        {/* 1. Opponents Area (Top) */}
                        <div style={{ flex: '0 0 auto', display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '10px', marginBottom: '10px' }}>
                            {game?.players.filter(p => p.username !== user?.username).map(p => (
                                <Card
                                    key={p.username}
                                    size="small"
                                    onClick={() => isSelectingTarget && handlePlayerClick(p.username)}
                                    style={{
                                        minWidth: 200,
                                        background: p.roundActive ? '#fff' : '#f0f0f0',
                                        border: game.currentPlayerIndex === game.players.indexOf(p) ? '2px solid #1890ff' : '1px solid #d9d9d9',
                                        cursor: isSelectingTarget ? 'pointer' : 'default',
                                        boxShadow: isSelectingTarget ? '0 0 8px #1890ff' : 'none'
                                    }}
                                >
                                    <Card.Meta
                                        avatar={<Avatar icon={<UserOutlined />} />}
                                        title={
                                            <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                                                <span>
                                                    {p.username}
                                                    {p.hasSecondChance && <HeartFilled style={{ color: 'red', marginLeft: '5px' }} />}
                                                </span>
                                                <Tag color="blue">{p.totalScore} pts</Tag>
                                            </div>
                                        }
                                        description={p.roundActive ? "In Round" : "Banked/Bust"}
                                    />
                                    <div style={{ marginTop: '10px' }}>
                                        <Text strong>Current Hand ({p.roundScore} pts)</Text>
                                        <div style={{ display: 'flex', flexWrap: 'wrap', marginTop: '5px' }}>
                                            {p.hand.map((c, i) => <CardComponent key={c.id || i} card={c} size="small" />)}
                                        </div>
                                    </div>
                                </Card>
                            ))}
                        </div>

                        {/* 2. Center Action Area / Deck Info */}
                        <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', background: 'rgba(255,255,255,0.05)', borderRadius: '12px', marginBottom: '10px', flexDirection: 'column' }}>
                            <Title level={4} style={{ color: 'white' }}>Round Info</Title>
                            {game && (
                                <>
                                    <Text style={{ color: 'white', fontSize: '1.2rem' }}>
                                        Current Turn: <Tag color="geekblue" style={{ fontSize: '1.2rem', padding: '5px 10px' }}>{game.players[game.currentPlayerIndex]?.username}</Tag>
                                    </Text>
                                    {game.pendingActionType && (
                                        <div style={{ marginTop: '20px', padding: '20px', background: 'rgba(220, 0, 0, 0.6)', borderRadius: '8px', border: '2px solid red', textAlign: 'center' }}>
                                            <Title level={3} style={{ color: 'white', margin: 0 }}>
                                                {game.pendingActionInitiator === user?.username ? "SELECT A PLAYER!" : `${game.pendingActionInitiator} IS SELECTING...`}
                                            </Title>
                                            <Text style={{ color: 'white', fontWeight: 'bold' }}>Effect: {game.pendingActionType.replace('_SELECTION', '')}</Text>
                                        </div>
                                    )}
                                </>
                            )}
                            {!game && <Text style={{ color: 'white' }}>Waiting for game state...</Text>}
                        </div>

                        {/* 3. My Player Area (Bottom) */}
                        <div
                            style={{
                                flex: '0 0 auto',
                                background: 'white',
                                borderRadius: '12px',
                                padding: '15px',
                                border: isSelectingTarget ? '4px solid #1890ff' : 'none',
                                cursor: isSelectingTarget ? 'pointer' : 'default'
                            }}
                            onClick={() => isSelectingTarget && myPlayer && handlePlayerClick(myPlayer.username)}
                        >
                            {myPlayer ? (
                                <div>
                                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
                                        <div>
                                            <Title level={3} style={{ margin: 0 }}>
                                                My Hand
                                                {myPlayer.hasSecondChance && <HeartFilled style={{ color: 'red', marginLeft: '10px' }} />}
                                            </Title>
                                            <Text type="secondary">Total Score: {myPlayer.totalScore}</Text>
                                        </div>
                                        <div style={{ textAlign: 'right' }}>
                                            <Title level={2} style={{ margin: 0, color: '#52c41a' }}>{myPlayer.roundScore} <span style={{ fontSize: '1rem', color: '#888' }}>points this round</span></Title>
                                            {!myPlayer.roundActive && <Tag color="red">ROUND OVER</Tag>}
                                        </div>
                                    </div>

                                    <div style={{ display: 'flex', gap: '10px', overflowX: 'auto', padding: '10px 0', minHeight: '130px', alignItems: 'center' }}>
                                        {myPlayer.hand.length === 0 && <Text type="secondary">No cards</Text>}
                                        {myPlayer.hand.map((c, i) => (
                                            <CardComponent key={c.id || i} card={c} />
                                        ))}
                                    </div>

                                    <Divider />

                                    <div style={{ display: 'flex', justifyContent: 'center', gap: '20px' }}>
                                        <Button
                                            type="primary"
                                            size="large"
                                            style={{ minWidth: '120px', height: '50px', fontSize: '1.2rem', background: '#52c41a' }}
                                            disabled={!isMyTurn() || !myPlayer.roundActive || isSelectingTarget}
                                            onClick={() => sendGameAction('HIT')}
                                        >
                                            HIT (Draw)
                                        </Button>
                                        <Button
                                            type="default"
                                            size="large"
                                            danger
                                            style={{ minWidth: '120px', height: '50px', fontSize: '1.2rem' }}
                                            disabled={!isMyTurn() || !myPlayer.roundActive || isSelectingTarget}
                                            onClick={() => sendGameAction('STAY')}
                                        >
                                            STAY (Bank)
                                        </Button>
                                    </div>
                                </div>
                            ) : (
                                <div style={{ textAlign: 'center', padding: '20px' }}>
                                    <Text>You are not in the player list. Spectating?</Text>
                                    <Button onClick={() => window.location.reload()}>Refresh</Button>
                                </div>
                            )}
                        </div>
                    </Col>

                    {/* RIGHT: Chat & Scoreboard */}
                    <Col xs={24} md={6} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <div style={{ flex: 1, minHeight: 0, marginBottom: '10px' }}>
                            <ChatWindow
                                messages={messages}
                                chatInput={chatInput}
                                setChatInput={setChatInput}
                                sendMessage={sendMessage}
                                connected={connected}
                                user={user}
                                style={{ height: '100%' }}
                            />
                        </div>

                        <Card title="Scoreboard (Goal: 200)" size="small" style={{ flex: '0 0 auto', marginBottom: '10px' }} bodyStyle={{ padding: '0 10px', maxHeight: '200px', overflowY: 'auto' }}>
                            {game?.players ? (
                                <ul style={{ listStyle: 'none', padding: 0 }}>
                                    {[...game.players].sort((a, b) => b.totalScore - a.totalScore).map(p => (
                                        <li key={p.username} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #f0f0f0', alignItems: 'center' }}>
                                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                                {p.totalScore >= 200 && <span>🏆</span>}
                                                <span style={{ fontWeight: p.username === user?.username ? 'bold' : 'normal' }}>{p.username}</span>
                                            </div>
                                            <Tag color={p.totalScore >= 200 ? 'gold' : 'blue'}>{p.totalScore}</Tag>
                                        </li>
                                    ))}
                                </ul>
                            ) : <Text>No scores yet</Text>}
                        </Card>

                        <div style={{ textAlign: 'center' }}>
                            <Button icon={<HomeOutlined />} onClick={() => navigate('/')} block>
                                Exit Game
                            </Button>
                        </div>
                    </Col>
                </Row>
            </div>

            <Modal
                title="Round Finished"
                open={game?.isRoundOver && !game?.isGameOver}
                footer={[
                    <Button
                        key="ready"
                        type="primary"
                        onClick={handleReady}
                        disabled={isReady}
                        loading={isReady}
                    >
                        {isReady ? `Waiting for others (${game?.readyPlayers?.length || 0}/${game?.players.length})` : 'Ready for Next Round'}
                    </Button>
                ]}
                closable={false}
                maskClosable={false}
                centered
                width={800}
            >
                <div style={{ textAlign: 'center' }}>
                    <Title level={4}>Your Round Score: <span style={{ color: '#1890ff' }}>{myPlayer?.lastRoundScore} pts</span></Title>
                    <Title level={5}>Total Score: {myPlayer?.totalScore} pts</Title>

                    <Divider>Your Hand</Divider>

                    <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '10px', marginTop: '20px' }}>
                        {myPlayer?.hand.map((card, idx) => (
                            <div key={idx} style={{ transform: 'scale(1)' }}>
                                <CardComponent card={card} />
                            </div>
                        ))}
                    </div>
                </div>
            </Modal>

            <Modal
                title="🏆 Game Finished 🏆"
                open={game?.isGameOver}
                footer={[
                    <Button
                        key="exit"
                        type="primary"
                        onClick={() => navigate('/')}
                    >
                        Return to Lobby
                    </Button>
                ]}
                closable={false}
                maskClosable={false}
                centered
            >
                <div style={{ textAlign: 'center' }}>
                    <Title level={2}>Winner: <span style={{ color: '#ffd700' }}>{game?.winner}</span></Title>
                    <Title level={4}>Congratulations!</Title>
                    <div style={{ fontSize: '40px' }}>🎉🎉🎉</div>
                </div>
            </Modal>
        </MainLayout>
    );
};

export default FlipSeven;
