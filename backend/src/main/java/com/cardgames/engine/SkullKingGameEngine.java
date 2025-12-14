package com.cardgames.engine;

import com.cardgames.model.skullking.*;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SkullKingGameEngine implements GameEngine {

    private final SimpMessageSendingOperations messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final LobbyService lobbyService;
    private final ObjectMapper objectMapper;

    private static final String GAME_PREFIX = "game:skullking:";

    public SkullKingGameEngine(SimpMessageSendingOperations messagingTemplate, StringRedisTemplate redisTemplate,
            LobbyService lobbyService, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.lobbyService = lobbyService;
        this.objectMapper = objectMapper;
    }

    @Override
    public void initializeGame(Long gameId) {
        Set<String> playerNames = lobbyService.getPlayers(gameId);
        if (playerNames == null || playerNames.isEmpty())
            return;

        SkullKingState state = new SkullKingState();
        List<SkullKingPlayer> players = new ArrayList<>();
        playerNames.forEach(p -> players.add(new SkullKingPlayer(p)));
        state.setPlayers(players);
        state.setCurrentPlayerIndex(0);
        state.setTrickStarterIndex(0);
        state.setDeck(generateDeck());
        state.setRoundNumber(1);
        state.setPhase("BIDDING");

        dealCards(state);

        saveState(gameId, state);
        broadcastGameState(gameId, state);
    }

    @Override
    public void handleAction(Action action) {
        Long gameId = action.getGameId();
        SkullKingState state = loadState(gameId);
        if (state == null)
            return;

        String sender = action.getSender();
        Map<String, Object> payload = action.getPayload();
        String type = (String) payload.get("action");

        boolean stateChanged = false;

        if ("BID".equals(type)) {
            int bid = (int) payload.get("bid");
            stateChanged = handleBid(state, sender, bid);
        } else if ("PLAY_CARD".equals(type)) {
            String cardId = (String) payload.get("cardId");
            stateChanged = handlePlayCard(state, sender, cardId);
        } else if ("PLAYER_READY".equals(type)) {
            stateChanged = handlePlayerReady(state, sender);
        } else if ("NEXT_ROUND".equals(type)) {
            // Deprecated or host-force? Let's keep for fallback but main flow is
            // PLAYER_READY
            stateChanged = startNextRound(state);
        }

        if (stateChanged) {
            saveState(gameId, state);
            broadcastGameState(gameId, state);
        }
    }

    private boolean handleBid(SkullKingState state, String sender, int bid) {
        if (!"BIDDING".equals(state.getPhase()))
            return false;

        SkullKingPlayer player = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(sender))
                .findFirst().orElse(null);

        if (player == null)
            return false;

        // Allow re-bidding? Or lock? Usually simultaneous.
        // Let's assume lock once bid.
        if (player.getBid() != null)
            return false;

        player.setBid(bid);

        // Check if all bid
        if (state.getPlayers().stream().allMatch(p -> p.getBid() != null)) {
            state.setPhase("PLAYING");
            state.setCurrentPlayerIndex(state.getTrickStarterIndex());
        }
        return true;
    }

    private boolean handlePlayerReady(SkullKingState state, String sender) {
        if (!"ROUND_OVER".equals(state.getPhase()) && !"TRICK_OVER".equals(state.getPhase()))
            return false;

        if (!state.getReadyPlayers().contains(sender)) {
            state.getReadyPlayers().add(sender);
        }

        // Check if all players are ready
        long playerCount = state.getPlayers().size();
        if (state.getReadyPlayers().size() >= playerCount) {
            if ("ROUND_OVER".equals(state.getPhase())) {
                startNextRound(state);
            } else if ("TRICK_OVER".equals(state.getPhase())) {
                startNextTrick(state);
            }
        }
        return true;
    }

    private void startNextTrick(SkullKingState state) {
        // Clear cards
        for (SkullKingPlayer p : state.getPlayers()) {
            p.setCardPlayed(null);
        }
        state.setTrickWinner(null);
        state.getReadyPlayers().clear();

        // Check if Round is Over (Hand empty)
        // We know who won the trick, they are the new starter.
        // But we need to find who that is again or store it properly.
        // We stored trickWinner name.

        // Find winner object
        // Actually we already updated tricksWon in resolveTrick.
        // We need to set currentPlayerIndex to the trick winner.
        // But wait, resolveTrick sets trickStarterIndex to winner.
        // So currentPlayerIndex should be set to trickStarterIndex.

        state.setCurrentPlayerIndex(state.getTrickStarterIndex());

        SkullKingPlayer starter = state.getPlayers().get(state.getTrickStarterIndex());

        if (starter.getHand().isEmpty()) {
            calculateRoundScores(state);
            state.setPhase("ROUND_OVER");
            if (state.getRoundNumber() >= 10) {
                determineGameWinner(state);
            }
        } else {
            state.setPhase("PLAYING");
        }
    }

    private boolean handlePlayCard(SkullKingState state, String sender, String cardId) {
        if (!"PLAYING".equals(state.getPhase()))
            return false;

        SkullKingPlayer currentPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());
        if (!currentPlayer.getUsername().equals(sender))
            return false;

        SkullKingCard card = currentPlayer.getHand().stream()
                .filter(c -> c.getId().equals(cardId))
                .findFirst().orElse(null);
        if (card == null)
            return false;

        if (!isValidMove(state, currentPlayer, card))
            return false;

        // Play card
        currentPlayer.getHand().remove(card);
        currentPlayer.setCardPlayed(card);

        // Advance to next player or finish trick
        // Check if all players have played
        long playersPlayed = state.getPlayers().stream().filter(p -> p.getCardPlayed() != null).count();
        if (playersPlayed == state.getPlayers().size()) {
            resolveTrick(state);
        } else {
            state.setCurrentPlayerIndex((state.getCurrentPlayerIndex() + 1) % state.getPlayers().size());
        }
        return true;
    }

    private boolean isValidMove(SkullKingState state, SkullKingPlayer player, SkullKingCard card) {
        // 1. Get lead color (if any)
        // Find who started looking at trickStarterIndex?
        // But we need to know the effective lead suit.
        // Effective lead suit determined by the FIRST non-Escape card played.

        List<SkullKingPlayer> orderedPlayers = new ArrayList<>();
        int count = state.getPlayers().size();
        for (int i = 0; i < count; i++) {
            SkullKingPlayer p = state.getPlayers().get((state.getTrickStarterIndex() + i) % count);
            if (p.getCardPlayed() != null) {
                orderedPlayers.add(p);
            }
        }
        // Ordered players now has the sequence of plays so far.

        SkullKingColor leadColor = null;
        for (SkullKingPlayer p : orderedPlayers) {
            SkullKingCard c = p.getCardPlayed();
            // Escape doesn't set color.
            // Colors and Black Flag set color?
            // Actually Black Flag IS a color/suit.
            if (c.getType() == SkullKingCardType.NUMBER) {
                leadColor = c.getColor();
                break;
            }
            // Specials usually don't have color to follow?
            // If Pirate played lead, no suit logic? usually yes.
            // If lead is special, usually no suit to follow.
        }

        // If no lead color (or lead was Special), any card is valid.
        if (leadColor == null)
            return true;

        // If card played is Special, it's always valid (Escapes, Pirates, SK, Mermaids)
        if (card.getType() != SkullKingCardType.NUMBER)
            return true;

        // If card is NUMBER (colored or black), must follow suit IF player has it.
        // DOES player have suit?
        SkullKingColor finalLeadColor = leadColor;
        boolean hasSuit = player.getHand().stream()
                .anyMatch(c -> c.getType() == SkullKingCardType.NUMBER && c.getColor() == finalLeadColor);

        if (hasSuit) {
            return card.getColor() == leadColor;
        }

        return true;
    }

    private void resolveTrick(SkullKingState state) {
        // Determine winner
        List<SkullKingPlayer> trickSequence = new ArrayList<>();
        int count = state.getPlayers().size();
        for (int i = 0; i < count; i++) {
            trickSequence.add(state.getPlayers().get((state.getTrickStarterIndex() + i) % count));
        }

        SkullKingPlayer winner = determineTrickWinner(trickSequence);
        winner.setTricksWon(winner.getTricksWon() + 1);
        state.setTrickWinner(winner.getUsername());

        // Update starter for next trick (so we know who it is later)
        int winnerIndex = state.getPlayers().indexOf(winner);
        state.setTrickStarterIndex(winnerIndex);

        // Pause phase
        state.setPhase("TRICK_OVER");
        state.getReadyPlayers().clear();
    }

    private SkullKingPlayer determineTrickWinner(List<SkullKingPlayer> tricks) {
        // Logic:
        // Highest card wins.
        // Hierarchy: SK > Mermaid > Pirate > Black Flag > Color (if matching lead) >
        // Escape (Lose)

        // Find best card
        SkullKingPlayer currentWinner = tricks.get(0);
        SkullKingCard bestCard = currentWinner.getCardPlayed();
        SkullKingColor leadColor = (bestCard.getType() == SkullKingCardType.NUMBER) ? bestCard.getColor() : null;

        // If lead is Escape, lead color is determined by next card...
        // Simplification: if first is Escape, next valid card sets leadColor.
        if (bestCard.getType() == SkullKingCardType.ESCAPE) {
            // Find first non-escape
            for (int i = 1; i < tricks.size(); i++) {
                SkullKingCard c = tricks.get(i).getCardPlayed();
                if (c.getType() != SkullKingCardType.ESCAPE) {
                    // This sets the standard?
                    if (c.getType() == SkullKingCardType.NUMBER) {
                        leadColor = c.getColor();
                    }
                    break;
                }
            }
        }

        for (int i = 1; i < tricks.size(); i++) {
            SkullKingPlayer challenger = tricks.get(i);
            SkullKingCard challengerCard = challenger.getCardPlayed();

            if (isBetter(challengerCard, bestCard, leadColor)) {
                currentWinner = challenger;
                bestCard = challengerCard;
            }
        }
        return currentWinner;
    }

    private boolean isBetter(SkullKingCard challenger, SkullKingCard currentBest, SkullKingColor leadColor) {
        // Implement rules

        // 1. Skull King
        if (challenger.getType() == SkullKingCardType.SKULL_KING)
            return true;
        if (currentBest.getType() == SkullKingCardType.SKULL_KING)
            return false;

        // 2. Mermaid
        // User rule: Mermaid beats Pirate. SK beats Mermaid. (Handled above by SK check
        // first)
        // If both Mermaid? Higher ID? First played usually wins in tie. Assume first
        // played (currentBest) wins ties.
        if (challenger.getType() == SkullKingCardType.MERMAID && currentBest.getType() == SkullKingCardType.PIRATE)
            return true;
        if (challenger.getType() == SkullKingCardType.MERMAID && currentBest.getType() != SkullKingCardType.MERMAID
                && currentBest.getType() != SkullKingCardType.SKULL_KING)
            return true; // Mermaid beats normal stuff? Yes.

        // 3. Pirate
        if (challenger.getType() == SkullKingCardType.PIRATE && currentBest.getType() != SkullKingCardType.SKULL_KING
                && currentBest.getType() != SkullKingCardType.MERMAID
                && currentBest.getType() != SkullKingCardType.PIRATE)
            return true;

        // 4. Black Flag (Trump)
        if (challenger.getType() == SkullKingCardType.NUMBER && challenger.getColor() == SkullKingColor.BLACK) {
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() == SkullKingColor.BLACK) {
                return challenger.getValue() > currentBest.getValue();
            }
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() != SkullKingColor.BLACK)
                return true;
            // Beats normals.
            // Does it beat Escape? Yes.
            if (currentBest.getType() == SkullKingCardType.ESCAPE)
                return true;
        }

        // 5. Suit Color
        if (challenger.getType() == SkullKingCardType.NUMBER && challenger.getColor() == leadColor) {
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() == leadColor) {
                return challenger.getValue() > currentBest.getValue();
            }
            // Challanger followed suit, Current Best didn't (and isn't trump/special)?
            // IF currentBest is Escape, Suit beats it.
            if (currentBest.getType() == SkullKingCardType.ESCAPE)
                return true;
            // If currentBest is diff color (off-suit), Suit wins.
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() != leadColor
                    && currentBest.getColor() != SkullKingColor.BLACK)
                return true;
        }

        // Default: Current Best stays best
        return false;
    }

    private void calculateRoundScores(SkullKingState state) {
        for (SkullKingPlayer p : state.getPlayers()) {
            int bid = p.getBid();
            int won = p.getTricksWon();
            int points = 0;

            if (bid == won) {
                if (bid == 0) {
                    points = state.getRoundNumber() * 10;
                } else {
                    points = bid * 20;
                }
            } else {
                int diff = Math.abs(bid - won);
                points = diff * -10;
            }
            p.setRoundPoints(points);
            p.setScore(p.getScore() + points);
        }
    }

    private boolean startNextRound(SkullKingState state) {
        if (state.getRoundNumber() >= 10) {
            determineGameWinner(state);
            return true;
        }
        state.setRoundNumber(state.getRoundNumber() + 1);
        state.setPhase("BIDDING");
        state.getReadyPlayers().clear();
        state.setDeck(generateDeck());
        state.setTrickWinner(null);

        // trickStarterIndex currently holds the winner of the last trick of the
        // previous round.
        // We keep it as is, so that player starts the first trick of the new round.

        for (SkullKingPlayer p : state.getPlayers()) {
            p.getHand().clear();
            p.setBid(null);
            p.setTricksWon(0);
            p.setCardPlayed(null);
            p.setRoundPoints(0);
        }
        dealCards(state);
        return true;
    }

    private void determineGameWinner(SkullKingState state) {
        state.setPhase("GAME_OVER");
        SkullKingPlayer winner = state.getPlayers().stream()
                .max(Comparator.comparingInt(SkullKingPlayer::getScore))
                .orElse(null);
        if (winner != null) {
            state.setWinner(winner.getUsername());
        }
    }

    private void dealCards(SkullKingState state) {
        Collections.shuffle(state.getDeck());
        int cardsToDeal = state.getRoundNumber();
        for (SkullKingPlayer p : state.getPlayers()) {
            for (int i = 0; i < cardsToDeal; i++) {
                if (!state.getDeck().isEmpty()) {
                    p.getHand().add(state.getDeck().remove(0));
                }
            }
        }
    }

    private List<SkullKingCard> generateDeck() {
        List<SkullKingCard> deck = new ArrayList<>();
        int idCounter = 0;

        // Colors
        SkullKingColor[] colors = { SkullKingColor.YELLOW, SkullKingColor.GREEN, SkullKingColor.PURPLE,
                SkullKingColor.RED };
        for (SkullKingColor c : colors) {
            for (int i = 1; i <= 14; i++) {
                deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.NUMBER, c, i));
            }
        }
        // Black Flag (Trump)
        for (int i = 1; i <= 14; i++) {
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.NUMBER, SkullKingColor.BLACK, i));
        }
        // Specials
        for (int i = 0; i < 5; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.PIRATE, SkullKingColor.NONE, 0));
        for (int i = 0; i < 2; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.MERMAID, SkullKingColor.NONE, 0));
        deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.SKULL_KING, SkullKingColor.NONE, 0));
        for (int i = 0; i < 5; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.ESCAPE, SkullKingColor.NONE, 0));

        return deck;
    }

    private void saveState(Long gameId, SkullKingState state) {
        try {
            String json = objectMapper.writeValueAsString(state);
            redisTemplate.opsForValue().set(GAME_PREFIX + gameId + ":state", json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private SkullKingState loadState(Long gameId) {
        String json = redisTemplate.opsForValue().get(GAME_PREFIX + gameId + ":state");
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, SkullKingState.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void broadcastGameState(Long gameId, SkullKingState state) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");
        // Hide deck in payload if needed
        payload.put("gameState", state);

        updateAction.setPayload(payload);
        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/game", updateAction);
    }
}
