package com.cardgames.engine;

import com.cardgames.websocket.model.Action;

public interface GameEngine {
    void handleAction(Action action);

    void initializeGame(Long gameId);
}
