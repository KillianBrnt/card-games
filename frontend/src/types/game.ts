export type GameStatus = 'WAITING' | 'PLAYING' | 'FINISHED';

export interface GameResponse {
    gameId: number;
    gameCode: string;
    status: GameStatus;
    hostUserId: number;
}

export interface CreateGameRequest {
    gameType: string;
}

export interface JoinGameRequest {
    gameCode: string;
}
