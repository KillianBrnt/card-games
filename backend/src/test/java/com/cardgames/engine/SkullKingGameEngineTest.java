package com.cardgames.engine;

import com.cardgames.model.skullking.*;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class SkullKingGameEngineTest {

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private LobbyService lobbyService;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SkullKingGameEngine gameEngine;

    private SkullKingState testState;
    private Long gameId = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testState = new SkullKingState();
        testState.setPlayers(new ArrayList<>());
        testState.getPlayers().add(new SkullKingPlayer("player1"));
        testState.getPlayers().add(new SkullKingPlayer("player2"));
        // Assuming round 1, simple state
        testState.setRoundNumber(1);
        testState.setPhase("BIDDING");
    }

    @Test
    public void testInitializeGame() throws JsonProcessingException {
        when(lobbyService.getPlayers(gameId)).thenReturn(Set.of("player1", "player2"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        gameEngine.initializeGame(gameId);

        verify(lobbyService, times(1)).getPlayers(gameId);
        verify(redisTemplate.opsForValue(), times((1))).set(anyString(), anyString()); // Saved state
        // Broadcasts: Initial state
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(Action.class));
    }

    @Test
    public void testProcessAction_Bid() throws JsonProcessingException {
        // Setup: Waiting for bids
        testState.setPhase("BIDDING");

        mockStateLoading();

        // Player 1 Bids 0
        Action action1 = createAction("player1", "BID");
        action1.getPayload().put("bid", 0);

        gameEngine.handleAction(action1);

        // Verify P1 bid set
        assertEquals(0, testState.getPlayers().get(0).getBid());
        // Phase still BIDDING because P2 hasn't bid
        assertEquals("BIDDING", testState.getPhase());

        // Player 2 Bids 1
        Action action2 = createAction("player2", "BID");
        action2.getPayload().put("bid", 1);

        gameEngine.handleAction(action2);

        // Verify P2 bid set
        assertEquals(1, testState.getPlayers().get(1).getBid());
        // Phase change to PLAYING
        assertEquals("PLAYING", testState.getPhase());
    }

    @Test
    public void testProcessAction_PlayCard_SimpleTrick() throws JsonProcessingException {
        // Setup: Playing phase, P1 turn
        testState.setPhase("PLAYING");
        testState.setCurrentPlayerIndex(0);

        // Give cards
        SkullKingCard card1 = new SkullKingCard("1", SkullKingCardType.NUMBER, SkullKingColor.RED, 5);
        SkullKingCard card2 = new SkullKingCard("2", SkullKingCardType.NUMBER, SkullKingColor.RED, 3);

        testState.getPlayers().get(0).getHand().add(card1);
        testState.getPlayers().get(1).getHand().add(card2);

        mockStateLoading();

        // P1 Plays Red 5
        Action action1 = createAction("player1", "PLAY_CARD");
        action1.getPayload().put("cardId", "1");

        gameEngine.handleAction(action1);

        // Verify card played
        assertEquals(card1, testState.getPlayers().get(0).getCardPlayed());
        // Turn passed to P2
        assertEquals(1, testState.getCurrentPlayerIndex());

        // P2 Plays Red 3
        Action action2 = createAction("player2", "PLAY_CARD");
        action2.getPayload().put("cardId", "2");

        gameEngine.handleAction(action2);

        // Verify Trick End logic (P1 wins - Red 5 > Red 3)
        // Checks:
        // 1. Trick winner set (implied by who starts next trick, if logic updates
        // trickStarterIndex)
        // 2. tricksWon incremented?

        // Note: The engine logic likely auto-resolves the trick when the last player
        // plays.
        // If P1 wins, P1 should start the next trick (trickStarterIndex = 0)
        // OR it's a new round if hands are empty?
        // Here hands are empty after play (1 card dealt). So round over?

        // Assuming round continues or ends based on cards.
        // If round ends (1 card per player), phase -> ROUND_OVER or update scores.
        // Let's check tricksWon if implemented right away.
        assertEquals(1, testState.getPlayers().get(0).getTricksWon());
        assertEquals(0, testState.getPlayers().get(1).getTricksWon());
    }

    private void mockStateLoading() throws JsonProcessingException {
        when(valueOperations.get(anyString())).thenReturn("json_state");
        when(objectMapper.readValue("json_state", SkullKingState.class)).thenReturn(testState);
        when(objectMapper.writeValueAsString(any())).thenReturn("new_json_state");
    }

    private Action createAction(String sender, String type) {
        Action action = new Action();
        action.setGameId(gameId);
        action.setSender(sender);
        Map<String, Object> p = new HashMap<>();
        p.put("action", type);
        action.setPayload(p);
        return action;
    }
}
