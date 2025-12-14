import React from 'react';
import SkullKingCardComponent from './SkullKingCard';
import type { SkullKingPlayer, SkullKingCard } from '../../../types/skullKing';

interface TrickAreaProps {
    activeTrickCards: { player: SkullKingPlayer; card: SkullKingCard }[];
    phase: string;
    trickWinner?: string;
}

const TrickArea: React.FC<TrickAreaProps> = ({ activeTrickCards, phase, trickWinner }) => {
    return (
        <div className="sk-trick-area">
            {trickWinner && phase === 'TRICK_OVER' && (
                <div className="sk-trick-winner-banner">
                    {trickWinner} wins the trick!
                </div>
            )}

            {activeTrickCards.length > 0 ? (
                <div className="sk-trick-cards">
                    {activeTrickCards.map(({ player, card }) => (
                        <div key={player.username} className="sk-trick-card-wrapper">
                            <span className="sk-player-label">{player.username}</span>
                            <SkullKingCardComponent card={card} size="md" />
                        </div>
                    ))}
                </div>
            ) : (
                <div className="sk-waiting">
                    Waiting for plays...
                </div>
            )}
        </div>
    );
};

export default TrickArea;
