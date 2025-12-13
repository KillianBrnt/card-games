import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector } from 'react-redux';
import { Card, Input, Button, Form, Typography, message } from 'antd';
import type { RootState } from '../../store';
import { gameService } from '../../services/gameService';
import MainLayout from '../../components/MainLayout';
import './JoinGame.css';

const { Title } = Typography;

const JoinGame: React.FC = () => {
    const navigate = useNavigate();
    const [gameCode, setGameCode] = useState('');
    const [isLoading, setIsLoading] = useState(false);
    const user = useSelector((state: RootState) => state.auth.user);

    useEffect(() => {
        if (!user) {
            navigate('/login');
        }
    }, [user, navigate]);

    const handleJoin = async () => {
        setIsLoading(true);
        try {
            const response = await gameService.joinGame(gameCode.toUpperCase());
            navigate(`/lobby?gameId=${response.gameId}&gameType=${response.gameType}`);
        } catch (err: any) {
            message.error(err.message || 'Failed to join game');
        } finally {
            setIsLoading(false);
        }
    };

    return (
        <MainLayout>
            <div className="join-container-content">
                <Card className="join-card-antd" bordered={false}>
                    <Title level={2} style={{ marginBottom: '2rem' }}>
                        Join a Game
                    </Title>
                    <Form onFinish={handleJoin}>
                        <Form.Item>
                            <Input
                                placeholder="ENTER CODE"
                                value={gameCode}
                                onChange={(e) => setGameCode(e.target.value)}
                                maxLength={6}
                                className="game-input-antd"
                            />
                        </Form.Item>
                        <Form.Item>
                            <Button
                                type="primary"
                                htmlType="submit"
                                block
                                loading={isLoading}
                                className="join-button-antd"
                                disabled={!gameCode}
                            >
                                {isLoading ? 'JOINING...' : 'JOIN GAME'}
                            </Button>
                        </Form.Item>
                    </Form>
                </Card>
            </div>
        </MainLayout>
    );
};

export default JoinGame;
