import React, { useEffect, useState } from 'react';
import { Row, Col, Button } from 'antd';
import { HomeOutlined } from '@ant-design/icons';
import MainLayout from '../../components/MainLayout';
import ChatWindow from '../../components/Chat/ChatWindow';
import { useFlipSeven } from './useFlipSeven';
import type { FlipSevenGameState } from '../../types/flipSeven';

// Components
import OpponentList from './components/OpponentList';
import GameCenter from './components/GameCenter';
import PlayerHand from './components/PlayerHand';
import Scoreboard from './components/Scoreboard';
import RoundOverModal from './components/RoundOverModal';
import GameOverModal from './components/GameOverModal';

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

    // Handling Target Selection
    const handlePlayerClick = (targetUsername: string) => {
        if (game?.pendingActionType &&
            game.pendingActionInitiator &&
            user?.username &&
            game.pendingActionInitiator.toLowerCase() === user.username.toLowerCase()) {

            sendGameAction('SELECT_TARGET', { target: targetUsername });
        }
    };

    const isSelectingTarget = !!(
        game?.pendingActionType &&
        game.pendingActionInitiator &&
        user?.username &&
        game.pendingActionInitiator.toLowerCase() === user.username.toLowerCase()
    );

    // Debugging logic to help identify why action might not trigger
    useEffect(() => {
        if (game?.pendingActionType) {
            console.log('Pending Action:', game.pendingActionType, 'Initiator:', game.pendingActionInitiator, 'Me:', user?.username, 'IsSelecting:', isSelectingTarget);
        }
    }, [game?.pendingActionType, game?.pendingActionInitiator, user?.username, isSelectingTarget]);
    const isReady = game?.readyPlayers?.includes(user?.username || '') || false;

    const handleReady = () => {
        sendGameAction('PLAYER_READY', {});
    };

    const handleHit = () => {
        sendGameAction('HIT');
    }

    const handleStay = () => {
        sendGameAction('STAY');
    }

    // Dynamic background or style could be added here
    return (
        <MainLayout>
            <div style={{
                padding: '20px',
                height: 'calc(100vh - 64px)',
                display: 'flex',
                flexDirection: 'column',
                // Background handling: we want to be transparent to show MainLayout's background
            }}>
                <Row gutter={24} style={{ flex: 1, overflow: 'hidden' }}>

                    {/* LEFT: Game Board */}
                    <Col xs={24} md={18} style={{ display: 'flex', flexDirection: 'column', height: '100%' }}>

                        {/* 1. Opponents Area (Top) */}
                        <div style={{ flex: '0 0 auto' }}>
                            {game && (
                                <OpponentList
                                    players={game.players}
                                    currentUser={user?.username}
                                    isSelectingTarget={isSelectingTarget}
                                    onPlayerClick={handlePlayerClick}
                                />
                            )}
                        </div>

                        {/* 2. Center Action Area / Deck Info */}
                        <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                            <GameCenter
                                game={game}
                                currentUsername={user?.username}
                                onHit={handleHit}
                                onStay={handleStay}
                                canInteract={isMyTurn() && !isSelectingTarget && (getMyPlayer()?.roundActive || false)}
                            />
                        </div>

                        {/* 3. My Player Area (Bottom) */}
                        <div style={{ flex: '0 0 auto', paddingBottom: '20px' }}>
                            <PlayerHand
                                player={myPlayer}
                                isMyTurn={isMyTurn()}
                                isSelectingTarget={isSelectingTarget}
                                onHit={handleHit}
                                onStay={handleStay}
                                onSelfClick={() => handlePlayerClick(user?.username || '')}
                            />
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
                                style={{ height: '100%', borderRadius: '12px', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }}
                            />
                        </div>

                        <Scoreboard players={game?.players || []} currentUsername={user?.username} />

                        <div style={{ textAlign: 'center', marginTop: '10px' }}>
                            <Button
                                type="primary"
                                danger
                                icon={<HomeOutlined />}
                                onClick={() => navigate('/')}
                                block
                                style={{ color: 'white' }}
                            >
                                Exit Game
                            </Button>
                        </div>
                    </Col>
                </Row>
            </div>

            <RoundOverModal
                game={game}
                myPlayer={myPlayer}
                isReady={isReady}
                onReady={handleReady}
            />

            <GameOverModal
                game={game}
                onExit={() => navigate('/')}
            />

        </MainLayout>
    );
};

export default FlipSeven;
