package com.cardgames.engine;

import com.cardgames.model.flipseven.*;
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

public class FlipSevenGameEngineTest {

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
    private FlipSevenGameEngine gameEngine;

    private FlipSevenState testState;
    private Long gameId = 1L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testState = new FlipSevenState();
        testState.setPlayers(new ArrayList<>());
        FlipSevenPlayer p1 = new FlipSevenPlayer("player1");
        p1.setRoundActive(true);
        testState.getPlayers().add(p1);

        FlipSevenPlayer p2 = new FlipSevenPlayer("player2");
        p2.setRoundActive(true);
        testState.getPlayers().add(p2);

        testState.setCurrentPlayerIndex(0);

        // Mock simple Deck
        List<Card> deck = new ArrayList<>();
        deck.add(new Card("1", CardType.NUMBER, 10, "10"));
        deck.add(new Card("2", CardType.NUMBER, 5, "5"));
        testState.setDeck(deck);
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
    public void testHandleAction_Hit_Normal() throws JsonProcessingException {
        // Setup P1 Hand
        testState.getPlayers().get(0).getHand().add(new Card("100", CardType.NUMBER, 2, "2"));

        // Mock loading
        mockStateLoading();

        // Action
        Action action = createAction("player1", "HIT");
        gameEngine.handleAction(action);

        // P1 should have 2 cards now (1 initial + 1 drawn)
        // Wait, logic: handleHit removes from deck. Deck has 10, 5.
        // P1 gets 10. Hand: [2, 10]. Score 12.

        verify(valueOperations, atLeastOnce()).set(anyString(), anyString());
        // Verify broadcast
        verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(), any(Action.class));
    }

    @Test
    public void testHandleAction_Hit_Bust() throws JsonProcessingException {
        // P1 has "10". Deck has "10". Hit -> Bust.
        testState.getPlayers().get(0).getHand().add(new Card("100", CardType.NUMBER, 10, "10"));

        // Deck with duplicate 10
        testState.getDeck().clear();
        testState.getDeck().add(new Card("101", CardType.NUMBER, 10, "10"));

        mockStateLoading();

        Action action = createAction("player1", "HIT");
        gameEngine.handleAction(action);

        // Verify Player 1 is busted (inactive, score 0)
        // But we need to capture the state saved to verify logic, or rely on Mockito
        // behavior?
        // Logic check: handleHit calls advanceTurn if bust.
        // Since we mock Serialization, we can't easily check the object passed to 'set'
        // unless we look at the 'testState' object which is modified by reference!
        // Yes, 'testState' is modified in place.

        assertFalse(testState.getPlayers().get(0).isRoundActive());
        assertEquals(0, testState.getPlayers().get(0).getRoundScore());

        // Turn should advance to Player 2
        assertEquals(1, testState.getCurrentPlayerIndex());
    }

    @Test
    public void testHandleAction_Stay() throws JsonProcessingException {
        // P1 has cards, decides to STAY.
        testState.getPlayers().get(0).getHand().add(new Card("100", CardType.NUMBER, 5, "5"));
        testState.getPlayers().get(0).setRoundScore(5);

        mockStateLoading();

        Action action = createAction("player1", "STAY");
        gameEngine.handleAction(action);

        // P1 inactive, Total Score updated
        assertFalse(testState.getPlayers().get(0).isRoundActive());
        assertEquals(5, testState.getPlayers().get(0).getTotalScore());

        // Turn advanced
        assertEquals(1, testState.getCurrentPlayerIndex());
    }

    @Test
    public void testHandleAction_Freeze() throws JsonProcessingException {
        // Deck gives Freeze card
        testState.getDeck().clear();
        testState.getDeck().add(new Card("99", CardType.ACTION_FREEZE, 0, "Freeze"));

        mockStateLoading();

        Action action = createAction("player1", "HIT");
        gameEngine.handleAction(action);

        // Should be pending target selection
        assertEquals("FREEZE_SELECTION", testState.getPendingActionType());
        assertEquals("player1", testState.getPendingActionInitiator());

        // Turn NOT advanced yet
        assertEquals(0, testState.getCurrentPlayerIndex());
    }

    @Test
    public void testHandleAction_SelectTarget_Freeze() throws JsonProcessingException {
        // State is waiting for freeze target
        testState.setPendingActionType("FREEZE_SELECTION");
        testState.setPendingActionInitiator("player1");
        // P1 needs the freeze card in hand to be consumable
        testState.getPlayers().get(0).getHand().add(new Card("99", CardType.ACTION_FREEZE, 0, "Freeze"));

        mockStateLoading();

        Action action = createAction("player1", "SELECT_TARGET");
        action.getPayload().put("target", "player2");

        gameEngine.handleAction(action);

        // Player 2 should be frozen (round active = false, score kept)
        // P2 had 0 score initially, let's say they had 10
        // But logic: target.setTotalScore(total + round). setRoundActive(false).

        assertFalse(testState.getPlayers().get(1).isRoundActive());
        assertNull(testState.getPendingActionType());

        // So it comes back to P1?
        assertEquals(0, testState.getCurrentPlayerIndex());
    }

    @Test
    public void testFlipSeven_Bonus() throws JsonProcessingException {
        // P1 has 6 numbers. Draws 7th unique number -> +15 bonus and Auto-Stay.
        List<Card> h = testState.getPlayers().get(0).getHand();
        for (int i = 0; i < 6; i++)
            h.add(new Card("" + i, CardType.NUMBER, i, "" + i));

        // Deck has 7th unique
        testState.getDeck().clear();
        testState.getDeck().add(new Card("7", CardType.NUMBER, 7, "7"));

        mockStateLoading();

        Action action = createAction("player1", "HIT");
        gameEngine.handleAction(action);

        // Score calc: 0+1+2+3+4+5 + 7 = 22. Bonus +15 = 37.
        // P1 Inactive (Auto Stay)
        assertFalse(testState.getPlayers().get(0).isRoundActive());
        assertEquals(37, testState.getPlayers().get(0).getTotalScore());
    }

    @Test
    public void testRoundRotation() throws JsonProcessingException {
        // Setup state: Round Over, waiting for ready
        testState.setRoundOver(true);
        testState.setRoundStarterIndex(0); // P1 started this round
        testState.getReadyPlayers().add("player1");

        mockStateLoading();

        // P2 becomes ready
        Action action = createAction("player2", "PLAYER_READY");
        gameEngine.handleAction(action);

        // Should trigger new round
        assertFalse(testState.isRoundOver());
        assertEquals(1, testState.getRoundStarterIndex());
        assertEquals(1, testState.getCurrentPlayerIndex());
    }

    private void mockStateLoading() throws JsonProcessingException {
        when(valueOperations.get(anyString())).thenReturn("json_state");
        when(objectMapper.readValue("json_state", FlipSevenState.class)).thenReturn(testState);
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
