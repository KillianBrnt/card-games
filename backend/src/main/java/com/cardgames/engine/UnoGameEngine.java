package com.cardgames.engine;

import com.cardgames.model.uno.*;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class UnoGameEngine implements GameEngine {

    private final SimpMessageSendingOperations messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final LobbyService lobbyService;
    private final ObjectMapper objectMapper;

    private static final String GAME_PREFIX = "game:uno:";

    public UnoGameEngine(SimpMessageSendingOperations messagingTemplate, StringRedisTemplate redisTemplate,
            LobbyService lobbyService, ObjectMapper objectMapper) {
        this.messagingTemplate = messagingTemplate;
        this.redisTemplate = redisTemplate;
        this.lobbyService = lobbyService;
        this.objectMapper = objectMapper;
    }

    /**
     * Initializes the game by setting up players, dealing initial cards, and
     * setting the first card.
     *
     * @param gameId The ID of the game to initialize.
     */
    @Override
    public void initializeGame(Long gameId) {
        Set<String> playerNames = lobbyService.getPlayers(gameId);
        if (playerNames == null || playerNames.isEmpty()) {
            return;
        }

        UnoState state = new UnoState();
        List<UnoPlayer> players = new ArrayList<>();
        playerNames.forEach(p -> players.add(new UnoPlayer(p)));
        Collections.shuffle(players);
        state.setPlayers(players);
        state.setCurrentPlayerIndex(0);

        state.setDeck(generateDeck());

        for (UnoPlayer p : players) {
            for (int i = 0; i < 7; i++) {
                drawCard(state, p);
            }
        }

        UnoCard firstCard = null;
        while (firstCard == null) {
            if (state.getDeck().isEmpty())
                break;
            UnoCard c = state.getDeck().remove(0);

            if (c.getType() == UnoCardType.WILD_DRAW_FOUR) {
                state.getDeck().add(c);
                Collections.shuffle(state.getDeck());
                continue;
            }
            firstCard = c;
        }

        state.getDiscardPile().add(firstCard);
        state.setCurrentTopCard(firstCard);

        handleFirstCard(state, firstCard);

        saveState(gameId, state);
        broadcastGameState(gameId, state);
    }

    /**
     * Handles the effect of the very first card turned over at the start of the
     * game.
     *
     * @param state The current game state.
     * @param card  The first card of the game.
     */
    private void handleFirstCard(UnoState state, UnoCard card) {
        if (card.getColor() != UnoCardColor.NONE) {
            state.setCurrentColor(card.getColor());
        }

        switch (card.getType()) {
            case WILD:
                state.setWaitingForColorSelection(true);
                state.setPendingActionInitiator(state.getPlayers().get(state.getCurrentPlayerIndex()).getUsername());
                break;
            case DRAW_TWO:
                UnoPlayer p0 = state.getPlayers().get(state.getCurrentPlayerIndex());
                drawCards(state, p0, 2);
                advanceTurn(state);
                break;
            case REVERSE:
                if (state.getPlayers().size() == 2) {
                    advanceTurn(state);
                } else {
                    state.setDirection(-1);
                }
                break;
            case SKIP:
                advanceTurn(state);
                break;
            default:
                break;
        }
    }

    /**
     * Processes incoming actions from players, such as playing a card, drawing, or
     * saying Uno.
     *
     * @param action The action received from the client.
     */
    @Override
    public void handleAction(Action action) {
        Long gameId = action.getGameId();
        UnoState state = loadState(gameId);
        if (state == null)
            return;

        String sender = action.getSender();
        Map<String, Object> payload = action.getPayload();
        String type = (String) payload.get("action");

        UnoPlayer currentPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());

        if ("PLAY_CARD".equals(type)) {
            String cardId = (String) payload.get("cardId");

            if (!currentPlayer.getUsername().equals(sender)) {
                UnoPlayer senderPlayer = state.getPlayers().stream().filter(p -> p.getUsername().equals(sender))
                        .findFirst().orElse(null);
                if (senderPlayer != null) {
                    UnoCard card = senderPlayer.getHand().stream().filter(c -> c.getId().equals(cardId)).findFirst()
                            .orElse(null);
                    UnoCard top = state.getCurrentTopCard();

                    if (card != null && top != null && card.getColor() == top.getColor()
                            && card.getColor() != UnoCardColor.NONE) {
                        boolean match = false;
                        if (card.getType() == UnoCardType.NUMBER && top.getType() == UnoCardType.NUMBER
                                && card.getValue() != null && card.getValue().equals(top.getValue())) {
                            match = true;
                        } else if (card.getType() == top.getType() && card.getType() != UnoCardType.NUMBER) {
                            match = true;
                        }

                        if (match) {
                            int senderIndex = state.getPlayers().indexOf(senderPlayer);
                            state.setCurrentPlayerIndex(senderIndex);
                            currentPlayer = senderPlayer;
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                } else {
                    return;
                }
            }

            if (state.isWaitingForColorSelection())
                return;

            if (payload.containsKey("saidUno") && Boolean.TRUE.equals(payload.get("saidUno"))) {
                currentPlayer.setSaidUno(true);
            }

            handlePlayCard(state, currentPlayer, cardId);

        } else if ("DRAW_CARD".equals(type)) {
            if (!currentPlayer.getUsername().equals(sender))
                return;
            if (state.isWaitingForColorSelection())
                return;

            handleDrawCard(state, currentPlayer);

        } else if ("SELECT_COLOR".equals(type)) {
            if (!currentPlayer.getUsername().equals(sender))
                return;
            if (!state.isWaitingForColorSelection())
                return;

            String colorStr = (String) payload.get("color");
            handleSelectColor(state, currentPlayer, colorStr);

        } else if ("SAY_UNO".equals(type)) {
            handleSayUno(state, sender);
        } else if ("SYNC_REQUEST".equals(type)) {
            broadcastGameState(gameId, state);
            return;
        }

        saveState(gameId, state);
        broadcastGameState(gameId, state);
    }

    /**
     * Validates and executes the logic for playing a card.
     *
     * @param state  The current game state.
     * @param player The player making the move.
     * @param cardId The ID of the card being played.
     */
    private void handlePlayCard(UnoState state, UnoPlayer player, String cardId) {
        UnoCard card = player.getHand().stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
        if (card == null)
            return;

        boolean isColorMatch = card.getColor() == state.getCurrentColor();
        boolean isValueMatch = card.getValue() != null && state.getCurrentTopCard().getValue() != null &&
                card.getValue().equals(state.getCurrentTopCard().getValue());
        boolean isTypeMatch = card.getType() == state.getCurrentTopCard().getType()
                && card.getType() != UnoCardType.NUMBER;
        boolean isWild = card.getColor() == UnoCardColor.NONE;

        if (!isColorMatch && !isValueMatch && !isTypeMatch && !isWild) {
            return;
        }

        if (card.getType() == UnoCardType.WILD_DRAW_FOUR) {
            boolean hasColor = player.getHand().stream().anyMatch(c -> c.getColor() == state.getCurrentColor());
        }

        player.getHand().remove(card);
        state.getDiscardPile().add(card);
        state.setCurrentTopCard(card);

        if (player.getHand().size() > 1) {
            player.setSaidUno(false);
        } else if (player.getHand().size() == 1) {
        }

        if (card.getColor() != UnoCardColor.NONE) {
            state.setCurrentColor(card.getColor());
        }

        switch (card.getType()) {
            case NUMBER:
                checkWin(state, player);
                if (!state.isGameOver())
                    advanceTurn(state);
                break;
            case SKIP:
                checkWin(state, player);
                if (!state.isGameOver())
                    advanceTurn(state, 2);
                break;
            case REVERSE:
                if (state.getPlayers().size() == 2) {
                    checkWin(state, player);
                    if (!state.isGameOver())
                        advanceTurn(state, 2);
                } else {
                    state.setDirection(state.getDirection() * -1);
                    checkWin(state, player);
                    if (!state.isGameOver())
                        advanceTurn(state);
                }
                break;
            case DRAW_TWO:
                checkWin(state, player);
                if (!state.isGameOver()) {
                    UnoPlayer next = getNextPlayer(state, 1);
                    drawCards(state, next, 2);
                    advanceTurn(state, 2);
                }
                break;
            case WILD:
            case WILD_DRAW_FOUR:
                checkWin(state, player);
                if (!state.isGameOver()) {
                    state.setWaitingForColorSelection(true);
                    state.setPendingActionInitiator(player.getUsername());
                }
                break;
            default:
                advanceTurn(state);
        }
    }

    /**
     * Handles the selection of a color after a Wild card is played.
     *
     * @param state    The current game state.
     * @param player   The player selecting the color.
     * @param colorStr The selected color as a string.
     */
    private void handleSelectColor(UnoState state, UnoPlayer player, String colorStr) {
        try {
            UnoCardColor color = UnoCardColor.valueOf(colorStr);
            state.setCurrentColor(color);
            state.setWaitingForColorSelection(false);
            state.setPendingActionInitiator(null);

            UnoCard top = state.getCurrentTopCard();
            if (top.getType() == UnoCardType.WILD_DRAW_FOUR) {
                UnoPlayer next = getNextPlayer(state, 1);
                drawCards(state, next, 4);
                advanceTurn(state, 2);
            } else {
                advanceTurn(state);
            }

        } catch (IllegalArgumentException e) {
            // ignore invalid color
        }
    }

    /**
     * Handles the logic for a player drawing a card from the deck.
     *
     * @param state  The current game state.
     * @param player The player drawing the card.
     */
    private void handleDrawCard(UnoState state, UnoPlayer player) {
        UnoCard drawn = drawCard(state, player);
        if (drawn == null)
            return;

        boolean playable = false;
        if (drawn.getColor() == UnoCardColor.NONE || drawn.getColor() == state.getCurrentColor())
            playable = true;
        if (drawn.getValue() != null && state.getCurrentTopCard().getValue() != null &&
                drawn.getValue().equals(state.getCurrentTopCard().getValue()))
            playable = true;
        if (drawn.getType() == state.getCurrentTopCard().getType() && drawn.getType() != UnoCardType.NUMBER)
            playable = true;

        if (!playable) {
            advanceTurn(state);
        } else {
        }
    }

    /**
     * Handles a player declaring "Uno".
     *
     * @param state    The current game state.
     * @param username The username of the player saying Uno.
     */
    private void handleSayUno(UnoState state, String username) {
        UnoPlayer p = state.getPlayers().stream().filter(pl -> pl.getUsername().equals(username)).findFirst()
                .orElse(null);
        if (p != null) {
            p.setSaidUno(true);
        }
    }

    /**
     * Checks if a player failed to say Uno and applies a penalty if necessary.
     *
     * @param state          The current game state.
     * @param previousPlayer The player to check for penalty.
     */
    private void checkUnoPenalty(UnoState state, UnoPlayer previousPlayer) {
        if (previousPlayer.getHand().size() == 1 && !previousPlayer.hasSaidUno()) {
            drawCards(state, previousPlayer, 2);
        }
        previousPlayer.setSaidUno(false);
    }

    /**
     * Advances the turn to the next player by 1 step.
     *
     * @param state The current game state.
     */
    private void advanceTurn(UnoState state) {
        advanceTurn(state, 1);
    }

    /**
     * Advances the turn by a specified number of steps, handling any end-of-turn
     * penalties.
     *
     * @param state The current game state.
     * @param steps The number of steps to advance (e.g., 2 for Skip).
     */
    private void advanceTurn(UnoState state, int steps) {
        UnoPlayer finishingPlayer = state.getPlayers().get(state.getCurrentPlayerIndex());
        if (!state.isGameOver() && finishingPlayer.getHand().size() == 1 && !finishingPlayer.hasSaidUno()) {
            drawCards(state, finishingPlayer, 2);
        }
        if (finishingPlayer.getHand().size() != 1) {
            finishingPlayer.setSaidUno(false);
        }

        int current = state.getCurrentPlayerIndex();
        int direction = state.getDirection();
        int numPlayers = state.getPlayers().size();

        int next = (current + (direction * steps)) % numPlayers;
        if (next < 0)
            next += numPlayers;

        state.setCurrentPlayerIndex(next);
        state.setWaitingForColorSelection(false);
        state.setPendingActionInitiator(null);
    }

    /**
     * Determines the next player index based on current direction and steps.
     *
     * @param state The current game state.
     * @param steps The number of steps ahead to look.
     * @return The next UnoPlayer in the sequence.
     */
    private UnoPlayer getNextPlayer(UnoState state, int steps) {
        int current = state.getCurrentPlayerIndex();
        int direction = state.getDirection();
        int numPlayers = state.getPlayers().size();
        int next = (current + (direction * steps)) % numPlayers;
        if (next < 0)
            next += numPlayers;
        return state.getPlayers().get(next);
    }

    /**
     * Checks if the player has won the game.
     *
     * @param state  The current game state.
     * @param player The player to check.
     */
    private void checkWin(UnoState state, UnoPlayer player) {
        if (player.getHand().isEmpty()) {
            state.setWinner(player.getUsername());
            state.setGameOver(true);
        }
    }

    /**
     * Draws a single card from the deck for a player.
     *
     * @param state  The current game state.
     * @param player The player drawing the card.
     * @return The drawn UnoCard.
     */
    private UnoCard drawCard(UnoState state, UnoPlayer player) {
        if (state.getDeck().isEmpty()) {
            reshuffleDeck(state);
            if (state.getDeck().isEmpty())
                return null;
        }
        UnoCard c = state.getDeck().remove(0);
        player.getHand().add(c);
        return c;
    }

    /**
     * Draws multiple cards for a player.
     *
     * @param state  The current game state.
     * @param player The player drawing the cards.
     * @param count  The number of cards to draw.
     */
    private void drawCards(UnoState state, UnoPlayer player, int count) {
        for (int i = 0; i < count; i++) {
            drawCard(state, player);
        }
    }

    /**
     * Reshuffles the discard pile back into the deck if the deck is empty.
     *
     * @param state The current game state.
     */
    private void reshuffleDeck(UnoState state) {
        if (state.getDiscardPile().isEmpty())
            return;

        UnoCard top = state.getDiscardPile().remove(state.getDiscardPile().size() - 1);

        List<UnoCard> rest = new ArrayList<>(state.getDiscardPile());
        state.getDiscardPile().clear();
        state.getDiscardPile().add(top);

        for (UnoCard c : rest) {
            if (c.getType() == UnoCardType.WILD || c.getType() == UnoCardType.WILD_DRAW_FOUR) {
                c.setColor(UnoCardColor.NONE);
            }
        }

        Collections.shuffle(rest);
        state.setDeck(rest);
    }

    /**
     * Generates a new shuffled Uno deck.
     *
     * @return A list of UnoCards.
     */
    private List<UnoCard> generateDeck() {
        List<UnoCard> deck = new ArrayList<>();
        int idCount = 0;

        UnoCardColor[] colors = { UnoCardColor.RED, UnoCardColor.BLUE, UnoCardColor.GREEN, UnoCardColor.YELLOW };

        for (UnoCardColor color : colors) {
            deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.NUMBER, 0, "0"));

            for (int i = 1; i <= 9; i++) {
                deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.NUMBER, i, String.valueOf(i)));
                deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.NUMBER, i, String.valueOf(i)));
            }

            for (int i = 0; i < 2; i++) {
                deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.SKIP, null, "Skip"));
                deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.REVERSE, null, "Reverse"));
                deck.add(new UnoCard(String.valueOf(idCount++), color, UnoCardType.DRAW_TWO, null, "+2"));
            }
        }

        for (int i = 0; i < 4; i++) {
            deck.add(new UnoCard(String.valueOf(idCount++), UnoCardColor.NONE, UnoCardType.WILD, null, "Wild"));
            deck.add(new UnoCard(String.valueOf(idCount++), UnoCardColor.NONE, UnoCardType.WILD_DRAW_FOUR, null, "+4"));
        }

        Collections.shuffle(deck);
        return deck;
    }

    /**
     * Persists the game state to Redis.
     *
     * @param gameId The ID of the game.
     * @param state  The game state to save.
     */
    private void saveState(Long gameId, UnoState state) {
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
     * @return The current UnoState, or null if not found.
     */
    private UnoState loadState(Long gameId) {
        String json = redisTemplate.opsForValue().get(GAME_PREFIX + gameId + ":state");
        if (json == null)
            return null;
        try {
            return objectMapper.readValue(json, UnoState.class);
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
    private void broadcastGameState(Long gameId, UnoState state) {
        Action updateAction = new Action();
        updateAction.setType(Action.ActionType.GAME_ACTION);
        updateAction.setGameId(gameId);
        updateAction.setSender("SYSTEM");

        Map<String, Object> payload = new HashMap<>();
        payload.put("type", "GAME_UPDATE");

        UnoState publicState = new UnoState();
        publicState.setDiscardPile(state.getDiscardPile());
        publicState.setCurrentTopCard(state.getCurrentTopCard());
        publicState.setCurrentColor(state.getCurrentColor());
        publicState.setCurrentPlayerIndex(state.getCurrentPlayerIndex());
        publicState.setDirection(state.getDirection());
        publicState.setGameOver(state.isGameOver());
        publicState.setWinner(state.getWinner());
        publicState.setWaitingForColorSelection(state.isWaitingForColorSelection());
        publicState.setPendingActionInitiator(state.getPendingActionInitiator());

        List<UnoPlayer> sanitizedPlayers = new ArrayList<>();
        for (UnoPlayer p : state.getPlayers()) {
            UnoPlayer sp = new UnoPlayer();
            sp.setUsername(p.getUsername());
            sp.setSaidUno(p.hasSaidUno());
            sp.setRoundActive(p.isRoundActive());
            sp.setHand(p.getHand());
            sanitizedPlayers.add(sp);
        }
        publicState.setPlayers(sanitizedPlayers);

        payload.put("gameState", publicState);
        updateAction.setPayload(payload);

        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/game", updateAction);
    }
}
