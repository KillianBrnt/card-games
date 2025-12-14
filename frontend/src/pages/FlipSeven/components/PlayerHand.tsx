import React from 'react';
import { Typography, Tag } from 'antd';
import { HeartFilled } from '@ant-design/icons';
import FlipSevenCard from './FlipSevenCard';
import type { FlipSevenPlayer } from '../../../types/flipSeven';

const { Title, Text } = Typography;

interface PlayerHandProps {
    player: FlipSevenPlayer | undefined;
    isMyTurn: boolean;
    isSelectingTarget: boolean;
    onHit?: () => void;
    onStay?: () => void;
    onSelfClick?: () => void;
    onCardClick?: (card: any) => void;
}

const PlayerHand: React.FC<PlayerHandProps> = ({
    player,
    isMyTurn,
    isSelectingTarget,
    onSelfClick
}) => {

    if (!player) return null;

    return (
        <div style={{
            background: 'rgba(255, 255, 255, 0.4)',
            backdropFilter: 'blur(20px)',
            borderRadius: '24px',
            padding: '16px',
            boxShadow: '0 8px 32px rgba(0,0,0,0.1)',
            border: (isSelectingTarget && player.roundActive) ? '4px solid #1890ff' : '1px solid rgba(255,255,255,0.5)',
            transition: 'all 0.3s ease',
            position: 'relative',
            overflow: 'hidden'
        }}>
            {/* Header */}
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '15px' }}>
                    <div style={{
                        width: '50px', height: '50px',
                        background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                        borderRadius: '50%',
                        display: 'flex', justifyContent: 'center', alignItems: 'center',
                        color: 'white', fontWeight: 'bold', fontSize: '1.5rem',
                        boxShadow: '0 4px 10px rgba(118, 75, 162, 0.4)'
                    }}>
                        {player.username.charAt(0).toUpperCase()}
                    </div>
                    <div>
                        <Title level={4} style={{ margin: 0 }}>
                            My Hand
                            {player.hasSecondChance && (
                                <Tag color="red" style={{ marginLeft: '10px', borderRadius: '12px' }}>
                                    <HeartFilled /> Second Chance Active
                                </Tag>
                            )}
                        </Title>
                        <Text type="secondary">Total Score: <b>{player.totalScore}</b></Text>
                    </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                    <div style={{
                        fontSize: '2.5rem',
                        fontWeight: '900',
                        color: '#52c41a',
                        lineHeight: 1
                    }}>
                        {player.roundScore}
                    </div>
                    <Text type="secondary">Points this round</Text>
                </div>
            </div>

            {/* Hand Area */}
            <div style={{
                background: 'rgba(255,255,255,0.3)',
                borderRadius: '16px',
                padding: '12px',
                minHeight: '160px',
                display: 'flex',
                gap: '15px',
                overflowX: 'auto',
                alignItems: 'center',
                boxShadow: 'inset 0 2px 8px rgba(0,0,0,0.05)',
                marginBottom: '20px'
            }}>
                {player.hand.length === 0 && (
                    <div style={{ width: '100%', textAlign: 'center', color: '#999', fontSize: '1.2rem' }}>
                        Your hand is empty. Hit the deck to start!
                    </div>
                )}
                {player.hand.map((c, i) => (
                    // We can add animation delays based on index `i` for staggering
                    <div key={i} className="card-anim-enter" style={{ animationDelay: `${i * 0.1}s` }}>
                        <FlipSevenCard card={c} size="normal" />
                    </div>
                ))}
            </div>

            {/* Click Handler for Self-Targeting */}
            {isSelectingTarget && (
                <div
                    style={{
                        position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                        zIndex: 20,
                        cursor: 'pointer',
                        background: 'rgba(24, 144, 255, 0.1)',
                        display: 'flex',
                        justifyContent: 'center',
                        alignItems: 'center',
                        border: '4px dashed #1890ff',
                        borderRadius: '24px'
                    }}
                    onClick={onSelfClick}
                >
                    <Tag color="blue" style={{ fontSize: '1.2rem', padding: '8px 16px' }}>CLICK TO SELECT SELF</Tag>
                </div>
            )}

            {/* Status Overlay if inactive */}
            {!player.roundActive && (
                <div style={{
                    position: 'absolute', top: 0, left: 0, right: 0, bottom: 0,
                    background: 'rgba(255,255,255,0.7)',
                    zIndex: 10,
                    display: 'flex', justifyContent: 'center', alignItems: 'center'
                }}>
                    <Tag color="red" style={{ fontSize: '2rem', padding: '10px 30px', borderRadius: '8px' }}>
                        ROUND OVER FOR YOU
                    </Tag>
                </div>
            )}
        </div>
    );
};

export default PlayerHand;
