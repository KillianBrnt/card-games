import type { CreateGameRequest, GameResponse, JoinGameRequest } from '../types/game';

export const gameService = {
    createGame: async (gameType: string): Promise<GameResponse> => {
        const payload: CreateGameRequest = { gameType };
        const response = await fetch('/game/create', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to create game');
        }

        return response.json();
    },

    joinGame: async (gameCode: string): Promise<GameResponse> => {
        const payload: JoinGameRequest = { gameCode };
        const response = await fetch('/game/join', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(payload),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to join game');
        }

        return response.json();
    },

    getGameInfo: async (gameId: number): Promise<GameResponse> => {
        const response = await fetch(`/game/info?gameId=${gameId}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            }
        });

        if (response.status === 403) {
            throw new Error('Access Denied: You are not a player in this game');
        }

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Failed to fetch game info');
        }

        return response.json();
    }
};
