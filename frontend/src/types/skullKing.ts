export type SkullKingCardType = 'NUMBER' | 'PIRATE' | 'MERMAID' | 'SKULL_KING' | 'ESCAPE';
export type SkullKingColor = 'YELLOW' | 'PURPLE' | 'GREEN' | 'RED' | 'BLACK' | 'NONE';

export interface SkullKingCard {
    id: string;
    type: SkullKingCardType;
    color: SkullKingColor;
    value: number; // 0 for specials, 1-13 for numbers
}

export interface SkullKingPlayer {
    username: string;
    hand: SkullKingCard[];
    bid?: number;
    tricksWon: number;
    score: number;
    roundPoints: number;
    cardPlayed?: SkullKingCard;
}

export interface SkullKingState {
    deck: SkullKingCard[]; // Usually empty/hidden for client
    players: SkullKingPlayer[];
    currentPlayerIndex: number;
    roundNumber: number;
    phase: 'BIDDING' | 'PLAYING' | 'ROUND_OVER' | 'GAME_OVER' | 'TRICK_OVER';
    trickStarterIndex: number;
    winner?: string;
    trickWinner?: string;
    readyPlayers: string[];
}

export interface SkullKingGameUpdate {
    type: 'GAME_UPDATE';
    gameState: SkullKingState;
}
