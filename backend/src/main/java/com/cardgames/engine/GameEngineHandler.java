package com.cardgames.engine;

import com.cardgames.websocket.model.Action;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEngineHandler {

    private final Map<String, GameEngine> engines = new ConcurrentHashMap<>();

    public GameEngineHandler(FlipSevenGameEngine flipSevenGameEngine, UnoGameEngine unoGameEngine,
            SkullKingGameEngine skullKingGameEngine) {
        engines.put("FLIP_SEVEN", flipSevenGameEngine);
        engines.put("UNO", unoGameEngine);
        engines.put("SKULL_KING", skullKingGameEngine);
    }

    /**
     * Delegates the action to the appropriate game engine based on the game type in
     * the action.
     *
     * @param action The action to be handled.
     */
    public void handleAction(Action action) {
        String gameType = action.getGameType();
        if (gameType != null && engines.containsKey(gameType)) {
            engines.get(gameType).handleAction(action);
        } else {
            if (engines.size() == 1 && gameType == null) {
                engines.values().iterator().next().handleAction(action);
            }
        }
    }

    /**
     * Initializes a game session for a specific game type.
     *
     * @param gameType The type of game to initialize (e.g., FLIP_SEVEN, UNO).
     * @param gameId   The ID of the game to initialize.
     */
    public void initializeGame(String gameType, Long gameId) {
        if (gameType != null && engines.containsKey(gameType)) {
            engines.get(gameType).initializeGame(gameId);
        }
    }
}
