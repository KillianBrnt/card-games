import React from 'react';
import type { SkullKingPlayer } from '../../../types/skullKing';
import '../SkullKing.css';

interface OpponentsAreaProps {
    players: SkullKingPlayer[];
    starterUsername?: string;
}

const OpponentsArea: React.FC<OpponentsAreaProps> = ({ players, starterUsername }) => {
    return (
        <div className="sk-opponents-area">
            {players.map(p => {
                const isStarter = starterUsername === p.username;
                return (
                    <div key={p.username} className="sk-opponent-card">
                        <div className="sk-opponent-name">
                            {p.username}
                            {isStarter && (
                                <div className="sk-starter-indicator" title="First Player">Lead</div>
                            )}
                        </div>
                        <div className="sk-opponent-stats">Score: {p.score}</div>
                        <div className="sk-opponent-stats">Tricks: {p.tricksWon} / {p.bid !== undefined && p.bid !== null ? p.bid : '-'}</div>
                        {/* Hand Backs */}
                        <div className="sk-hand-backs">
                            {Array.from({ length: Math.min(p.hand.length, 5) }).map((_, i) => (
                                <div key={i} className="sk-card-back"></div>
                            ))}
                            {p.hand.length > 5 && <span className="sk-opponent-stats" style={{ alignSelf: 'center', marginLeft: '0.25rem' }}>+{p.hand.length - 5}</span>}
                        </div>
                    </div>
                );
            })}
        </div>
    );
};

export default OpponentsArea;
