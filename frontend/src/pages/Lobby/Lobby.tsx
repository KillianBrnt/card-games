
import React, { useEffect, useState } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { Card, Typography, Spin, Button, message, Tag } from 'antd';
import { CopyOutlined, HomeOutlined } from '@ant-design/icons';
import { gameService } from '../../services/gameService';
import type { GameResponse } from '../../types/game';
import MainLayout from '../../components/MainLayout';
import './Lobby.css';

const { Title, Text } = Typography;

const Lobby: React.FC = () => {
    const [searchParams] = useSearchParams();
    const gameId = searchParams.get('gameId');
    const navigate = useNavigate();

    const [game, setGame] = useState<GameResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);

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

    const copyCode = () => {
        if (game?.gameCode) {
            navigator.clipboard.writeText(game.gameCode);
            message.success('Game code copied to clipboard!');
        }
    };

    if (loading) {
        return (
            <MainLayout>
                <div className="lobby-container-content">
                    <Spin size="large" tip="Loading Lobby..." />
                </div>
            </MainLayout>
        );
    }

    if (error) {
        return (
            <MainLayout>
                <div className="lobby-container-content">
                    <Card className="lobby-card-antd" bordered={false}>
                        <Title level={2} type="danger">Error</Title>
                        <Text style={{ fontSize: '1.2rem', display: 'block', marginBottom: '2rem' }}>
                            {error}
                        </Text>
                        <Button type="primary" icon={<HomeOutlined />} onClick={() => navigate('/')}>
                            Return Home
                        </Button>
                    </Card>
                </div>
            </MainLayout>
        );
    }

    return (
        <MainLayout>
            <div className="lobby-container-content">
                <Card className="lobby-card-antd" bordered={false}>
                    <Title level={1} style={{ marginBottom: '0.5rem' }}>Lobby</Title>
                    <Tag color="blue" style={{ marginBottom: '2rem' }}>{game?.status}</Tag>

                    <div style={{ marginBottom: '2rem' }}>
                        <Text type="secondary" style={{ fontSize: '1.2rem', display: 'block' }}>
                            GAME CODE
                        </Text>
                        <div className="game-code-display" onClick={copyCode} style={{ cursor: 'pointer' }}>
                            <Title level={1} style={{ color: '#ffd700', margin: 0, letterSpacing: '5px' }}>
                                {game?.gameCode} <CopyOutlined style={{ fontSize: '20px', verticalAlign: 'middle' }} />
                            </Title>
                        </div>
                    </div>

                    <div className="players-section">
                        <Title level={3}>Players</Title>
                        {/* Placeholder for player list - since the API doesn't return players yet, we keep waiting text */}
                        <Text type="secondary" style={{ fontStyle: 'italic' }}>
                            Waiting for players to join...
                        </Text>
                    </div>
                </Card>
            </div>
        </MainLayout>
    );
};

export default Lobby;
