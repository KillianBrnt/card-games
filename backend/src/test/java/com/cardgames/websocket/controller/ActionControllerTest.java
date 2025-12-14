package com.cardgames.websocket.controller;

import com.cardgames.engine.GameEngineHandler;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class ActionControllerTest {

    @Mock
    private LobbyService lobbyService;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private GameEngineHandler gameEngineHandler;

    @InjectMocks
    private ActionController actionController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSendMessage_GameAction() {
        String gameId = "1";
        Action action = new Action();
        action.setType(Action.ActionType.GAME_ACTION);
        action.setSender("user1");

        actionController.sendMessage(gameId, action);

        verify(gameEngineHandler, times(1)).handleAction(action);
        assertEquals(1L, action.getGameId());
    }

    @Test
    public void testSendMessage_Chat() {
        String gameId = "1";
        Action action = new Action();
        action.setType(Action.ActionType.SYSTEM); // Not GAME_ACTION
        action.setSender("user1");

        actionController.sendMessage(gameId, action);

        verify(gameEngineHandler, never()).handleAction(action);
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/lobby/1/chat"), eq(action));
    }

    @Test
    public void testAddUser() {
        String gameId = "1";
        Action action = new Action();
        action.setSender("user1");

        SimpMessageHeaderAccessor headerAccessor = mock(SimpMessageHeaderAccessor.class);
        Map<String, Object> sessionAttributes = new HashMap<>();
        when(headerAccessor.getSessionAttributes()).thenReturn(sessionAttributes);
        when(lobbyService.getPlayers(1L)).thenReturn(Set.of("user1"));

        actionController.addUser(gameId, action, headerAccessor);

        // Verify session update
        assertEquals("user1", sessionAttributes.get("username"));
        assertEquals(1L, sessionAttributes.get("gameId"));

        // Verify Service call
        verify(lobbyService, times(1)).addPlayer(1L, "user1");

        // Verify Broadcasts
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/lobby/1"), any(Set.class)); // User list
        verify(messagingTemplate, times(1)).convertAndSend(eq("/topic/lobby/1/chat"), eq(action)); // Join msg
    }
}
