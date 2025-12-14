import React from 'react';
import FlipSevenCard from './FlipSevenCard';

interface FlipSevenDeckProps {
    onClick?: () => void;
    disabled?: boolean;
    cardsRemaining?: number;
}

const FlipSevenDeck: React.FC<FlipSevenDeckProps> = ({ onClick, disabled = false, cardsRemaining }) => {
    return (
        <div
            style={{
                position: 'relative',
                width: '120px',
                height: '180px',
                cursor: disabled ? 'not-allowed' : 'pointer',
                opacity: disabled ? 0.7 : 1
            }}
            onClick={() => !disabled && onClick && onClick()}
        >
            {/* Stack effect */}
            <div style={{ position: 'absolute', top: 4, left: 4, zIndex: 1 }}>
                <FlipSevenCard isBack size="normal" />
            </div>
            <div style={{ position: 'absolute', top: 2, left: 2, zIndex: 2 }}>
                <FlipSevenCard isBack size="normal" />
            </div>
            <div style={{ position: 'absolute', top: 0, left: 0, zIndex: 3 }}>
                <FlipSevenCard isBack size="normal" className="deck-top-card" />
            </div>

            {/* Visual indicator of "Draw" */}
            {!disabled && (
                <div style={{
                    position: 'absolute',
                    bottom: '-30px',
                    width: '100%',
                    textAlign: 'center',
                    color: 'white',
                    fontWeight: 'bold',
                    textShadow: '0 1px 2px black'
                }}>
                    CLICK TO DRAW
                </div>
            )}
        </div>
    );
};

export default FlipSevenDeck;
