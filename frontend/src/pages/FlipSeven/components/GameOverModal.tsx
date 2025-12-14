import React from 'react';
import { Modal, Button, Typography } from 'antd';
import type { FlipSevenGameState } from '../../../types/flipSeven';

const { Title } = Typography;

interface GameOverModalProps {
    game: FlipSevenGameState | null;
    onExit: () => void;
}

const GameOverModal: React.FC<GameOverModalProps> = ({ game, onExit }) => {
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

export default GameOverModal;
