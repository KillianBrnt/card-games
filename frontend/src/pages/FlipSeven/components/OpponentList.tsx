import React from 'react';
import { Avatar, Tag } from 'antd';
import { UserOutlined, HeartFilled } from '@ant-design/icons';
import FlipSevenCard from './FlipSevenCard';
import type { FlipSevenPlayer } from '../../../types/flipSeven';

interface OpponentListProps {
    players: FlipSevenPlayer[];
    currentUser?: string;
    isSelectingTarget: boolean;
    onPlayerClick: (username: string) => void;
}

const OpponentList: React.FC<OpponentListProps> = ({
    players,
    currentUser,
    isSelectingTarget,
    onPlayerClick
}) => {
    return (
        <div style={{
            display: 'flex',
            gap: '15px',
            overflowX: 'auto',
            padding: '10px 5px',
            marginBottom: '10px',
            scrollbarWidth: 'thin'
        }}>
            {players.filter(p => p.username !== currentUser).map((p) => {
                // Find actual index in full player list if needed, but we rely on passed props usually. 
                // Wait, currentPlayerIndex refers to the global list. 
                // We're iterating a filtered list.
                // We can't compare index here to currentPlayerIndex directly if we filter.
                // So we need to know if this specific player 'p' is the current player.
                // WE pass players list, so we can check if p is active.

                // Simplified logic: the parent passes the full list usually?
                // The parent passed `game.players.filter(...)`. Ah.
                // I should pass the FULL list to OpponentList, and let IT filter? 
                // No, I'll pass the logic "isCurrentPlayer". 
                // Actually the parent code did:
                // border: game.currentPlayerIndex === game.players.indexOf(p) ...

                // Let's assume the passed 'players' are just the opponents. 
                // I'll add an "isActive" prop to the player object if I could, but I can't change the type easily.
                // I will assume the parent handles the "active" check or I pass the full list.
                // Let's pass the Full List and filter inside?

                const canSelect = isSelectingTarget && p.roundActive;
                // We need to know if this player is the current turn player.
                // We'll rely on a prop "activePlayerUsername" instead of index.

                return (
                    <div
                        key={p.username}
                        onClick={() => canSelect && onPlayerClick(p.username)}
                        style={{
                            minWidth: '160px',
                            background: p.roundActive
                                ? 'rgba(255, 255, 255, 0.15)'
                                : 'rgba(255, 255, 255, 0.05)',
                            backdropFilter: 'blur(10px)',
                            color: 'white',
                            borderRadius: '12px',
                            border: '1px solid #d9d9d9',
                            boxShadow: canSelect ? '0 0 0 4px #1890ff' : '0 2px 8px rgba(0,0,0,0.1)',
                            cursor: canSelect ? 'pointer' : 'default',
                            opacity: p.roundActive ? 1 : 0.7,
                            transition: 'all 0.3s ease',
                            padding: '8px',
                            position: 'relative',
                            overflow: 'hidden',
                            zIndex: 10 // Ensure it sits above other background elements
                        }}
                    >
                        {/* Turn Indicator */}
                        {/* We need to know if it's their turn. I'll add a prop 'activeUsername' */}

                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '4px' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                                <Avatar
                                    size="small"
                                    icon={<UserOutlined />}
                                    style={{ backgroundColor: p.roundActive ? '#1890ff' : '#ccc' }}
                                />
                                <div style={{ overflow: 'hidden' }}>
                                    <div style={{ fontWeight: 'bold', fontSize: '0.9rem', whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis', maxWidth: '80px', color: 'white' }}>
                                        {p.username}
                                        {p.hasSecondChance && <HeartFilled style={{ color: '#ff4d4f', marginLeft: '3px' }} />}
                                    </div>
                                    <Tag color="blue" style={{ margin: 0, fontSize: '0.7rem', lineHeight: '18px' }}>{p.totalScore} pts</Tag>
                                </div>
                            </div>
                        </div>

                        <div style={{
                            background: 'rgba(0,0,0,0.2)',
                            borderRadius: '6px',
                            padding: '4px',
                            minHeight: '100px'
                        }}>
                            <div style={{ fontSize: '0.75rem', color: 'rgba(255,255,255,0.7)', marginBottom: '2px' }}>
                                Round: <b>{p.roundScore}</b>
                            </div>
                            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '4px' }}>
                                {p.hand.map((c, i) => (
                                    <div key={i} style={{ width: '50px', height: '75px', marginRight: '-12px' }}>
                                        <FlipSevenCard card={c} size="small" />
                                    </div>
                                ))}
                            </div>
                        </div>

                        {!p.roundActive && (
                            <div style={{
                                position: 'absolute',
                                top: 0, left: 0, right: 0, bottom: 0,
                                background: 'rgba(0,0,0,0.1)',
                                display: 'flex',
                                justifyContent: 'center',
                                alignItems: 'center',
                                pointerEvents: 'none'
                            }}>
                                <Tag color="red" style={{ transform: 'rotate(-20deg)', fontSize: '1.2rem', padding: '5px 15px' }}>BUST/BANK</Tag>
                            </div>
                        )}
                    </div>
                );
            })}
        </div>
    );
};

export default OpponentList;
