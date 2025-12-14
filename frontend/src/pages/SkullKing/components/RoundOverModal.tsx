import React from 'react';
import { Modal, Button, Table } from 'antd';
import type { SkullKingState } from '../../../types/skullKing';
import { CheckCircleOutlined } from '@ant-design/icons';

interface RoundOverModalProps {
    gameState: SkullKingState | null;
    onReady: () => void;
    currentUsername?: string;
}

const RoundOverModal: React.FC<RoundOverModalProps> = ({ gameState, onReady, currentUsername }) => {
    if (!gameState) return null;

    const isVisible = gameState.phase === 'ROUND_OVER';
    const isReady = gameState.readyPlayers.includes(currentUsername || '');

    const columns = [
        {
            title: 'Player',
            dataIndex: 'username',
            key: 'username',
            render: (text: string) => (
                <span>
                    {text} {gameState.readyPlayers.includes(text) && <CheckCircleOutlined style={{ color: '#52c41a', marginLeft: 8 }} />}
                </span>
            ),
        },
        {
            title: 'Bid',
            dataIndex: 'bid',
            key: 'bid',
        },
        {
            title: 'Won',
            dataIndex: 'tricksWon',
            key: 'tricksWon',
        },
        {
            title: 'Round Points',
            dataIndex: 'roundPoints',
            key: 'roundPoints',
            render: (points: number) => (
                <span style={{ color: points >= 0 ? '#52c41a' : '#ff4d4f', fontWeight: 'bold' }}>
                    {points > 0 ? '+' : ''}{points}
                </span>
            ),
        }
    ];

    return (
        <Modal
            open={isVisible}
            title={<div style={{ textAlign: 'center', fontSize: '1.5rem', fontWeight: 'bold' }}>Round {gameState.roundNumber} Complete!</div>}
            footer={[
                <Button
                    key="ready"
                    type="primary"
                    size="large"
                    onClick={onReady}
                    disabled={isReady}
                    block
                >
                    {isReady ? 'Waiting for others...' : (gameState.roundNumber >= 10 ? 'See Winner' : 'Ready for Next Round')}
                </Button>
            ]}
            closable={false}
            maskClosable={false}
            centered
            width={600}
        >
            <Table
                dataSource={gameState.players}
                columns={columns}
                rowKey="username"
                pagination={false}
                bordered
            />
        </Modal>
    );
};

export default RoundOverModal;
