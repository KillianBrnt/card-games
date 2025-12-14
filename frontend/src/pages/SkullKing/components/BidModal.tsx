import React from 'react';
import './BidModal.css';

interface BidModalProps {
    roundNumber: number;
    onBid: (bid: number) => void;
}

const BidModal: React.FC<BidModalProps> = ({ roundNumber, onBid }) => {
    const possibleBids = Array.from({ length: roundNumber + 1 }, (_, i) => i);

    return (
        <div className="sk-bid-modal-overlay">
            <div className="sk-bid-modal-content">
                <h2 className="sk-bid-title">Place Your Bid!</h2>
                <p className="sk-bid-subtitle">How many tricks will you take?</p>

                <div className="sk-bid-grid">
                    {possibleBids.map((num) => (
                        <button
                            key={num}
                            onClick={() => onBid(num)}
                            className={`sk-bid-btn ${num === 0 ? 'sk-bid-btn-zero' : 'sk-bid-btn-normal'}`}
                        >
                            {num === 0 ? 'Zero (0)' : num}
                        </button>
                    ))}
                </div>
            </div>
        </div>
    );
};

export default BidModal;
