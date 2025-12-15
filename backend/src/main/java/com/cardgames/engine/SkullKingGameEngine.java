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

    /**
     * Initializes the game by setting up players, deck, and initial state.
     *
     * @param gameId The ID of the game to initialize.
     */
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

    /**
     * Processes incoming actions from players, such as bids and card plays.
     *
     * @param action The action received from the client.
     */
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
            stateChanged = startNextRound(state);
        }

        if (stateChanged) {
            saveState(gameId, state);
            broadcastGameState(gameId, state);
        }
    }

    /**
     * Updates the state with a player's bid and advances the phase if all players have bid.
     *
     * @param state  The current game state.
     * @param sender The username of the player placing the bid.
     * @param bid    The bid amount.
     * @return true if the state changed, false otherwise.
     */
    private boolean handleBid(SkullKingState state, String sender, int bid) {
        if (!"BIDDING".equals(state.getPhase()))
            return false;

        SkullKingPlayer player = state.getPlayers().stream()
                .filter(p -> p.getUsername().equals(sender))
                .findFirst().orElse(null);

        if (player == null)
            return false;

        if (player.getBid() != null)
            return false;

        player.setBid(bid);

        if (state.getPlayers().stream().allMatch(p -> p.getBid() != null)) {
            state.setPhase("PLAYING");
            state.setCurrentPlayerIndex(state.getTrickStarterIndex());
        }
        return true;
    }

    /**
     * Marks a player as ready and proceeds to the next phase if all players are ready.
     *
     * @param state  The current game state.
     * @param sender The username of the player.
     * @return true if the state changed, false otherwise.
     */
    private boolean handlePlayerReady(SkullKingState state, String sender) {
        if (!"ROUND_OVER".equals(state.getPhase()) && !"TRICK_OVER".equals(state.getPhase()))
            return false;

        if (!state.getReadyPlayers().contains(sender)) {
            state.getReadyPlayers().add(sender);
        }

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

    /**
     * Prepares the state for the next trick or ends the round if hands are empty.
     *
     * @param state The current game state.
     */
    private void startNextTrick(SkullKingState state) {
        for (SkullKingPlayer p : state.getPlayers()) {
            p.setCardPlayed(null);
        }
        state.setTrickWinner(null);
        state.getReadyPlayers().clear();

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

    /**
     * Validates and executes a card play by a player.
     *
     * @param state  The current game state.
     * @param sender The username of the player playing the card.
     * @param cardId The ID of the card being played.
     * @return true if the state changed, false otherwise.
     */
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

        currentPlayer.getHand().remove(card);
        currentPlayer.setCardPlayed(card);

        long playersPlayed = state.getPlayers().stream().filter(p -> p.getCardPlayed() != null).count();
        if (playersPlayed == state.getPlayers().size()) {
            resolveTrick(state);
        } else {
            state.setCurrentPlayerIndex((state.getCurrentPlayerIndex() + 1) % state.getPlayers().size());
        }
        return true;
    }

    /**
     * Validates if the played card follows the game rules regarding suit following and special cards.
     *
     * @param state  The current game state.
     * @param player The player making the move.
     * @param card   The card being played.
     * @return true if the move is valid, false otherwise.
     */
    private boolean isValidMove(SkullKingState state, SkullKingPlayer player, SkullKingCard card) {
        List<SkullKingPlayer> orderedPlayers = new ArrayList<>();
        int count = state.getPlayers().size();
        for (int i = 0; i < count; i++) {
            SkullKingPlayer p = state.getPlayers().get((state.getTrickStarterIndex() + i) % count);
            if (p.getCardPlayed() != null) {
                orderedPlayers.add(p);
            }
        }

        SkullKingColor leadColor = null;
        for (SkullKingPlayer p : orderedPlayers) {
            SkullKingCard c = p.getCardPlayed();
            if (c.getType() == SkullKingCardType.NUMBER) {
                leadColor = c.getColor();
                break;
            }
        }

        if (leadColor == null)
            return true;

        if (card.getType() != SkullKingCardType.NUMBER)
            return true;

        SkullKingColor finalLeadColor = leadColor;
        boolean hasSuit = player.getHand().stream()
                .anyMatch(c -> c.getType() == SkullKingCardType.NUMBER && c.getColor() == finalLeadColor);

        if (hasSuit) {
            return card.getColor() == leadColor;
        }

        return true;
    }

    /**
     * Determines the winner of the trick and updates scores.
     *
     * @param state The current game state.
     */
    private void resolveTrick(SkullKingState state) {
        List<SkullKingPlayer> trickSequence = new ArrayList<>();
        int count = state.getPlayers().size();
        for (int i = 0; i < count; i++) {
            trickSequence.add(state.getPlayers().get((state.getTrickStarterIndex() + i) % count));
        }

        SkullKingPlayer winner = determineTrickWinner(trickSequence);
        winner.setTricksWon(winner.getTricksWon() + 1);
        state.setTrickWinner(winner.getUsername());

        int winnerIndex = state.getPlayers().indexOf(winner);
        state.setTrickStarterIndex(winnerIndex);

        state.setPhase("TRICK_OVER");
        state.getReadyPlayers().clear();
    }

    /**
     * Logic to determine which card wins the trick based on Skull King rules.
     *
     * @param tricks The list of players in the order they played for this trick.
     * @return The player who won the trick.
     */
    private SkullKingPlayer determineTrickWinner(List<SkullKingPlayer> tricks) {
        SkullKingPlayer currentWinner = tricks.get(0);
        SkullKingCard bestCard = currentWinner.getCardPlayed();
        SkullKingColor leadColor = (bestCard.getType() == SkullKingCardType.NUMBER) ? bestCard.getColor() : null;

        if (bestCard.getType() == SkullKingCardType.ESCAPE) {
            for (int i = 1; i < tricks.size(); i++) {
                SkullKingCard c = tricks.get(i).getCardPlayed();
                if (c.getType() != SkullKingCardType.ESCAPE) {
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

    /**
     * Compares two cards to see if the challenger beats the current best card.
     *
     * @param challenger  The card being compared.
     * @param currentBest The current winning card.
     * @param leadColor   The lead color of the trick.
     * @return true if the challenger wins, false otherwise.
     */
    private boolean isBetter(SkullKingCard challenger, SkullKingCard currentBest, SkullKingColor leadColor) {
        if (challenger.getType() == SkullKingCardType.SKULL_KING)
            return true;
        if (currentBest.getType() == SkullKingCardType.SKULL_KING)
            return false;

        if (challenger.getType() == SkullKingCardType.MERMAID && currentBest.getType() == SkullKingCardType.PIRATE)
            return true;
        if (challenger.getType() == SkullKingCardType.MERMAID && currentBest.getType() != SkullKingCardType.MERMAID
                && currentBest.getType() != SkullKingCardType.SKULL_KING)
            return true;

        if (challenger.getType() == SkullKingCardType.PIRATE && currentBest.getType() != SkullKingCardType.SKULL_KING
                && currentBest.getType() != SkullKingCardType.MERMAID
                && currentBest.getType() != SkullKingCardType.PIRATE)
            return true;

        if (challenger.getType() == SkullKingCardType.NUMBER && challenger.getColor() == SkullKingColor.BLACK) {
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() == SkullKingColor.BLACK) {
                return challenger.getValue() > currentBest.getValue();
            }
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() != SkullKingColor.BLACK)
                return true;
            if (currentBest.getType() == SkullKingCardType.ESCAPE)
                return true;
        }

        if (challenger.getType() == SkullKingCardType.NUMBER && challenger.getColor() == leadColor) {
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() == leadColor) {
                return challenger.getValue() > currentBest.getValue();
            }
            if (currentBest.getType() == SkullKingCardType.ESCAPE)
                return true;
            if (currentBest.getType() == SkullKingCardType.NUMBER && currentBest.getColor() != leadColor
                    && currentBest.getColor() != SkullKingColor.BLACK)
                return true;
        }

        return false;
    }

    /**
     * Calculates scores for all players at the end of a round based on bids and tricks won.
     *
     * @param state The current game state.
     */
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

    /**
     * Resets the state for a new round or ends the game if max rounds reached.
     *
     * @param state The current game state.
     * @return true if the state changed, false otherwise.
     */
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

    /**
     * Identifies the winner of the game based on total scores.
     *
     * @param state The current game state.
     */
    private void determineGameWinner(SkullKingState state) {
        state.setPhase("GAME_OVER");
        SkullKingPlayer winner = state.getPlayers().stream()
                .max(Comparator.comparingInt(SkullKingPlayer::getScore))
                .orElse(null);
        if (winner != null) {
            state.setWinner(winner.getUsername());
        }
    }

    /**
     * Distributes cards to players for the current round.
     *
     * @param state The current game state.
     */
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

    /**
     * Creates a standard Skull King deck with all card types.
     *
     * @return A list of SkullKingCards representing a new deck.
     */
    private List<SkullKingCard> generateDeck() {
        List<SkullKingCard> deck = new ArrayList<>();
        int idCounter = 0;

        SkullKingColor[] colors = { SkullKingColor.YELLOW, SkullKingColor.GREEN, SkullKingColor.PURPLE,
                SkullKingColor.RED };
        for (SkullKingColor c : colors) {
            for (int i = 1; i <= 14; i++) {
                deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.NUMBER, c, i));
            }
        }
        for (int i = 1; i <= 14; i++) {
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.NUMBER, SkullKingColor.BLACK, i));
        }
        for (int i = 0; i < 5; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.PIRATE, SkullKingColor.NONE, 0));
        for (int i = 0; i < 2; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.MERMAID, SkullKingColor.NONE, 0));
        deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.SKULL_KING, SkullKingColor.NONE, 0));
        for (int i = 0; i < 5; i++)
            deck.add(new SkullKingCard(String.valueOf(idCounter++), SkullKingCardType.ESCAPE, SkullKingColor.NONE, 0));

        return deck;
    }

    /**
     * Persists the game state to Redis.
     *
     * @param gameId The ID of the game.
     * @param state  The game state to save.
     */
    private void saveState(Long gameId, SkullKingState state) {
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
     * @return The current SkullKingState, or null if not found.
     */
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

    /**
     * Sends the current game state to all clients via WebSocket.
     *
     * @param gameId The ID of the game.
     * @param state  The game state to broadcast.
     */
    private void broadcastGameState(Long gameId, SkullKingState state) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");
        payload.put("gameState", state);

        updateAction.setPayload(payload);
        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/game", updateAction);
    }
}
