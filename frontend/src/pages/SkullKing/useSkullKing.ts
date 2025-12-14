import { useState, useEffect } from 'react';
import { useWebSocket } from '../../context/WebSocketContext';
import type { SkullKingState } from '../../types/skullKing';
import { useSearchParams } from 'react-router-dom';


export const useSkullKing = () => {
    const { connected, connect, sendGameAction, gameState: contextGameState, messages, sendMessage } = useWebSocket();
    const [searchParams] = useSearchParams();
    const gameId = searchParams.get('gameId');
    const [gameState, setGameState] = useState<SkullKingState | null>(null);
    const [isLoading, setIsLoading] = useState(true);
    const [chatInput, setChatInput] = useState('');

    // Connect if not connected
    useEffect(() => {
        if (gameId && !connected) {
            connect(Number(gameId), 'SKULL_KING');
        }
    }, [gameId, connected, connect]);

    // Sync state from context
    useEffect(() => {
        if (contextGameState && contextGameState.gameState) {
            // Check if it's actually SkullKing state or previous game state?
            // The engine sends { type: 'GAME_UPDATE', gameState: { ... } }
            // contextGameState is the payload.
            setGameState(contextGameState.gameState as SkullKingState);
            setIsLoading(false);
        }
    }, [contextGameState]);

    useEffect(() => {
        if (connected && gameId) {
            // Request sync
            sendGameAction('SYNC_REQUEST');
        }
    }, [connected, gameId, sendGameAction]);

    const sendBid = (bid: number) => {
        sendGameAction('BID', { bid });
    };

    const playCard = (cardId: string) => {
        sendGameAction('PLAY_CARD', { cardId });
    };

    const startNextRound = () => {
        sendGameAction('PLAYER_READY');
    }

    const sendReady = () => {
        sendGameAction('PLAYER_READY');
    }

    const onSendMessage = () => {
        if (chatInput.trim()) {
            sendMessage(chatInput);
            setChatInput('');
        }
    };

    return {
        gameState,
        sendBid,
        playCard,
        startNextRound,
        sendReady,
        isLoading,
        gameId,
        messages,
        sendMessage: onSendMessage,
        connected,
        chatInput,
        setChatInput
    };
};
