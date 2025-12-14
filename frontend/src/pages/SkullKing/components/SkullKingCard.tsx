import React from 'react';
import type { SkullKingCard as CardType } from '../../../types/skullKing';
import './SkullKingCard.css';

interface SkullKingCardProps {
    card: CardType;
    onClick?: () => void;
    disabled?: boolean;
    size?: 'sm' | 'md' | 'lg';
}

const SkullKingCard: React.FC<SkullKingCardProps> = ({ card, onClick, disabled = false, size = 'md' }) => {

    // Determine CSS classes for style
    let styleClass = '';

    if (card.type === 'NUMBER') {
        switch (card.color) {
            case 'YELLOW': styleClass = 'sk-yellow'; break;
            case 'GREEN': styleClass = 'sk-green'; break;
            case 'PURPLE': styleClass = 'sk-purple'; break;
            case 'RED': styleClass = 'sk-red'; break;
            case 'BLACK': styleClass = 'sk-black-flag'; break; // Distinct from default black?
            case 'NONE': styleClass = 'sk-black'; break; // Fallback
        }
    } else {
        switch (card.type) {
            case 'PIRATE': styleClass = 'sk-pirate'; break;
            case 'MERMAID': styleClass = 'sk-mermaid'; break;
            case 'SKULL_KING': styleClass = 'sk-skullking'; break;
            case 'ESCAPE': styleClass = 'sk-escape'; break;
        }
    }

    // Determine Size Class
    const sizeClass = `sk-card-${size}`;

    // Display Text/Icon
    let displayText = '';
    if (card.type === 'NUMBER') displayText = card.value.toString();
    else if (card.type === 'PIRATE') displayText = '☠️';
    else if (card.type === 'MERMAID') displayText = '🧜‍♀️';
    else if (card.type === 'SKULL_KING') displayText = '👑';
    else if (card.type === 'ESCAPE') displayText = '🏳️';

    return (
        <div
            className={`sk-card ${sizeClass} ${styleClass} ${disabled ? 'disabled' : ''}`}
            onClick={!disabled ? onClick : undefined}
        >
            <div className="sk-card-corner-tl">{card.type === 'NUMBER' ? card.value : ''}</div>
            <div className="sk-card-center-icon">{displayText}</div>
            <div className="sk-card-corner-br">{card.type === 'NUMBER' ? card.value : ''}</div>
        </div>
    );
};

export default SkullKingCard;
