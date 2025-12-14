package com.cardgames.engine;

import com.cardgames.websocket.model.Action;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GameEngineHandler {

    private final Map<String, GameEngine> engines = new ConcurrentHashMap<>();

    // Inject engines using constructor or setter, or let Spring collect them if we
    // implement a registry pattern.
    // simpler: inject known engines.
    public GameEngineHandler(FlipSevenGameEngine flipSevenGameEngine, UnoGameEngine unoGameEngine) {
        engines.put("FLIP_SEVEN", flipSevenGameEngine);
        engines.put("UNO", unoGameEngine);
    }

    public void handleAction(Action action) {
        // getGameType from Action. Assuming frontend sends it.
        // If not, we might need to look it up from specific gameId, but let's assume
        // valid action.
        String gameType = action.getGameType();
        if (gameType != null && engines.containsKey(gameType)) {
            engines.get(gameType).handleAction(action);
        } else {
            // Default or Error handling?
            // For now, if no type, maybe fallback to FLIP_SEVEN for dev if only one engine?
            // Or log warning.
            if (engines.size() == 1 && gameType == null) {
                engines.values().iterator().next().handleAction(action);
            }
        }
    }

    public void initializeGame(String gameType, Long gameId) {
        if (gameType != null && engines.containsKey(gameType)) {
            engines.get(gameType).initializeGame(gameId);
        }
    }
}
