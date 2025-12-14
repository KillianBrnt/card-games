import React from 'react';
import { Button } from 'antd';
import SkullKingCardComponent from './SkullKingCard';
import type { SkullKingPlayer } from '../../../types/skullKing';

interface PlayerControlAreaProps {
    myPlayer?: SkullKingPlayer;
    isMyTurn: boolean;
    phase: string;
    playCard: (cardId: string) => void;
    sendReady: () => void;
    iAmReady: boolean;
    trickStarterUsername?: string;
}

const PlayerControlArea: React.FC<PlayerControlAreaProps> = ({
    myPlayer,
    isMyTurn,
    phase,
    playCard,
    sendReady,
    iAmReady,
    trickStarterUsername
}) => {
    return (
        <div className="sk-my-control-area">
            <div className="sk-control-header">
                <div>
                    <div className="sk-my-stats">
                        {myPlayer?.username} (You)
                        {trickStarterUsername === myPlayer?.username && (
                            <span className="sk-starter-indicator" style={{ marginLeft: '1rem' }} title="You start this trick">You Start</span>
                        )}
                    </div>
                    <div className="sk-score">Score: {myPlayer?.score}</div>
                    <div className="sk-tricks">Tricks: {myPlayer?.tricksWon} / {myPlayer?.bid !== undefined && myPlayer?.bid !== null ? myPlayer?.bid : '?'}</div>
                </div>

                {isMyTurn && (
                    <div className="sk-turn-indicator">
                        YOUR TURN
                    </div>
                )}

                {phase === 'TRICK_OVER' && (
                    <Button
                        type="primary"
                        onClick={sendReady}
                        disabled={iAmReady}
                        size="large"
                        style={{
                            borderRadius: '9999px',
                            fontWeight: 700
                        }}
                    >
                        {iAmReady ? 'Waiting...' : 'Ready Next turn'}
                    </Button>
                )}
            </div>

            {/* My Hand */}
            <div className="sk-my-hand">
                {myPlayer?.hand.map(card => (
                    <div key={card.id} className="sk-hand-card-wrapper">
                        <SkullKingCardComponent
                            card={card}
                            onClick={() => isMyTurn && playCard(card.id)}
                            disabled={!isMyTurn}
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};

export default PlayerControlArea;
