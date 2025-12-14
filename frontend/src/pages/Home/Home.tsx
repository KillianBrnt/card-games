import React, { useState } from 'react';
import { Row, Col } from 'antd';
import { PlayCircleOutlined, LoginOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { gameService } from '../../services/gameService';
import MainLayout from '../../components/MainLayout';
import './Home.css';

const Home: React.FC = () => {
  const navigate = useNavigate();
  const [creatingGame, setCreatingGame] = useState(false);

  const handleCreateGame = async (gameType: string) => {
    setCreatingGame(true);
    try {
      const response = await gameService.createGame(gameType);
      navigate(`/lobby?gameId=${response.gameId}&gameType=${response.gameType}`, {
        state: { gameCode: response.gameCode }
      });
    } catch (error) {
      console.error("Failed to create game", error);
    } finally {
      setCreatingGame(false);
    }
  };

  return (
    <MainLayout>
      <div className="home-content">
        <h1 className="main-title">Pick a game</h1>
        <Row gutter={[32, 32]} justify="center" align="middle" style={{ alignItems: 'stretch' }}>
          <Col xs={24} sm={12} md={8} lg={6} style={{ display: 'flex', flexDirection: 'column' }}>
            <div className="game-card home-flip7-card" onClick={() => handleCreateGame('FLIP_SEVEN')}>
              <div className="card-overlay"></div>
              <div className="card-content">
                <PlayCircleOutlined className="card-icon" />
                <h2>FLIP 7</h2>
                <p>Push your luck, flip the cards!</p>
                {creatingGame && <div className="loading-spinner"></div>}
              </div>
            </div>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6} style={{ display: 'flex', flexDirection: 'column' }}>
            <div className="game-card uno-card-home" onClick={() => handleCreateGame('UNO')}>
              <div className="card-overlay"></div>
              <div className="card-content">
                <PlayCircleOutlined className="card-icon" />
                <h2>UNO</h2>
                <p>Classic fun, watch out for +4!</p>
                {creatingGame && <div className="loading-spinner"></div>}
              </div>
            </div>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6} style={{ display: 'flex', flexDirection: 'column' }}>
            <div className="game-card skull-king-card" onClick={() => handleCreateGame('SKULL_KING')}>
              <div className="card-overlay"></div>
              <div className="card-content">
                <span className="card-icon" style={{ fontSize: '4rem' }}>🏴‍☠️</span>
                <h2>SKULL KING</h2>
                <p>Bid correctly or walk the plank!</p>
                {creatingGame && <div className="loading-spinner"></div>}
              </div>
            </div>
          </Col>
          <Col xs={24} sm={12} md={8} lg={6} style={{ display: 'flex', flexDirection: 'column' }}>
            <div className="game-card join-card" onClick={() => navigate('/join-game')}>
              <div className="card-overlay"></div>
              <div className="card-content">
                <LoginOutlined className="card-icon" />
                <h2>Join Game</h2>
                <p>Enter a code to join your friends</p>
              </div>
            </div>
          </Col>
        </Row>
      </div>
    </MainLayout>
  );
};

export default Home;
