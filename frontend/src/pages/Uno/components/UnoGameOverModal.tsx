import React from 'react';
import { Modal, Button, Typography } from 'antd';

const { Title } = Typography;

interface UnoGameOverModalProps {
    open: boolean;
    winner: string | null;
}

const UnoGameOverModal: React.FC<UnoGameOverModalProps> = ({ open, winner }) => {
    return (
        <Modal
            open={open}
            footer={[<Button key="home" href="/">Home</Button>]}
            closable={false}
        >
            <Title level={2}>Difficile! {winner} Wins!</Title>
        </Modal>
    );
};

export default UnoGameOverModal;
