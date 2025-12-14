import React from 'react';
import { Card, Tag, Typography } from 'antd';
import type { FlipSevenPlayer } from '../../../types/flipSeven';

const { Text } = Typography;

interface ScoreboardProps {
    players: FlipSevenPlayer[];
    currentUsername?: string;
}

const Scoreboard: React.FC<ScoreboardProps> = ({ players, currentUsername }) => {
    return (
        <Card
            title="Scoreboard (Goal: 200)"
            size="small"
            style={{ flex: '0 0 auto', marginBottom: '10px' }}
            bodyStyle={{ padding: '0 10px', maxHeight: '200px', overflowY: 'auto' }}
        >
            {players && players.length > 0 ? (
                <ul style={{ listStyle: 'none', padding: 0 }}>
                    {[...players].sort((a, b) => b.totalScore - a.totalScore).map(p => (
                        <li key={p.username} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 0', borderBottom: '1px solid #f0f0f0', alignItems: 'center' }}>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                                {p.totalScore >= 200 && <span>🏆</span>}
                                <span style={{ fontWeight: p.username === currentUsername ? 'bold' : 'normal' }}>
                                    {p.username}
                                </span>
                            </div>
                            <Tag color={p.totalScore >= 200 ? 'gold' : 'blue'}>{p.totalScore}</Tag>
                        </li>
                    ))}
                </ul>
            ) : <Text>No scores yet</Text>}
        </Card>
    );
};

export default Scoreboard;
