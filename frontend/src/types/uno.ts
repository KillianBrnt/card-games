export type UnoCardColor = 'RED' | 'BLUE' | 'GREEN' | 'YELLOW' | 'NONE';

export type UnoCardType = 'NUMBER' | 'SKIP' | 'REVERSE' | 'DRAW_TWO' | 'WILD' | 'WILD_DRAW_FOUR';

export interface UnoCard {
    id: string;
    color: UnoCardColor;
    type: UnoCardType;
    value?: number;
    displayValue: string;
}

export interface UnoPlayer {
    username: string;
    hand: UnoCard[];
    roundActive: boolean;
    saidUno: boolean;
}

export interface UnoState {
    players: UnoPlayer[];
    currentPlayerIndex: number;
    direction: number;
    currentTopCard: UnoCard;
    currentColor: UnoCardColor;
    discardPile: UnoCard[]; // Usually we just need top card, but full pile useful for visuals
    waitingForColorSelection: boolean;
    pendingActionInitiator: string | null;
    gameOver: boolean;
    winner: string | null;
}
