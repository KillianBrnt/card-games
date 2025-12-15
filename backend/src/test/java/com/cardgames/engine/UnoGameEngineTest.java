package com.cardgames.engine;

import com.cardgames.model.uno.*;
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

public class UnoGameEngineTest {

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
    private UnoGameEngine gameEngine;

    private UnoState testState;
    private Long gameId = 2L;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        testState = new UnoState();
        testState.setPlayers(new ArrayList<>());
        testState.getPlayers().add(new UnoPlayer("player1"));
        testState.getPlayers().add(new UnoPlayer("player2"));

        // Initial setup
        testState.setCurrentPlayerIndex(0);
        testState.setDirection(1);
        testState.setDeck(new ArrayList<>());
        testState.setDiscardPile(new ArrayList<>());
    }

    @Test
    public void testInitializeGame() throws JsonProcessingException {
        when(lobbyService.getPlayers(gameId)).thenReturn(Set.of("player1", "player2"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        gameEngine.initializeGame(gameId);

        verify(lobbyService, times(1)).getPlayers(gameId);
        // Should save state
        verify(redisTemplate.opsForValue(), times(1)).set(anyString(), anyString());
        // Should broadcast
        // verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(),
        // any(Action.class));
    }

    @Test
    public void testProcessAction_PlayCard_ColorMatch() throws JsonProcessingException {
        // Setup: Top card RED 5, Current Color RED
        UnoCard topCard = new UnoCard("1", UnoCardColor.RED, UnoCardType.NUMBER, 5, "5");
        testState.setCurrentTopCard(topCard);
        testState.setCurrentColor(UnoCardColor.RED);

        // P1 Hand: RED 7 and BLUE 9 (to prevent win)
        UnoCard p1Card = new UnoCard("2", UnoCardColor.RED, UnoCardType.NUMBER, 7, "7");
        UnoCard extraCard = new UnoCard("3", UnoCardColor.BLUE, UnoCardType.NUMBER, 9, "9");
        testState.getPlayers().get(0).getHand().add(p1Card);
        testState.getPlayers().get(0).getHand().add(extraCard);

        mockStateLoading();

        // P1 Plays RED 7
        Action action = createAction("player1", "PLAY_CARD");
        action.getPayload().put("cardId", "2");

        gameEngine.handleAction(action);

        // Verify:
        // 1. Top card is now RED 7
        assertEquals("2", testState.getCurrentTopCard().getId());
        // 2. P1 hand has 1 card left
        assertEquals(1, testState.getPlayers().get(0).getHand().size());
        // 3. Turn advanced to P2
        assertEquals(1, testState.getCurrentPlayerIndex());

        // verify(messagingTemplate, atLeastOnce()).convertAndSend(anyString(),
        // any(Action.class));
    }

    @Test
    public void testProcessAction_PlayCard_Invalid() throws JsonProcessingException {
        // Setup: Top card RED 5
        UnoCard topCard = new UnoCard("1", UnoCardColor.RED, UnoCardType.NUMBER, 5, "5");
        testState.setCurrentTopCard(topCard);
        testState.setCurrentColor(UnoCardColor.RED);

        // P1 Hand: BLUE 8 (No match)
        UnoCard p1Card = new UnoCard("2", UnoCardColor.BLUE, UnoCardType.NUMBER, 8, "8");
        testState.getPlayers().get(0).getHand().add(p1Card);

        mockStateLoading();

        // P1 Plays BLUE 8
        Action action = createAction("player1", "PLAY_CARD");
        action.getPayload().put("cardId", "2");

        gameEngine.handleAction(action);

        // Verify state did NOT change
        assertEquals("1", testState.getCurrentTopCard().getId());
        assertEquals(0, testState.getCurrentPlayerIndex());
    }

    /*
     * @Test
     * public void testProcessAction_DrawCard() throws JsonProcessingException {
     * // Setup: Deck has card
     * testState.setDeck(new ArrayList<>());
     * testState.getDeck().add(new UnoCard("3", UnoCardColor.GREEN,
     * UnoCardType.NUMBER, 1, "1"));
     * testState.setDiscardPile(new ArrayList<>()); // redundant but safe
     * 
     * mockStateLoading();
     * 
     * Action action = createAction("player1", "DRAW_CARD");
     * 
     * gameEngine.handleAction(action);
     * 
     * // Verify P1 has 1 card
     * assertEquals(1, testState.getPlayers().get(0).getHand().size());
     * assertEquals("3", testState.getPlayers().get(0).getHand().get(0).getId());
     * }
     */

    @Test
    public void testProcessAction_Wild_ColorSelection() throws JsonProcessingException {
        // Setup: P1 plays Wild
        // We simulate the post-play state where we are waiting for color
        testState.setWaitingForColorSelection(true);
        testState.setPendingActionInitiator("player1");
        testState.setCurrentTopCard(new UnoCard("99", UnoCardColor.NONE, UnoCardType.WILD, null, "Wild"));

        mockStateLoading();

        // P1 Selects BLUE
        Action action = createAction("player1", "SELECT_COLOR");
        action.getPayload().put("color", "BLUE");

        gameEngine.handleAction(action);

        // Verify color set
        assertEquals(UnoCardColor.BLUE, testState.getCurrentColor());
        // Waiting flag cleared
        assertFalse(testState.isWaitingForColorSelection());
        // Turn likely flows to next
        assertEquals(1, testState.getCurrentPlayerIndex());
    }

    private void mockStateLoading() throws JsonProcessingException {
        when(valueOperations.get(anyString())).thenReturn("json_state");
        when(objectMapper.readValue("json_state", UnoState.class)).thenReturn(testState);
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
