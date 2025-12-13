import { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { useWebSocket } from '../../context/WebSocketContext';

export const useFlipSeven = () => {
    const [searchParams] = useSearchParams();
    const gameId = searchParams.get('gameId');
    const navigate = useNavigate();
    const user = useSelector((state: any) => state.auth.user);

    // Use Context
    const { connected, messages, connect, sendMessage, sendGameAction, sendAction, currentGameId, gameState } = useWebSocket();

    const [loading, setLoading] = useState(true);
    const [chatInput, setChatInput] = useState('');

    useEffect(() => {
        if (!gameId || !user) {
            setLoading(false);
            return;
        }

        // Ensure strictly connected to this game.
        connect(Number(gameId), 'FLIP_SEVEN');
        setLoading(false);

    }, [gameId, user, connect]);

    // Sync Game State on Connect
    useEffect(() => {
        if (connected && currentGameId === Number(gameId) && sendAction) {
            sendAction('SYNC_REQUEST', {});
        }
    }, [connected, currentGameId, gameId, sendAction]);

    const handleSendMessage = () => {
        if (chatInput.trim()) {
            sendMessage(chatInput);
            setChatInput('');
        }
    };

    return {
        gameId,
        user,
        loading,
        connected: connected && currentGameId === Number(gameId),
        messages,
        chatInput,
        setChatInput,
        sendMessage: handleSendMessage,
        sendGameAction,
        gameState,
        navigate
    };
};
