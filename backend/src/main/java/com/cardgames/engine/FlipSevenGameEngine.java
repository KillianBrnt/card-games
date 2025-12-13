package com.cardgames.engine;

import com.cardgames.websocket.model.Action;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FlipSevenGameEngine implements GameEngine {

    private final SimpMessageSendingOperations messagingTemplate;
    private final StringRedisTemplate redisTemplate;

    private final com.cardgames.service.LobbyService lobbyService;

    private static final String GAME_PREFIX = "game:flipseven:";

    public FlipSevenGameEngine(SimpMessageSendingOperations messagingTemplate, StringRedisTemplate redisTemplate,
            com.cardgames.service.LobbyService lobbyService) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.lobbyService = lobbyService;
    }

    @Override
    public void handleAction(Action action) {
        Long gameId = action.getGameId();
        Map<String, Object> payload = action.getPayload();
        String gameAction = (String) payload.get("action");

        String sender = action.getSender();
        if (sender != null) {
            String scoresKey = GAME_PREFIX + gameId + ":scores";
            if (!Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey(scoresKey, sender))) {
                redisTemplate.opsForHash().put(scoresKey, sender, "0");
            }
        }

        if (Action.ActionType.SYNC_REQUEST.equals(action.getType())) {
            // Fetch current state and send back (broadcast or user-specific? Broadcast is
            // easier)
            String key = GAME_PREFIX + gameId + ":counter";
            String val = redisTemplate.opsForValue().get(key);
            int currentCount = val != null ? Integer.parseInt(val) : 0;

            // Lazy-init scores if missing
            String scoresKey = GAME_PREFIX + gameId + ":scores";
            if (redisTemplate.opsForHash().size(scoresKey) == 0) {
                java.util.Set<String> players = lobbyService.getPlayers(gameId);
                if (players != null && !players.isEmpty()) {
                    Map<String, String> initialScores = new HashMap<>();
                    for (String player : players) {
                        initialScores.put(player, "0");
                    }
                    redisTemplate.opsForHash().putAll(scoresKey, initialScores);
                }
            }

            broadcastGameState(gameId, currentCount);
            return;
        }

        if (gameAction == null)
            return;

        String key = GAME_PREFIX + gameId + ":counter";
        Long currentCount = 0L;

        if ("INCREMENT".equals(gameAction)) {
            currentCount = redisTemplate.opsForValue().increment(key);
        } else if ("DECREMENT".equals(gameAction)) {
            currentCount = redisTemplate.opsForValue().decrement(key);
        } else {
            // Just read current state if other action (or fetch logic)
            String val = redisTemplate.opsForValue().get(key);
            currentCount = val != null ? Long.parseLong(val) : 0L;
        }

        broadcastGameState(gameId, currentCount != null ? currentCount.intValue() : 0);
    }

    @Override
    public void initializeGame(Long gameId) {
        String key = GAME_PREFIX + gameId + ":counter";
        redisTemplate.opsForValue().set(key, "0");

        // Initialize Scores
        String scoresKey = GAME_PREFIX + gameId + ":scores";
        redisTemplate.delete(scoresKey);
        java.util.Set<String> players = lobbyService.getPlayers(gameId);
        if (players != null && !players.isEmpty()) {
            Map<String, String> initialScores = new HashMap<>();
            for (String player : players) {
                initialScores.put(player, "0");
            }
            redisTemplate.opsForHash().putAll(scoresKey, initialScores);
        }

        broadcastGameState(gameId, 0);
    }

    private void broadcastGameState(Long gameId, int count) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");
        payload.put("counter", currentCountOrFetched(gameId, count));

        // Include scores
        String scoresKey = GAME_PREFIX + gameId + ":scores";
        Map<Object, Object> scores = redisTemplate.opsForHash().entries(scoresKey);
        payload.put("scores", scores);

        updateAction.setPayload(payload);

        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/game", updateAction);
    }

    private int currentCountOrFetched(Long gameId, int passedCount) {
        // Optimization: if passedCount is 0 maybe we want to fetch?
        // Actually, let's just trust passed count for now or re-fetch to be safe if
        // broadcasting from initialize.
        return passedCount;
    }
}
