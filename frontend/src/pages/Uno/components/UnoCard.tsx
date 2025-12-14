import React from 'react';
import './UnoCard.css';
import type { UnoCard as UnoCardType } from '../../../types/uno';

interface UnoCardProps {
    card: UnoCardType;
    onClick?: () => void;
    disabled?: boolean;
    size?: 'small' | 'normal' | 'large';
    highlighted?: boolean;
}

const UnoCard: React.FC<UnoCardProps> = ({ card, onClick, disabled, size = 'normal', highlighted }) => {
    const getColorClass = (color: string) => {
        switch (color) {
            case 'RED': return 'uno-red';
            case 'BLUE': return 'uno-blue';
            case 'GREEN': return 'uno-green';
            case 'YELLOW': return 'uno-yellow';
            default: return 'uno-black';
        }
    };

    const getDisplayContent = () => {
        if (card.type === 'NUMBER') return card.value;
        if (card.type === 'SKIP') return '🚫';
        if (card.type === 'REVERSE') return '⇄';
        if (card.type === 'DRAW_TWO') return '+2';
        if (card.type === 'WILD') return '🌈';
        if (card.type === 'WILD_DRAW_FOUR') return '+4';
        return card.displayValue;
    };

    return (
        <div
            className={`uno-card ${getColorClass(card.color)} ${size} ${disabled ? 'disabled' : ''} ${highlighted ? 'highlighted-interception' : ''}`}
            onClick={!disabled ? onClick : undefined}
        >
            <div className="uno-card-inner">
                <span className="uno-card-value">{getDisplayContent()}</span>
            </div>
            {/* Small corner values */}
            <span className="uno-corner-tl">{getDisplayContent()}</span>
            <span className="uno-corner-br">{getDisplayContent()}</span>
        </div>
    );
};

export default UnoCard;
