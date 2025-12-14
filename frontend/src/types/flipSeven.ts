export interface Card {
    id: string;
    type: 'NUMBER' | 'ACTION_FREEZE' | 'ACTION_FLIP3' | 'ACTION_SECOND_CHANCE' | 'MODIFIER_PLUS' | 'MODIFIER_MULTIPLY';
    value: number;
    name: string;
    noEffect?: boolean;
}

export interface FlipSevenPlayer {
    username: string;
    hand: Card[];
    bankedCards: Card[];
    roundScore: number;
    lastRoundScore: number;
    totalScore: number;
    roundActive: boolean;
    hasSecondChance: boolean;
}

export interface FlipSevenGameState {
    players: FlipSevenPlayer[];
    currentPlayerIndex: number;
    gameCheck: boolean;
    deck?: any; // Hidded
    pendingActionType?: string;
    pendingActionInitiator?: string;
    isRoundOver?: boolean;
    readyPlayers?: string[];
    winner?: string;
    isGameOver?: boolean;
}
