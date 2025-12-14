import React from 'react';
import { HeartFilled, LockFilled } from '@ant-design/icons';
import type { Card as CardType } from '../../../types/flipSeven';
import './FlipSevenCard.css';

interface FlipSevenCardProps {
    card?: CardType;
    isBack?: boolean;
    size?: 'small' | 'normal' | 'large';
    style?: React.CSSProperties;
    className?: string;
    onClick?: () => void;
}

const FlipSevenCard: React.FC<FlipSevenCardProps> = ({
    card,
    isBack = false,
    size = 'normal',
    style,
    className = '',
    onClick
}) => {

    // Scale factor based on size
    const getScale = () => {
        switch (size) {
            case 'small': return 0.4;
            case 'large': return 1.15;
            default: return 1;
        }
    };

    const scale = getScale();
    const baseWidth = 120;
    const baseHeight = 180;

    const wrapperStyle: React.CSSProperties = {
        width: `${baseWidth * scale}px`,
        height: `${baseHeight * scale}px`,
        fontSize: `${scale * 16}px`, // Base size 16px, scales with card
        ...style
    };

    // If it's the back of the card
    if (isBack || !card) {
        return (
            <div
                className={`flip7-card card-back ${className}`}
                style={wrapperStyle}
                onClick={onClick}
            >
                <div className="diagonal-band">
                    <span className="diagonal-text">FLIP7</span>
                </div>
            </div>
        );
    }

    // Determine card type visuals
    const isSecondChance = card.type === 'ACTION_SECOND_CHANCE';
    const isFreeze = card.type === 'ACTION_FREEZE';
    const isFlip3 = card.type === 'ACTION_FLIP3';
    // Modifiers or Numbers
    // const isModifier = card.type === 'MODIFIER_PLUS' || card.type === 'MODIFIER_MULTIPLY';

    // Default to number if not special
    let cardClass = 'card-face-number';
    let content = null;

    if (isSecondChance) {
        cardClass = 'card-face-seconde-chance';
        content = (
            <>
                <div className="card-icon-overlay"><HeartFilled style={{ color: 'white' }} /></div>
                <div className="card-icon-overlay left"><HeartFilled style={{ color: 'white' }} /></div>

                <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <HeartFilled style={{ fontSize: `${3 * scale}rem`, color: 'white', opacity: 0.8 }} />
                </div>

                <div className="face-band-container">
                    <div className="face-band">
                        <span className="face-text text-seconde-chance">SECONDE CHANCE</span>
                    </div>
                </div>
            </>
        );
    } else if (isFreeze) {
        cardClass = 'card-face-freeze';
        content = (
            <>
                <div className="card-icon-overlay"><LockFilled style={{ color: 'white' }} /></div>
                <div className="card-icon-overlay left"><LockFilled style={{ color: 'white' }} /></div>

                <div style={{ flex: 1, display: 'flex', justifyContent: 'center', alignItems: 'center' }}>
                    <LockFilled style={{ fontSize: `${3 * scale}rem`, color: 'white', opacity: 0.8 }} />
                </div>

                <div className="face-band-container">
                    <div className="face-band">
                        <span className="face-text text-freeze">FREEZE</span>
                    </div>
                </div>
            </>
        );
    } else if (isFlip3) {
        cardClass = 'card-face-flip3';
        content = (
            <>
                <div className="face-band-container">
                    <div className="face-band">
                        <span className="face-text text-flip3">FLIP 3</span>
                    </div>
                </div>
            </>
        );
    } else {
        // Number Card
        const colors = ['#f5222d', '#fa8c16', '#faad14', '#52c41a', '#13c2c2', '#1890ff', '#2f54eb', '#722ed1', '#eb2f96'];
        const numVal = parseInt(card.name) || 0;
        const color = colors[numVal % colors.length] || '#000';

        content = (
            <>
                <div className="number-main" style={{ color: color }}>
                    {card.name}
                </div>
                <div className="number-bottom-band" style={{ color: color }}>
                    {card.name}
                </div>
            </>
        );
    }

    return (
        <div
            className={`flip7-card card-face ${cardClass} ${className}`}
            style={{
                ...wrapperStyle,
                opacity: card.noEffect ? 0.5 : 1,
                filter: card.noEffect ? 'grayscale(80%)' : 'none',
            }}
            onClick={onClick}
        >
            {content}
        </div>
    );
};

export default FlipSevenCard;

