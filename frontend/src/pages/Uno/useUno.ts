import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useWebSocket } from '../../context/WebSocketContext';
import type { UnoState } from '../../types/uno';

export const useUno = () => {
    const [searchParams] = useSearchParams();
    const gameId = searchParams.get('gameId');
    const user = useSelector((state: any) => state.auth.user);

    // Get gameState directly from context
    const { connected, messages, sendGameAction, connect, gameState: contextGameState, sendMessage } = useWebSocket();

    const [gameState, setGameState] = useState<UnoState | null>(null);

    useEffect(() => {
        if (gameId && user) {
            connect(Number(gameId), 'UNO');
        }
    }, [gameId, user, connect]);

    // Request initial state once connected
    useEffect(() => {
        if (connected && gameId) {
            sendGameAction('SYNC_REQUEST');
        }
    }, [connected, gameId, sendGameAction]);

    // Update local state when context state changes
    useEffect(() => {
        if (contextGameState && contextGameState.gameState) {
            setGameState(contextGameState.gameState as UnoState);
        }
    }, [contextGameState]);

    // Track if user said Uno continuously for this turn/session
    const [hasSaidUnoLocally, setHasSaidUnoLocally] = useState(false);

    const playCard = (cardId: string) => {
        if (!gameId) return;
        sendGameAction('PLAY_CARD', {
            cardId,
            saidUno: hasSaidUnoLocally
        });
        setHasSaidUnoLocally(false); // Reset after use
    };

    const drawCard = () => {
        sendGameAction('DRAW_CARD');
    };

    const selectColor = (color: string) => {
        sendGameAction('SELECT_COLOR', { color });
    };

    const sayUno = () => {
        setHasSaidUnoLocally(true);
        sendGameAction('SAY_UNO');
    };

    // Chat state
    const [chatInput, setChatInput] = useState('');

    const onSendMessage = () => {
        if (chatInput.trim()) {
            sendMessage(chatInput);
            setChatInput('');
        }
    };

    return {
        gameState,
        user,
        connected,
        messages,
        chatInput,
        setChatInput,
        sendMessage: onSendMessage,
        playCard,
        drawCard,
        selectColor,
        sayUno
    };
};
