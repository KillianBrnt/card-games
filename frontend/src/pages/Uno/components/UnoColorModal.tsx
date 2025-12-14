import React from 'react';
import { Modal } from 'antd';

interface UnoColorModalProps {
    open: boolean;
    onColorSelect: (color: string) => void;
}

const UnoColorModal: React.FC<UnoColorModalProps> = ({ open, onColorSelect }) => {
    return (
        <Modal
            title="Choose a Color"
            open={open}
            footer={null}
            closable={false}
            centered
        >
            <div className="color-picker-grid">
                {['RED', 'BLUE', 'GREEN', 'YELLOW'].map(c => (
                    <div
                        key={c}
                        className={`color-btn bg-${c}`}
                        onClick={() => onColorSelect(c)}
                    >{c}</div>
                ))}
            </div>
        </Modal>
    );
};

export default UnoColorModal;
