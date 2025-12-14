import React from 'react';
import { Modal, Button, Typography, Divider } from 'antd';
import FlipSevenCard from './FlipSevenCard';
import type { FlipSevenGameState, FlipSevenPlayer } from '../../../types/flipSeven';

const { Title } = Typography;

interface RoundOverModalProps {
    game: FlipSevenGameState | null;
    myPlayer: FlipSevenPlayer | undefined;
    isReady: boolean;
    onReady: () => void;
}

export const RoundOverModal: React.FC<RoundOverModalProps> = ({ game, myPlayer, isReady, onReady }) => {
    if (!game) return null;

    return (
        <Modal
            title="Round Finished"
            open={game.isRoundOver && !game.isGameOver}
            footer={[
                <Button
                    key="ready"
                    type="primary"
                    onClick={onReady}
                    disabled={isReady}
                    loading={isReady}
                >
                    {isReady ? `Waiting for others (${game.readyPlayers?.length || 0}/${game.players.length})` : 'Ready for Next Round'}
                </Button>
            ]}
            closable={false}
            maskClosable={false}
            centered
            width={800}
        >
            <div style={{ textAlign: 'center' }}>
                <Title level={4}>Your Round Score: <span style={{ color: '#1890ff' }}>{myPlayer?.lastRoundScore ?? 0} pts</span></Title>
                <Title level={5}>Total Score: {myPlayer?.totalScore ?? 0} pts</Title>

                <Divider>Your Hand</Divider>

                <div style={{ display: 'flex', flexWrap: 'wrap', justifyContent: 'center', gap: '10px', marginTop: '20px' }}>
                    {myPlayer?.hand.map((card, idx) => (
                        <div key={idx} style={{ transform: 'scale(0.8)' }}>
                            <FlipSevenCard card={card} />
                        </div>
                    ))}
                </div>
            </div>
        </Modal>
    );
};

interface GameOverModalProps {
    game: FlipSevenGameState | null;
    onExit: () => void;
}

export const GameOverModal: React.FC<GameOverModalProps> = ({ game, onExit }) => {
    if (!game) return null;

    return (
        <Modal
            title="🏆 Game Finished 🏆"
            open={game.isGameOver}
            footer={[
                <Button
                    key="exit"
                    type="primary"
                    onClick={onExit}
                >
                    Return to Lobby
                </Button>
            ]}
            closable={false}
            maskClosable={false}
            centered
        >
            <div style={{ textAlign: 'center' }}>
                <Title level={2}>Winner: <span style={{ color: '#ffd700' }}>{game.winner}</span></Title>
                <Title level={4}>Congratulations!</Title>
                <div style={{ fontSize: '40px' }}>🎉🎉🎉</div>
            </div>
        </Modal>
    );
};
