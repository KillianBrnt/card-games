import React, { createContext, useContext, useRef, useState, useEffect, useCallback } from 'react';
import SockJS from 'sockjs-client';
import { Stomp } from '@stomp/stompjs';
import { useSelector } from 'react-redux';

export interface Action {
    sender: string;
    payload: any;
    type: string;
    gameId?: number;
    gameType?: string;
    // UI convenience
    content?: string;
}

interface WebSocketContextType {
    connected: boolean;
    messages: Action[];
    players: string[];
    gameState: any;
    connect: (gameId: number) => void;
    disconnect: () => void;
    sendMessage: (content: string) => void;
    sendGameAction: (actionType: string, props?: any) => void;
    sendAction: (type: string, payload: any) => void;
    currentGameId: number | null;
}

const WebSocketContext = createContext<WebSocketContextType | undefined>(undefined);

export const WebSocketProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
    const user = useSelector((state: any) => state.auth.user);
    const [connected, setConnected] = useState(false);
    const [messages, setMessages] = useState<Action[]>([]);
    const [players, setPlayers] = useState<string[]>([]);
    const [gameState, setGameState] = useState<any>(null);
    const [currentGameId, setCurrentGameId] = useState<number | null>(null);

    const stompClientRef = useRef<any>(null);

    const disconnect = useCallback(() => {
        if (stompClientRef.current) {
            try {
                stompClientRef.current.disconnect();
            } catch (e) {
                console.error("Error disconnecting", e);
            }
            stompClientRef.current = null;
        }
        setConnected(false);
        setCurrentGameId(null);
        setMessages([]);
        setPlayers([]);
        setGameState(null);
    }, []);

    const connect = useCallback((gameId: number) => {
        if (!user) return;

        // If already connected to this game, do nothing
        if (stompClientRef.current && connected && currentGameId === gameId) {
            console.log("Already connected to game", gameId);
            return;
        }

        // If connected to another game, disconnect first
        if (stompClientRef.current) {
            disconnect();
        }

        const socket = new SockJS('/ws');
        const stompClient = Stomp.over(socket);
        // stompClient.debug = () => { };

        stompClient.connect({}, (frame: any) => {
            console.log('Context Connected: ' + frame);
            setConnected(true);
            setCurrentGameId(gameId);

            // Subscribe to Chat/Actions
            stompClient.subscribe(`/topic/lobby/${gameId}/chat`, (payload: any) => {
                const rawAction = JSON.parse(payload.body);
                // Map payload to content for UI compatibility
                const action = {
                    ...rawAction,
                    content: rawAction.payload?.content || rawAction.payload?.text
                };

                if (action.type === 'CHAT' || action.type === 'JOIN' || action.type === 'LEAVE' || action.type === 'SYSTEM') {
                    if (action.type === 'JOIN') action.content = `${action.sender} joined!`;
                    if (action.type === 'LEAVE') action.content = `${action.sender} left!`;
                    // System messages like GAME_STARTED
                    if (action.type === 'SYSTEM' && action.payload?.content === 'GAME_STARTED') {
                        action.content = 'GAME_STARTED'; // Ensure explicit check works
                    }

                    setMessages(prev => [...prev, action].slice(-100));
                }
            });

            // Subscribe to Player List
            stompClient.subscribe(`/topic/lobby/${gameId}`, (payload: any) => {
                const activePlayers = JSON.parse(payload.body);
                setPlayers(activePlayers);
            });

            // Subscribe to Game Updates
            stompClient.subscribe(`/topic/lobby/${gameId}/game`, (payload: any) => {
                const gameAction = JSON.parse(payload.body);
                console.log("Game Action Received:", gameAction);
                if (gameAction.payload?.type === 'GAME_UPDATE') {
                    setGameState((prev: any) => ({
                        ...prev,
                        ...gameAction.payload
                    }));
                }
            });

            // Send Join Action
            // Backend expects Action with payload now
            const joinAction = {
                sender: user.username,
                type: 'JOIN',
                payload: {}
            };
            stompClient.send(`/app/action/${gameId}/addUser`, {}, JSON.stringify(joinAction));

        }, (err: any) => {
            console.error('STOMP Context Error:', err);
            setConnected(false);
        });

        stompClientRef.current = stompClient;

    }, [user, connected, currentGameId, disconnect]);

    const sendAction = useCallback((type: string, payload: any) => {
        if (stompClientRef.current && connected && currentGameId && user) {
            const action = {
                sender: user.username,
                payload: payload,
                type: type,
                gameType: 'FLIP_SEVEN' // Should be dynamic but hardcoded for this requirement
            };
            stompClientRef.current.send(`/app/action/${currentGameId}/sendMessage`, {}, JSON.stringify(action));
        }
    }, [connected, currentGameId, user]);

    const sendMessage = useCallback((content: string) => {
        sendAction('CHAT', { content });
    }, [sendAction]);

    const sendGameAction = useCallback((actionType: string, props: any = {}) => {
        sendAction('GAME_ACTION', { action: actionType, ...props });
    }, [sendAction]);

    // Cleanup on unmount (App level unmount)
    useEffect(() => {
        return () => {
            if (stompClientRef.current) {
                try {
                    stompClientRef.current.disconnect();
                } catch (e) { console.error(e); }
            }
        };
    }, []);

    return (
        <WebSocketContext.Provider value={{ connected, messages, players, gameState, connect, disconnect, sendMessage, sendGameAction, sendAction, currentGameId }}>
            {children}
        </WebSocketContext.Provider>
    );
};

export const useWebSocket = () => {
    const context = useContext(WebSocketContext);
    if (context === undefined) {
        throw new Error('useWebSocket must be used within a WebSocketProvider');
    }
    return context;
};
