import React, { useState } from 'react';
import { Typography, Button, Avatar, Row, Col } from 'antd';
import { HomeOutlined } from '@ant-design/icons';
import { useUno } from './useUno';
import UnoCard from './components/UnoCard';
import MainLayout from '../../components/MainLayout';
import ChatWindow from '../../components/Chat/ChatWindow';
import UnoColorModal from './components/UnoColorModal';
import UnoGameOverModal from './components/UnoGameOverModal';
import './Uno.css';
import type { UnoPlayer } from '../../types/uno';

const { Text } = Typography;

const Uno: React.FC = () => {
    const {
        gameState,
        user,
        playCard,
        drawCard,
        selectColor,
        sayUno,
        connected,
        messages,
        chatInput,
        setChatInput,
        sendMessage
    } = useUno();

    const [isColorModalOpen, setColorModalOpen] = useState(false);

    if (!gameState) return <div>Loading Uno...</div>;

    const myPlayerIndex = gameState.players.findIndex(p => p.username === user?.username);
    const myPlayer = gameState.players[myPlayerIndex];
    const isMyTurn = gameState.players[gameState.currentPlayerIndex].username === user?.username;

    // Determine opponents (rotate array so me is bottom)
    const opponents: UnoPlayer[] = [];
    if (myPlayerIndex !== -1) {
        for (let i = 1; i < gameState.players.length; i++) {
            const idx = (myPlayerIndex + i) % gameState.players.length;
            opponents.push(gameState.players[idx]);
        }
    }

    const isInterceptionPossible = (card: any) => {
        if (!gameState.currentTopCard) return false;
        if (card.color === 'NONE' || gameState.currentTopCard.color === 'NONE') return false;
        if (card.color !== gameState.currentTopCard.color) return false;

        // Exact match check
        if (card.type === 'NUMBER' && gameState.currentTopCard.type === 'NUMBER') {
            return card.value === gameState.currentTopCard.value;
        }
        if (card.type === gameState.currentTopCard.type && card.type !== 'NUMBER') {
            return true; // Match Action
        }
        return false;
    };

    const handleCardClick = (card: any) => {
        if (!isMyTurn && !isInterceptionPossible(card)) return;

        if (card.type === 'WILD' || card.type === 'WILD_DRAW_FOUR') {
            playCard(card.id);
        } else {
            playCard(card.id);
        }
    };

    if (gameState.waitingForColorSelection &&
        gameState.pendingActionInitiator === user?.username &&
        !isColorModalOpen) {
        setColorModalOpen(true);
    }

    const handleColorSelect = (color: string) => {
        selectColor(color);
        setColorModalOpen(false);
    };

    return (
        <MainLayout background="radial-gradient(circle at center, #2b32b2, #1488cc)">
            <div style={{
                paddingTop: '20px',
                height: '100%',
                display: 'flex',
                flexDirection: 'column',
            }}>
                <Row gutter={24} style={{ flex: 1, overflow: 'hidden' }}>
                    {/* LEFT: Game Board */}
                    <Col xs={24} md={18} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
                        <div className="uno-game-container">
                            {/* Opponents Area */}
                            <div className="uno-opponents">
                                {opponents.map((p, idx) => (
                                    <div key={idx} className={`uno-opponent ${gameState.currentPlayerIndex === gameState.players.indexOf(p) ? 'active' : ''}`}>
                                        <Avatar size="large" style={{ backgroundColor: '#f56a00' }}>{p.username[0]}</Avatar>
                                        <Text style={{ color: 'white', display: 'block' }}>{p.username}</Text>
                                        <Text style={{ color: '#ccc' }}>Cards: {p.hand.length}</Text>
                                        {p.saidUno && <div className="uno-shout">UNO!</div>}
                                    </div>
                                ))}
                            </div>

                            {/* Game Center */}
                            <div className="uno-center">
                                <div className="uno-deck" onClick={isMyTurn && !gameState.waitingForColorSelection ? drawCard : undefined}>
                                    <div className="uno-back-card">UNO</div>
                                </div>
                                <div className="uno-discard">
                                    <UnoCard card={gameState.currentTopCard} disabled />
                                </div>

                                <div className="uno-info">
                                    <Text style={{ color: 'white' }}>Current Color: <span style={{ color: gameState.currentColor?.toLowerCase() || 'white', fontWeight: 'bold' }}>{gameState.currentColor}</span></Text>
                                    <br />
                                    <Text style={{ color: 'white' }}>Direction: {gameState.direction === 1 ? '↻' : '↺'}</Text>
                                </div>
                            </div>

                            {/* Player Area */}
                            <div className="uno-player-area">
                                <div className="uno-controls">
                                    <Button
                                        className="uno-button-shout"
                                        type="primary"
                                        danger
                                        disabled={!myPlayer || myPlayer.hand.length > 2}
                                        onClick={sayUno}
                                    >
                                        UNO!
                                    </Button>
                                </div>

                                <div className={`uno-hand ${isMyTurn ? 'my-turn' : ''}`}>
                                    {myPlayer?.hand.map(card => (
                                        <UnoCard
                                            key={card.id}
                                            card={card}
                                            onClick={() => handleCardClick(card)}
                                            disabled={(!isMyTurn && !isInterceptionPossible(card)) || (gameState.waitingForColorSelection && gameState.pendingActionInitiator === user?.username)}
                                            highlighted={!isMyTurn && isInterceptionPossible(card)}
                                        />
                                    ))}
                                </div>
                            </div>
                        </div>
                    </Col>

                    {/* RIGHT: Chat & Controls */}
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

                        <div style={{ textAlign: 'center', marginTop: 'auto' }}>
                            <Button
                                type="primary"
                                danger
                                icon={<HomeOutlined />}
                                href="/"
                                block
                                style={{ color: 'white' }}
                            >
                                Exit Game
                            </Button>
                        </div>
                    </Col>
                </Row>
            </div>

            <UnoColorModal
                open={isColorModalOpen}
                onColorSelect={handleColorSelect}
            />

            <UnoGameOverModal
                open={gameState.gameOver}
                winner={gameState.winner}
            />
        </MainLayout>
    );
};

export default Uno;
