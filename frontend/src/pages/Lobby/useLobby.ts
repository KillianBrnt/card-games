import { useState, useEffect } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { message } from 'antd';
import { useSelector } from 'react-redux';
import { gameService } from '../../services/gameService';
import type { GameResponse } from '../../types/game';
import { useWebSocket } from '../../context/WebSocketContext';

export const useLobby = () => {
    const [searchParams] = useSearchParams();
    const gameId = searchParams.get('gameId');
    const gameType = searchParams.get('gameType');
    const navigate = useNavigate();
    const user = useSelector((state: any) => state.auth.user);
    const { connected, messages, players, connect, sendMessage, currentGameId } = useWebSocket();

    const [game, setGame] = useState<GameResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [chatInput, setChatInput] = useState('');

    // Fetch Game Info
    useEffect(() => {
        if (!gameId) {
            setError("No game ID provided");
            setLoading(false);
            return;
        }

        const fetchGameInfo = async () => {
            try {
                const data = await gameService.getGameInfo(Number(gameId));
                setGame(data);
            } catch (err: any) {
                console.error("Failed to fetch game info:", err);
                setError(err.message || "Failed to load lobby");
            } finally {
                setLoading(false);
            }
        };

        fetchGameInfo();
    }, [gameId]);

    // WebSocket Connection via Context
    useEffect(() => {
        if (gameId && user) {
            const type = gameType || game?.gameType;
            if (type) {
                connect(Number(gameId), type);
            }
        }
    }, [gameId, user, connect, gameType, game]);

    // Check for System Messages to Navigate (if context messages update)
    // Note: The context itself could handle navigation if we passed navigate to it, 
    // but typically UI components react to state.
    // However, since we are in a hook designated for Lobby, we should check messages.
    useEffect(() => {
        const lastMessage = messages[messages.length - 1];
        if (lastMessage && lastMessage.type === 'SYSTEM' && lastMessage.content === 'GAME_STARTED' && lastMessage.gameId === Number(gameId)) {
            const targetType = gameType || game?.gameType || 'FLIP_SEVEN';
            if (targetType === 'FLIP_SEVEN') {
                navigate(`/flipseven?gameId=${gameId}`);
            } else if (targetType === 'UNO') {
                navigate(`/uno?gameId=${gameId}`);
            } else {
                // Future games can be added here
                console.warn("Unknown game type:", targetType);
            }
        }
    }, [messages, gameId, navigate, gameType, game]);


    const handleSendMessage = () => {
        if (chatInput.trim()) {
            sendMessage(chatInput);
            setChatInput('');
        }
    };

    const handleLaunchGame = async () => {
        try {
            await gameService.startGame(Number(gameId));
            // Navigation handled by effect on messages
        } catch (err: any) {
            message.error("Failed to start game: " + err.message);
        }
    };

    const copyCode = () => {
        if (game?.gameCode) {
            navigator.clipboard.writeText(game.gameCode);
            message.success('Game code copied to clipboard!');
        }
    };

    return {
        game,
        loading,
        error,
        connected: connected && currentGameId === Number(gameId),
        messages,
        chatInput,
        setChatInput,
        players,
        sendMessage: handleSendMessage,
        handleLaunchGame,
        copyCode,
        user,
        navigate
    };
};
