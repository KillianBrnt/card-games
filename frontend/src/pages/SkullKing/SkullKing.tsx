import React from 'react';
import { useSelector } from 'react-redux';
import { Row, Col, Button, Typography, Spin } from 'antd';
import { HomeOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import type { RootState } from '../../store';
import { useSkullKing } from './useSkullKing';
import MainLayout from '../../components/MainLayout';
import ChatWindow from '../../components/Chat/ChatWindow';

// Components
import OpponentsArea from './components/OpponentsArea';
import TrickArea from './components/TrickArea';
import PlayerControlArea from './components/PlayerControlArea';
import BidModal from './components/BidModal';
import RoundOverModal from './components/RoundOverModal';
import GameOverModal from './components/GameOverModal';

import './SkullKing.css';

const { Title, Text } = Typography;

const SkullKing: React.FC = () => {
    // Hooks
    const { user } = useSelector((state: RootState) => state.auth);
    const navigate = useNavigate();
    const {
        gameState,
        sendBid,
        playCard,
        sendReady,
        isLoading,
        messages,
        chatInput,
        setChatInput,
        sendMessage,
        connected
    } = useSkullKing();

    if (isLoading || !gameState) {
        return (
            <MainLayout>
                <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '100%' }}>
                    <Spin size="large" tip="Loading Game..." />
                </div>
            </MainLayout>
        );
    }

    const myPlayer = gameState.players.find(p => p.username === user?.username);
    const otherPlayers = gameState.players.filter(p => p.username !== user?.username);

    // Calculate trick cards
    const activeTrickCards = gameState.players
        .filter(p => p.cardPlayed)
        .map(p => ({ player: p, card: p.cardPlayed! }));

    const isMyTurn = gameState.phase === 'PLAYING' &&
        gameState.players[gameState.currentPlayerIndex].username === user?.username;

    const iAmReady = gameState.readyPlayers && gameState.readyPlayers.includes(user?.username || '');

    const trickStarterUsername = gameState.players[gameState.trickStarterIndex]?.username;

    return (
        <MainLayout>
            <div style={{
                padding: '20px',
                height: 'calc(100vh - 64px)',
                display: 'flex',
                flexDirection: 'column',
            }}>
                <Row gutter={24} style={{ flex: 1 }}>
                    {/* LEFT: Game Board */}
                    <Col xs={24} md={18} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>

                        {/* Game Header Info */}
                        <div style={{
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center',
                            marginBottom: '1rem',
                            padding: '0.5rem 1rem',
                            background: 'rgba(0,0,0,0.5)',
                            borderRadius: '8px',
                            color: 'white'
                        }}>
                            <div>
                                <Title level={4} style={{ margin: 0, color: 'white' }}>Skull King</Title>
                                <Text style={{ color: '#d9d9d9' }}>Round {gameState.roundNumber} / 10</Text>
                            </div>
                            <div className="sk-phase">
                                Phase: <span>{gameState.phase === 'TRICK_OVER' ? 'Turn Complete' : gameState.phase}</span>
                            </div>
                            {gameState.winner && (
                                <div className="sk-winner">Winner: {gameState.winner}</div>
                            )}
                        </div>

                        {/* Opponents */}
                        <div style={{ flex: '0 0 auto', marginBottom: '1rem' }}>
                            <OpponentsArea
                                players={otherPlayers}
                                starterUsername={trickStarterUsername}
                            />
                        </div>

                        {/* Trick Area */}
                        <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center', position: 'relative' }}>
                            <TrickArea
                                activeTrickCards={activeTrickCards}
                                phase={gameState.phase}
                                trickWinner={gameState.trickWinner}
                            />
                        </div>

                        {/* My Control Area */}
                        <div style={{ flex: '0 0 auto' }}>
                            <PlayerControlArea
                                myPlayer={myPlayer}
                                isMyTurn={isMyTurn}
                                phase={gameState.phase}
                                playCard={playCard}
                                sendReady={sendReady}
                                iAmReady={iAmReady}
                                trickStarterUsername={trickStarterUsername}
                            />
                        </div>

                        {/* Modals integrated in the game board area or effectively global */}
                        {gameState.phase === 'BIDDING' && myPlayer?.bid == null && (
                            <div style={{ position: 'absolute', top: 0, left: 0, right: 0, bottom: 0, zIndex: 20 }}>
                                <BidModal roundNumber={gameState.roundNumber} onBid={sendBid} />
                            </div>
                        )}

                    </Col>

                    {/* RIGHT: Chat & Menu */}
                    <Col xs={24} md={6} style={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
                        <div style={{ flex: 1, minHeight: 0, marginBottom: '10px' }}>
                            <ChatWindow
                                messages={messages}
                                chatInput={chatInput}
                                setChatInput={setChatInput}
                                sendMessage={sendMessage}
                                connected={connected}
                                user={user}
                                style={{ height: '100%', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                            />
                        </div>

                        <div style={{ textAlign: 'center', marginTop: '10px' }}>
                            <Button
                                type="primary"
                                danger
                                icon={<HomeOutlined />}
                                onClick={() => navigate('/lobby')}
                                block
                                size="large"
                            >
                                Exit Game
                            </Button>
                        </div>
                    </Col>
                </Row>
            </div>

            <RoundOverModal
                gameState={gameState}
                onReady={sendReady}
                currentUsername={user?.username}
            />

            <GameOverModal
                gameState={gameState}
                onExit={() => navigate('/lobby')}
            />

        </MainLayout>
    );
};

export default SkullKing;
