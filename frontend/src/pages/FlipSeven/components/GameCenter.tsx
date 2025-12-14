import { Typography, Tag, Alert, Button } from 'antd';
import { StopOutlined } from '@ant-design/icons';
import FlipSevenDeck from './FlipSevenDeck';
import type { FlipSevenGameState } from '../../../types/flipSeven';

const { Title, Text } = Typography;

interface GameCenterProps {
    game: FlipSevenGameState | null;
    currentUsername: string | undefined;
    onHit: () => void;
    onStay: () => void;
    canInteract: boolean;
}

const GameCenter: React.FC<GameCenterProps> = ({ game, currentUsername, onHit, onStay, canInteract }) => {

    if (!game) {
        return (
            <div style={{ display: 'flex', justifyContent: 'center', alignItems: 'center', height: '200px' }}>
                <Text style={{ color: 'white' }}>Waiting for game state...</Text>
            </div>
        );
    }

    const isPendingAction = !!game.pendingActionType;
    const isMeSelecting = isPendingAction && game.pendingActionInitiator === currentUsername;
    const isMyTurn = game.players[game.currentPlayerIndex]?.username === currentUsername;

    return (
        <div style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            background: 'rgba(0,0,0,0.2)', // Semi-transparent dark background
            backdropFilter: 'blur(10px)',
            borderRadius: '20px',
            padding: '20px',
            margin: '10px 0',
            border: '1px solid rgba(255,255,255,0.1)'
        }}>

            <div style={{ marginBottom: '20px', textAlign: 'center' }}>
                <Text style={{ color: 'rgba(255,255,255,0.8)', fontSize: '1rem', textTransform: 'uppercase', letterSpacing: '1px' }}>
                    Current Turn
                </Text>
                <div>
                    <Tag color="geekblue" style={{ fontSize: '1.5rem', padding: '8px 20px', borderRadius: '8px', marginTop: '5px' }}>
                        {game.players[game.currentPlayerIndex]?.username}
                    </Tag>
                </div>
            </div>

            {/* The Deck & Controls Area */}
            <div style={{ display: 'flex', alignItems: 'center', gap: '40px', margin: '20px 0' }}>

                {/* Deck - Clickable to Draw */}
                <div style={{ transform: 'scale(1.2)' }}>
                    <FlipSevenDeck
                        onClick={onHit}
                        disabled={!canInteract || !isMyTurn}
                    />
                </div>

                {/* Stop Button */}
                <Button
                    type="primary"
                    shape="circle"
                    danger
                    icon={<StopOutlined style={{ fontSize: '24px' }} />}
                    style={{
                        width: '80px',
                        height: '80px',
                        boxShadow: '0 4px 15px rgba(255, 77, 79, 0.5)',
                        border: '4px solid white',
                        opacity: (canInteract && isMyTurn) ? 1 : 0.5
                    }}
                    disabled={!canInteract || !isMyTurn}
                    onClick={onStay}
                    title="Stop / Stay"
                />
            </div>


            {/* Notifications / Pending Actions */}
            {isPendingAction && (
                <div className="animate-pulse" style={{ width: '100%', maxWidth: '400px', marginTop: '20px' }}>
                    <Alert
                        message={
                            <div style={{ textAlign: 'center' }}>
                                <Title level={4} style={{ margin: 0, color: isMeSelecting ? '#f5222d' : '#faad14' }}>
                                    {isMeSelecting ? "SELECT A TARGET PLAYER!" : `${game.pendingActionInitiator} IS SELECTING A TARGET...`}
                                </Title>
                                <Text strong>{game.pendingActionType?.replace('_SELECTION', '')} EFFECT</Text>
                            </div>
                        }
                        type={isMeSelecting ? "error" : "warning"}
                        showIcon={false}
                        style={{
                            border: isMeSelecting ? '2px solid red' : '2px solid orange',
                            borderRadius: '12px',
                            background: '#fff'
                        }}
                    />
                </div>
            )}
        </div>
    );
};

export default GameCenter;
