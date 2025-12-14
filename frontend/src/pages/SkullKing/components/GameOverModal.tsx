import React from 'react';
import { Modal, Button, Typography } from 'antd';
import { TrophyOutlined } from '@ant-design/icons';
import type { SkullKingState } from '../../../types/skullKing';

const { Title } = Typography;

interface GameOverModalProps {
    gameState: SkullKingState | null;
    onExit: () => void;
}

const GameOverModal: React.FC<GameOverModalProps> = ({ gameState, onExit }) => {
    if (!gameState) return null;
    const isVisible = gameState.phase === 'GAME_OVER';

    return (
        <Modal
            open={isVisible}
            footer={null}
            closable={false}
            maskClosable={false}
            centered
            bodyStyle={{ textAlign: 'center', padding: '3rem' }}
        >
            <TrophyOutlined style={{ fontSize: '6rem', color: '#ffd700', marginBottom: '1rem' }} />
            <Title level={1}>GAME OVER</Title>
            <Title level={3} style={{ marginTop: 0 }}>
                Winner: <span style={{ color: '#52c41a' }}>{gameState.winner}</span>
            </Title>

            <Button
                type="primary"
                size="large"
                onClick={onExit}
                style={{ marginTop: '2rem', paddingLeft: '3rem', paddingRight: '3rem' }}
            >
                Back to Lobby
            </Button>
        </Modal>
    );
};

export default GameOverModal;
