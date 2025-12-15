package com.cardgames.engine;

import com.cardgames.model.flipseven.Card;
import com.cardgames.model.flipseven.CardType;
import com.cardgames.model.flipseven.FlipSevenPlayer;
import com.cardgames.model.flipseven.FlipSevenState;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FlipSevenGameEngine implements GameEngine {

    private final SimpMessageSendingOperations messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final LobbyService lobbyService;
    private final ObjectMapper objectMapper;

    private static final String GAME_PREFIX = "game:flipseven:";

    public FlipSevenGameEngine(SimpMessageSendingOperations messagingTemplate, StringRedisTemplate redisTemplate,
            LobbyService lobbyService, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.lobbyService = lobbyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Initializes the game by setting up players, deck, and initial state.
     *
     * @param gameId The ID of the game to initialize.
     */
    @Override
    public void initializeGame(Long gameId) {
        Set<String> playerNames = lobbyService.getPlayers(gameId);
        if (playerNames == null || playerNames.isEmpty()) {
            return;
        }

        FlipSevenState state = new FlipSevenState();
        List<FlipSevenPlayer> players = new ArrayList<>();
        playerNames.forEach(p -> players.add(new FlipSevenPlayer(p)));
        state.setPlayers(players);
        state.setCurrentPlayerIndex(0);
        state.setRoundStarterIndex(0);

        state.setDeck(generateDeck());

        startNewRound(gameId, state);

        saveState(gameId, state);

        broadcastGameState(gameId, state);
    }

    /**
     * Processes incoming actions from players, such as hit, stay, or using special
     * cards.
     *
     * @param action The action received from the client.
     */
    @Override
    public void handleAction(Action action) {
        Long gameId = action.getGameId();
        FlipSevenState state = loadState(gameId);
        if (state == null)
            return;

        String sender = action.getSender();
        Map<String, Object> payload = action.getPayload();
        String type = (String) payload.get("action");

        if ("PLAYER_READY".equals(type)) {
            handlePlayerReady(state, sender, gameId);
            saveState(gameId, state);
            broadcastGameState(gameId, state);
            return;
        }

        FlipSevenPlayer currentPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());
        if (!currentPlayer.getUsername().equals(sender)) {
            return;
        }

        if (state.getPendingActionType() != null && state.getPendingActionInitiator().equals(sender)) {
            if (!"SELECT_TARGET".equals(type)) {
                return;
            }
        }

        boolean stateChanged = false;

        if ("HIT".equals(type)) {
            handleHit(state, currentPlayer);
            stateChanged = true;
        } else if ("STAY".equals(type)) {
            handleStay(state, currentPlayer);
            stateChanged = true;
        } else if ("SELECT_TARGET".equals(type)) {
            handleSelectTarget(state, currentPlayer, (String) payload.get("target"));
            stateChanged = true;
        } else if ("SYNC_REQUEST".equals(type)) {
            broadcastGameState(gameId, state);
            return;
        }

        if (stateChanged) {
            saveState(gameId, state);
            broadcastGameState(gameId, state);
        }
    }

    /**
     * Handles the selection of a target player for specific action cards.
     *
     * @param state          The current game state.
     * @param initiator      The player initiating the action.
     * @param targetUsername The username of the target player.
     */
    private void handleSelectTarget(FlipSevenState state, FlipSevenPlayer initiator, String targetUsername) {
        if (state.getPendingActionType() == null
                || !initiator.getUsername().equals(state.getPendingActionInitiator())) {
            return;
        }

        FlipSevenPlayer target = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(targetUsername))
                .findFirst()
                .orElse(null);

        if (target == null || !target.isRoundActive())
            return;

        if ("FREEZE_SELECTION".equals(state.getPendingActionType())) {
            disableCardByType(initiator, CardType.ACTION_FREEZE);

            target.setTotalScore(target.getTotalScore() + target.getRoundScore());
            target.setLastRoundScore(target.getRoundScore());
            target.setRoundActive(false);
            target.setRoundScore(0);

            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, initiator);

        } else if ("FLIP3_SELECTION".equals(state.getPendingActionType())) {
            disableCardByType(initiator, CardType.ACTION_FLIP3);

            state.setFlip3ActiveTarget(target.getUsername());
            state.setFlip3DrawsRemaining(3);

            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, target);

        } else if ("GIVE_SECOND_CHANCE".equals(state.getPendingActionType())) {
            target.setHasSecondChance(true);

            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, initiator);
        }
    }

    /**
     * Disables the effect of a specific card type in a player's hand.
     *
     * @param player The player whose card should be disabled.
     * @param type   The type of card to disable.
     */
    private void disableCardByType(FlipSevenPlayer player, CardType type) {
        for (Card c : player.getHand()) {
            if (c.getType() == type && !c.isNoEffect()) {
                c.setNoEffect(true);
                return;
            }
        }
    }

    /**
     * Processes the drawing of cards during a Flip 3 action.
     *
     * @param state  The current game state.
     * @param target The player being targeted by the Flip 3 action.
     */
    private void processNextFlip3Card(FlipSevenState state, FlipSevenPlayer target) {
        if (state.getFlip3DrawsRemaining() <= 0) {
            checkNextStep(state, target);
            return;
        }

        if (state.getDeck().isEmpty()) {
            state.setFlip3DrawsRemaining(0);
            checkNextStep(state, target);
            return;
        }

        Card card = state.getDeck().remove(0);
        target.getHand().add(card);
        state.setFlip3DrawsRemaining(state.getFlip3DrawsRemaining() - 1);

        if (card.getType() == CardType.ACTION_FREEZE) {
            state.getPendingActionQueue().add("FREEZE_SELECTION");
        } else if (card.getType() == CardType.ACTION_FLIP3) {
            state.getPendingActionQueue().add("FLIP3_SELECTION");
        } else if (card.getType() == CardType.ACTION_SECOND_CHANCE) {
            if (target.isHasSecondChance()) {
                state.setPendingActionType("GIVE_SECOND_CHANCE");
                state.setPendingActionInitiator(target.getUsername());
                target.setRoundScore(calculateScore(target.getHand()));
                return;
            }
            target.setHasSecondChance(true);
        }

        if (isBust(target)) {
            if (target.isHasSecondChance()) {
                target.setHasSecondChance(false);
                card.setNoEffect(true);
                processNextFlip3Card(state, target);
            } else {
                target.setRoundScore(0);
                target.setLastRoundScore(0);
                target.setRoundActive(false);

                state.setFlip3DrawsRemaining(0);
                state.setFlip3ActiveTarget(null);
                state.getPendingActionQueue().clear();
                advanceTurn(state);
            }
        } else {
            target.setRoundScore(calculateScore(target.getHand()));
            processNextFlip3Card(state, target);
        }
    }

    /**
     * Checks what the next step in the game flow should be.
     *
     * @param state        The current game state.
     * @param activePlayer The currently active player.
     */
    private void checkNextStep(FlipSevenState state, FlipSevenPlayer activePlayer) {
        if (state.getFlip3DrawsRemaining() > 0) {
            String activeTargetName = state.getFlip3ActiveTarget();
            if (activeTargetName != null) {
                FlipSevenPlayer target = state.getPlayers().stream()
                        .filter(p -> p.getUsername().equals(activeTargetName))
                        .findFirst().orElse(null);
                if (target != null) {
                    processNextFlip3Card(state, target);
                    return;
                }
            }
        }

        if (state.getPendingActionQueue() != null && !state.getPendingActionQueue().isEmpty()) {
            String nextAction = state.getPendingActionQueue().remove(0);
            state.setPendingActionType(nextAction);
            state.setPendingActionInitiator(activePlayer.getUsername());
            return;
        }

        state.setFlip3ActiveTarget(null);
        state.setFlip3DrawsRemaining(0);
        advanceTurn(state);
    }

    /**
     * Marks a player as ready and starts a new round if all players are ready.
     *
     * @param state    The current game state.
     * @param username The username of the ready player.
     * @param gameId   The ID of the game.
     */
    private void handlePlayerReady(FlipSevenState state, String username, Long gameId) {
        if (!state.isRoundOver())
            return;

        if (!state.getReadyPlayers().contains(username)) {
            state.getReadyPlayers().add(username);
        }

        int required = state.getPlayers().size();

        if (state.getReadyPlayers().size() >= required) {
            state.setRoundOver(false);
            state.getReadyPlayers().clear();

            int nextStarter = (state.getRoundStarterIndex() + 1) % state.getPlayers().size();
            state.setRoundStarterIndex(nextStarter);

            startNewRound(gameId, state);

            saveState(gameId, state);
            broadcastGameState(gameId, state);
        }
    }

    /**
     * Handles the 'Hit' action where a player draws a card.
     *
     * @param state  The current game state.
     * @param player The player performing the action.
     */
    private void handleHit(FlipSevenState state, FlipSevenPlayer player) {
        if (state.getDeck().isEmpty()) {
            return;
        }

        Card card = state.getDeck().remove(0);
        player.getHand().add(card);

        if (card.getType() == CardType.ACTION_FREEZE) {
            state.setPendingActionType("FREEZE_SELECTION");
            state.setPendingActionInitiator(player.getUsername());
            player.setRoundScore(calculateScore(player.getHand()));
            return;
        } else if (card.getType() == CardType.ACTION_FLIP3) {
            state.setPendingActionType("FLIP3_SELECTION");
            state.setPendingActionInitiator(player.getUsername());
            player.setRoundScore(calculateScore(player.getHand()));
            return;
        } else if (card.getType() == CardType.ACTION_SECOND_CHANCE) {
            if (player.isHasSecondChance()) {
                state.setPendingActionType("GIVE_SECOND_CHANCE");
                state.setPendingActionInitiator(player.getUsername());
                player.setRoundScore(calculateScore(player.getHand()));
                return;
            }
            player.setHasSecondChance(true);
        }

        if (isBust(player)) {
            if (player.isHasSecondChance()) {
                player.setHasSecondChance(false);
                card.setNoEffect(true);
                advanceTurn(state);
            } else {
                player.setRoundScore(0);
                player.setLastRoundScore(0);
                player.setRoundActive(false);
                advanceTurn(state);
            }
        } else {
            player.setRoundScore(calculateScore(player.getHand()));

            if (checkFlipSeven(player.getHand())) {
                player.setRoundScore(player.getRoundScore() + 15);
                handleStay(state, player);
                return;
            }

            advanceTurn(state);
        }
    }

    /**
     * Handles the 'Stay' action where a player ends their turn for the round.
     *
     * @param state  The current game state.
     * @param player The player performing the action.
     */
    private void handleStay(FlipSevenState state, FlipSevenPlayer player) {
        player.setTotalScore(player.getTotalScore() + player.getRoundScore());
        player.setLastRoundScore(player.getRoundScore());
        player.setRoundActive(false);
        player.setRoundScore(0);

        advanceTurn(state);
    }

    /**
     * Advances the turn to the next active player.
     *
     * @param state The current game state.
     */
    private void advanceTurn(FlipSevenState state) {
        int initialIndex = state.getCurrentPlayerIndex();
        int numPlayers = state.getPlayers().size();

        for (int i = 1; i <= numPlayers; i++) {
            int nextIndex = (initialIndex + i) % numPlayers;
            FlipSevenPlayer p = state.getPlayers().get(nextIndex);
            if (p.isRoundActive()) {
                state.setCurrentPlayerIndex(nextIndex);
                checkInitialHandAction(state, p);
                return;
            }
        }

        resolveRound(state);
    }

    /**
     * Checks if the player's initial hand requires any immediate action.
     *
     * @param state  The current game state.
     * @param player The player to check.
     */
    private void checkInitialHandAction(FlipSevenState state, FlipSevenPlayer player) {
        if (player.getHand().size() == 1) {
            Card c = player.getHand().get(0);
            if (c.isNoEffect())
                return;
            if (c.getType() == CardType.ACTION_FREEZE) {
                state.setPendingActionType("FREEZE_SELECTION");
                state.setPendingActionInitiator(player.getUsername());
            } else if (c.getType() == CardType.ACTION_FLIP3) {
                state.setPendingActionType("FLIP3_SELECTION");
                state.setPendingActionInitiator(player.getUsername());
            }
        }
    }

    /**
     * Ends the round and checks for a game winner.
     *
     * @param state The current game state.
     */
    private void resolveRound(FlipSevenState state) {
        state.setRoundOver(true);
        state.getReadyPlayers().clear();

        FlipSevenPlayer potentialWinner = null;
        for (FlipSevenPlayer p : state.getPlayers()) {
            if (p.getTotalScore() >= 200) {
                if (potentialWinner == null || p.getTotalScore() > potentialWinner.getTotalScore()) {
                    potentialWinner = p;
                }
            }
        }

        if (potentialWinner != null) {
            state.setWinner(potentialWinner.getUsername());
            state.setGameOver(true);
        }
    }

    /**
     * Resets the game state for a new round.
     *
     * @param gameId The ID of the game.
     * @param state  The current game state.
     */
    private void startNewRound(Long gameId, FlipSevenState state) {
        if (state.getDeck().size() < state.getPlayers().size() * 5) {
            state.setDeck(generateDeck());
        }

        state.setFlip3DrawsRemaining(0);
        state.setFlip3ActiveTarget(null);

        for (FlipSevenPlayer p : state.getPlayers()) {
            p.setRoundActive(true);
            p.setHasSecondChance(false);
            p.getHand().clear();
            p.setRoundScore(0);
            p.setLastRoundScore(0);

            if (!state.getDeck().isEmpty()) {
                Card c = state.getDeck().remove(0);
                p.getHand().add(c);

                if (c.getType() == CardType.ACTION_SECOND_CHANCE) {
                    p.setHasSecondChance(true);
                }
            }
            p.setRoundScore(calculateScore(p.getHand()));
        }
        state.setCurrentPlayerIndex(state.getRoundStarterIndex());

        if (!state.getPlayers().isEmpty()) {
            try {
                checkInitialHandAction(state, state.getPlayers().get(state.getCurrentPlayerIndex()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Checks if a player has busted by having duplicate numbers.
     *
     * @param player The player to check.
     * @return true if the player has busted, false otherwise.
     */
    private boolean isBust(FlipSevenPlayer player) {
        Set<Integer> numbers = new HashSet<>();
        for (Card c : player.getHand()) {
            if (c.isNoEffect())
                continue;
            if (c.getType() == CardType.NUMBER) {
                if (numbers.contains(c.getValue()))
                    return true;
                numbers.add(c.getValue());
            }
        }
        return false;
    }

    /**
     * Checks if the player has collected 7 unique numbers.
     *
     * @param hand The player's hand.
     * @return true if the player has achieved Flip 7, false otherwise.
     */
    private boolean checkFlipSeven(List<Card> hand) {
        Set<Integer> numbers = new HashSet<>();
        for (Card c : hand) {
            if (c.isNoEffect())
                continue;
            if (c.getType() == CardType.NUMBER) {
                numbers.add(c.getValue());
            }
        }
        return numbers.size() >= 7;
    }

    /**
     * Calculates the score of a hand based on card values and multipliers.
     *
     * @param hand The hand of cards to calculate score for.
     * @return The calculated score.
     */
    private int calculateScore(List<Card> hand) {
        int score = 0;
        int multiplier = 1;
        for (Card c : hand) {
            if (c.isNoEffect())
                continue;
            switch (c.getType()) {
                case NUMBER:
                    score += c.getValue();
                    break;
                case MODIFIER_PLUS:
                    score += c.getValue();
                    break;
                case MODIFIER_MULTIPLY:
                    multiplier *= 2;
                    break;
                default:
                    break;
            }
        }
        return score * multiplier;
    }

    /**
     * Generates a new shuffled deck of cards.
     *
     * @return A list of Cards representing the deck.
     */
    private List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        int idCounter = 0;

        deck.add(new Card(idCounter++ + "", CardType.NUMBER, 0, "0"));
        deck.add(new Card(idCounter++ + "", CardType.NUMBER, 1, "1"));
        for (int i = 2; i <= 12; i++) {
            for (int k = 0; k < i; k++) {
                deck.add(new Card(idCounter++ + "", CardType.NUMBER, i, String.valueOf(i)));
            }
        }

        for (int i = 0; i < 3; i++) {
            deck.add(new Card(idCounter++ + "", CardType.ACTION_FREEZE, 0, "Freeze"));
            deck.add(new Card(idCounter++ + "", CardType.ACTION_FLIP3, 0, "Flip 3"));
            deck.add(new Card(idCounter++ + "", CardType.ACTION_SECOND_CHANCE, 0, "Second Chance"));
        }

        for (int i = 2; i <= 10; i += 2) {
            deck.add(new Card(idCounter++ + "", CardType.MODIFIER_PLUS, i, "+" + i));
        }
        deck.add(new Card(idCounter++ + "", CardType.MODIFIER_MULTIPLY, 0, "x2"));

        Collections.shuffle(deck);
        return deck;
    }

    /**
     * Persists the game state to Redis.
     *
     * @param gameId The ID of the game.
     * @param state  The game state to save.
     */
    private void saveState(Long gameId, FlipSevenState state) {

        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(GAME_PREFIX + gameId + ":state", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves the game state from Redis.
     *
     * @param gameId The ID of the game.
     * @return The current FlipSevenState, or null if not found.
     */
    private FlipSevenState loadState(Long gameId) {
        String json = redisTemplate.opsForValue().get(GAME_PREFIX + gameId + ":state");
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, FlipSevenState.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sends the current game state to all clients via WebSocket.
     *
     * @param gameId The ID of the game.
     * @param state  The game state to broadcast.
     */
    private void broadcastGameState(Long gameId, FlipSevenState state) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");

        FlipSevenState sanitized = new FlipSevenState();
        sanitized.setPlayers(state.getPlayers());
        sanitized.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        sanitized.setGameCheck(state.isGameCheck());
        sanitized.setDeck(Collections.emptyList());
        sanitized.setPendingActionType(state.getPendingActionType());
        sanitized.setPendingActionInitiator(state.getPendingActionInitiator());
        sanitized.setRoundOver(state.isRoundOver());
        sanitized.setReadyPlayers(state.getReadyPlayers());
        sanitized.setWinner(state.getWinner());
        sanitized.setGameOver(state.isGameOver());

        payload.put("gameState", sanitized);

        updateAction.setPayload(payload);
        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/game", updateAction);
    }
}
