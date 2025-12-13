import React from 'react';
import type { Card as CardType } from '../../types/flipSeven';

interface CardComponentProps {
    card: CardType;
    size?: 'small' | 'normal';
}

const CardComponent: React.FC<CardComponentProps> = ({ card, size = 'normal' }) => {
    const isNumber = card.type === 'NUMBER';
    const isAction = card.type.startsWith('ACTION');
    const isModifier = card.type.startsWith('MODIFIER');

    // Style Mapping based on type
    let borderColor = '#000';
    let textColor = '#000';

    if (isAction) {
        borderColor = '#f5222d'; // Red
        textColor = '#f5222d';
    } else if (isModifier) {
        borderColor = '#1890ff'; // Blue
        textColor = '#1890ff';
    }

    const width = size === 'small' ? '40px' : '80px';
    const height = size === 'small' ? '60px' : '120px';
    const fontSize = size === 'small' ? '0.8rem' : '1.2rem';

    return (
        <div style={{
            width,
            height,
            border: `2px solid ${borderColor}`,
            borderRadius: '8px',
            background: 'white',
            display: 'flex',
            justifyContent: 'center',
            alignItems: 'center',
            flexDirection: 'column',
            margin: '4px',
            boxShadow: '0 2px 4px rgba(0,0,0,0.2)',
            color: textColor,
            fontWeight: 'bold',
            textAlign: 'center',
            userSelect: 'none'
        }}>
            <span style={{ fontSize }}>{card.name}</span>
            {/* Optional: Add icon or suit equivalent here */}
        </div>
    );
};

export default CardComponent;
