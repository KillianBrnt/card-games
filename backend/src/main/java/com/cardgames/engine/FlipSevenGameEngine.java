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

    @Override
    public void initializeGame(Long gameId) {
        // 1. Get Players
        Set<String> playerNames = lobbyService.getPlayers(gameId);
        if (playerNames == null || playerNames.isEmpty()) {
            return;
        }

        // 2. Create Initial State
        FlipSevenState state = new FlipSevenState();
        List<FlipSevenPlayer> players = new ArrayList<>();
        playerNames.forEach(p -> players.add(new FlipSevenPlayer(p)));
        state.setPlayers(players);
        state.setCurrentPlayerIndex(0);

        // 3. Generate and Shuffle Deck
        state.setDeck(generateDeck());

        // 6. Start First Turn (Auto-deal 1 card?)
        // Rules say: "Chaque joueur reçoit 1 carte au début de la manche."
        startNewRound(gameId, state);

        // 4. Save State (Modified by startNewRound)
        saveState(gameId, state);

        // 5. Broadcast Initial State
        broadcastGameState(gameId, state);
    }

    @Override
    public void handleAction(Action action) {
        Long gameId = action.getGameId();
        FlipSevenState state = loadState(gameId);
        if (state == null)
            return;

        String sender = action.getSender();
        Map<String, Object> payload = action.getPayload();
        String type = (String) payload.get("action"); // HIT, STAY, etc.

        // Allow PLAYER_READY from any player regardless of turn
        if ("PLAYER_READY".equals(type)) {
            handlePlayerReady(state, sender, gameId);
            // Backup save/broadcast in case handlePlayerReady didn't trigger new round or
            // failed
            saveState(gameId, state);
            broadcastGameState(gameId, state);
            return;
        }

        // Validate Turn
        FlipSevenPlayer currentPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());
        if (!currentPlayer.getUsername().equals(sender)) {
            return;
        }

        // Block HIT/STAY if pending action selection
        if (state.getPendingActionType() != null && state.getPendingActionInitiator().equals(sender)) {
            if (!"SELECT_TARGET".equals(type)) {
                // Ignore other actions while waiting for target selection
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

    private void handleSelectTarget(FlipSevenState state, FlipSevenPlayer initiator, String targetUsername) {
        if (state.getPendingActionType() == null
                || !initiator.getUsername().equals(state.getPendingActionInitiator())) {
            return; // Invalid
        }

        FlipSevenPlayer target = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(targetUsername))
                .findFirst()
                .orElse(null);

        if (target == null || !target.isRoundActive())
            return;

        if ("FREEZE_SELECTION".equals(state.getPendingActionType())) {
            // Disable the action card from initiator's hand
            disableCardByType(initiator, CardType.ACTION_FREEZE);

            // Freeze target breakdown
            target.setTotalScore(target.getTotalScore() + target.getRoundScore());
            target.setLastRoundScore(target.getRoundScore());
            target.setRoundActive(false);
            // target.getHand().clear(); // Removed to show hand in modal
            target.setRoundScore(0);

            // Clear pending
            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, initiator);

        } else if ("FLIP3_SELECTION".equals(state.getPendingActionType())) {
            // Disable the action card from initiator's hand
            disableCardByType(initiator, CardType.ACTION_FLIP3);

            // Initiate Flip 3 sequence on target
            state.setFlip3ActiveTarget(target.getUsername());
            state.setFlip3DrawsRemaining(3);

            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, target);

        } else if ("GIVE_SECOND_CHANCE".equals(state.getPendingActionType())) {
            // Initiator gives Second Chance status to target
            // Note: The card logic is: Initator keeps card (points), but Target gets
            // status?
            // "il désigne un joueur pour la lui donner".
            // Since Initiator KEEPS their own status (they already had one), the NEW status
            // is given.
            // The NEW status came from the newly drawn card.
            // So Target gets Status = true.
            // Initiator keeps their Status = true.
            // Initiator keeps the card in hand (as points/history).
            // But usually "Second Chance" card has no points? Value 0.

            // Should the card be marked noEffect for Initiator?
            // If it gave the status to someone else, it effectively "worked".
            // So it's fine.

            target.setHasSecondChance(true);

            state.setPendingActionType(null);
            state.setPendingActionInitiator(null);

            checkNextStep(state, initiator);
        }
    }

    private void disableCardByType(FlipSevenPlayer player, CardType type) {
        for (Card c : player.getHand()) {
            if (c.getType() == type && !c.isNoEffect()) {
                c.setNoEffect(true);
                return; // Disable only one
            }
        }
    }

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

        // Queue Special Actions for later
        if (card.getType() == CardType.ACTION_FREEZE) {
            state.getPendingActionQueue().add("FREEZE_SELECTION");
        } else if (card.getType() == CardType.ACTION_FLIP3) {
            state.getPendingActionQueue().add("FLIP3_SELECTION");
        } else if (card.getType() == CardType.ACTION_SECOND_CHANCE) {
            if (target.isHasSecondChance()) {
                state.setPendingActionType("GIVE_SECOND_CHANCE");
                state.setPendingActionInitiator(target.getUsername());
                // Break recursion to wait for action
                target.setRoundScore(calculateScore(target.getHand()));
                return;
            }
            target.setHasSecondChance(true);
        }

        // Check Bust
        if (isBust(target)) {
            if (target.isHasSecondChance()) {
                target.setHasSecondChance(false);
                card.setNoEffect(true);
                // Saved! Continue drawing remaining cards.
                processNextFlip3Card(state, target);
            } else {
                target.setRoundScore(0);
                target.setLastRoundScore(0);
                target.setRoundActive(false); // Eliminated

                state.setFlip3DrawsRemaining(0);
                state.setFlip3ActiveTarget(null);
                state.getPendingActionQueue().clear(); // Clear actions if busted
                advanceTurn(state);
            }
        } else {
            target.setRoundScore(calculateScore(target.getHand()));
            // Continue sequence
            processNextFlip3Card(state, target);
        }
    }

    private void checkNextStep(FlipSevenState state, FlipSevenPlayer activePlayer) {
        // 1. Priority: Finish forced draws
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

        // 2. Priority: Process queued actions from the draws
        // Ensure queue is initialized, though startNewRound does it. Or handle null
        // safety.
        if (state.getPendingActionQueue() != null && !state.getPendingActionQueue().isEmpty()) {
            String nextAction = state.getPendingActionQueue().remove(0);
            state.setPendingActionType(nextAction);
            state.setPendingActionInitiator(activePlayer.getUsername());
            return; // Wait for user selection
        }

        // 3. No more draws, no more actions -> Advance
        state.setFlip3ActiveTarget(null);
        state.setFlip3DrawsRemaining(0);
        advanceTurn(state);
    }

    private void handlePlayerReady(FlipSevenState state, String username, Long gameId) {
        if (!state.isRoundOver())
            return;

        if (!state.getReadyPlayers().contains(username)) {
            state.getReadyPlayers().add(username);
        }

        int required = state.getPlayers().size();

        // If all players are ready, start the new round
        if (state.getReadyPlayers().size() >= required) {
            state.setRoundOver(false);
            state.getReadyPlayers().clear();

            startNewRound(gameId, state);

            // Force save and broadcast to ensure new round state (with dealt cards) is
            // persisted
            saveState(gameId, state);
            broadcastGameState(gameId, state);
        }
    }

    private void handleHit(FlipSevenState state, FlipSevenPlayer player) {
        if (state.getDeck().isEmpty()) {
            // Handle empty deck (reshuffle discard pile? rule check needed. For now just
            // standard end logic?)
            // Assuming infinite deck or just end? Let's just assume big deck for now.
            return;
        }

        Card card = state.getDeck().remove(0);
        player.getHand().add(card);

        // Logic for Specials
        if (card.getType() == CardType.ACTION_FREEZE) {
            state.setPendingActionType("FREEZE_SELECTION");
            state.setPendingActionInitiator(player.getUsername());
            // Do NOT advance turn yet
            // Calculate score (it's in hand now)
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

        // Check Bust
        if (isBust(player)) {
            if (player.isHasSecondChance()) {
                // Consume second chance
                player.setHasSecondChance(false);
                // "ne meurt pas si doublon pourra jouer au prochain tour"
                // Discard duplicate -> Set noEffect
                card.setNoEffect(true);
                // Don't lose turn immediately? Or pass? "pourra jouer au prochain tour" -> Pass
                // turn.
                advanceTurn(state);
            } else {
                player.setRoundScore(0);
                player.setLastRoundScore(0);
                player.setRoundActive(false);
                advanceTurn(state);
            }
        } else {
            // Calculate Score
            player.setRoundScore(calculateScore(player.getHand()));

            // Check Flip 7
            if (checkFlipSeven(player.getHand())) {
                // Bonus +15, auto-stay?
                player.setRoundScore(player.getRoundScore() + 15);
                handleStay(state, player); // Auto stay on Flip 7 completion
                return;
            }

            // Safe Hit -> Advance Turn (User wants: "chacun son tour on pioche une carte")
            advanceTurn(state);
        }
    }

    private void handleStay(FlipSevenState state, FlipSevenPlayer player) {
        player.setTotalScore(player.getTotalScore() + player.getRoundScore());
        player.setLastRoundScore(player.getRoundScore());
        player.setRoundActive(false);
        // Do not clear hand yet, so it can be shown in Round Summary
        // player.getHand().clear();
        player.setRoundScore(0); // Reset round score for next round display?

        advanceTurn(state);
    }

    private void advanceTurn(FlipSevenState state) {
        int initialIndex = state.getCurrentPlayerIndex();
        int numPlayers = state.getPlayers().size();

        // Find next active player
        for (int i = 1; i <= numPlayers; i++) {
            int nextIndex = (initialIndex + i) % numPlayers;
            FlipSevenPlayer p = state.getPlayers().get(nextIndex);
            if (p.isRoundActive()) {
                state.setCurrentPlayerIndex(nextIndex);
                checkInitialHandAction(state, p);
                return; // Found next player
            }
        }

        // If we exit loop, NO active players found (all stayed or busted)
        resolveRound(state);
    }

    private void checkInitialHandAction(FlipSevenState state, FlipSevenPlayer player) {
        // Only trigger if it's the very first card (size 1)
        // If the player has accumulated more cards, this logic shouldn't trigger
        // automatically.
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

    private void resolveRound(FlipSevenState state) {
        state.setRoundOver(true);
        state.getReadyPlayers().clear();

        // Check Victory Condition (200 pts)
        // Rule: The game ends only after the round is fully resolved for all players.
        // If multiple players exceed 200, the one with the highest score wins.
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

    private void startNewRound(Long gameId, FlipSevenState state) {
        // Reshuffle if deck check?
        if (state.getDeck().size() < state.getPlayers().size() * 5) {
            state.setDeck(generateDeck()); // Reshuffle for MVP
        }

        // Reset global flip 3 state
        state.setFlip3DrawsRemaining(0);
        state.setFlip3ActiveTarget(null);

        for (FlipSevenPlayer p : state.getPlayers()) {
            p.setRoundActive(true);
            p.setHasSecondChance(false); // Reset second chance
            p.getHand().clear();
            p.setRoundScore(0);
            p.setLastRoundScore(0); // Reset last round score too? Or keep it for history? Usually reset for new
                                    // round logic.

            if (!state.getDeck().isEmpty()) {
                Card c = state.getDeck().remove(0);
                p.getHand().add(c);

                if (c.getType() == CardType.ACTION_SECOND_CHANCE) {
                    p.setHasSecondChance(true);
                }
            }
            p.setRoundScore(calculateScore(p.getHand()));
        }
        state.setCurrentPlayerIndex(0); // Rotate starting player in future?

        // Check if the starting player has an actionable card immediately
        if (!state.getPlayers().isEmpty()) {
            try {
                checkInitialHandAction(state, state.getPlayers().get(0));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

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
                    // Action cards usually don't add points themselves, or?
                    // "Action: 5 of each...". Assuming 0 value.
                    break;
            }
        }
        return score * multiplier;
    }

    private List<Card> generateDeck() {
        List<Card> deck = new ArrayList<>();
        int idCounter = 0;

        // 1. Numbers 0-12
        // 0 (1x), 1 (1x), 2 (2x), 3 (3x)... 12 (12x)
        // Wait, prompt says: "1 carte 0, 1 carte 1, 2 cartes 2, ... 12 cartes 12"
        // so N copies of value N, except 0 and 1 which are 1 copy?
        // Prompt: "1 carte 0, 1 carte 1, 2 cartes 2, … 12 cartes 12" yes.

        deck.add(new Card(idCounter++ + "", CardType.NUMBER, 0, "0"));
        deck.add(new Card(idCounter++ + "", CardType.NUMBER, 1, "1"));
        for (int i = 2; i <= 12; i++) {
            for (int k = 0; k < i; k++) {
                deck.add(new Card(idCounter++ + "", CardType.NUMBER, i, String.valueOf(i)));
            }
        }

        // 2. Action Cards (5 of each)
        for (int i = 0; i < 3; i++) {
            deck.add(new Card(idCounter++ + "", CardType.ACTION_FREEZE, 0, "Freeze"));
            deck.add(new Card(idCounter++ + "", CardType.ACTION_FLIP3, 0, "Flip 3"));
            deck.add(new Card(idCounter++ + "", CardType.ACTION_SECOND_CHANCE, 0, "Second Chance"));
        }

        // 3. Modifiers (1 of each: +2 to +10 even?, +2, +4... UP TO +10. "oneof each")
        // Prompt: "+2, +4, +6, …, up to +10". So 2, 4, 6, 8, 10.
        // Prompt: "x2 double final score".
        for (int i = 2; i <= 10; i += 2) {
            deck.add(new Card(idCounter++ + "", CardType.MODIFIER_PLUS, i, "+" + i));
        }
        deck.add(new Card(idCounter++ + "", CardType.MODIFIER_MULTIPLY, 0, "x2"));

        Collections.shuffle(deck);
        return deck;
    }

    private void saveState(Long gameId, FlipSevenState state) {

        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(GAME_PREFIX + gameId + ":state", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

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

    private void broadcastGameState(Long gameId, FlipSevenState state) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");
        // We can send the whole state or sanitized view.
        // For MVP, sending whole state is easier, but opponents shouldn't see deck
        // order.

        // Sanitizing deck:
        FlipSevenState sanitized = new FlipSevenState();
        sanitized.setPlayers(state.getPlayers()); // Players see other hands? "Affichage clair des cartes en main"
        // Usually you see opponents cards in Flip 7 (it's public info often).
        sanitized.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        sanitized.setGameCheck(state.isGameCheck());
        // Hide deck
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
