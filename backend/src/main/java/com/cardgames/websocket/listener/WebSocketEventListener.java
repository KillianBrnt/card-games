package com.cardgames.websocket.listener;

import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Objects;
import java.util.Set;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private LobbyService lobbyService;

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        logger.info("Received a new web socket connection");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        String username = (String) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("username");
        Long gameId = (Long) headerAccessor.getSessionAttributes().get("gameId");

        if (username != null && gameId != null) {
            logger.info("User Disconnected : " + username + " from game " + gameId);

            Action action = new Action();
            action.setType(Action.ActionType.LEAVE);
            action.setSender(username);
            action.setGameId(gameId);

            lobbyService.removePlayer(gameId, username);

            messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/chat", action);

            // Broadcast updated user list
            Set<String> players = lobbyService.getPlayers(gameId);
            messagingTemplate.convertAndSend("/topic/lobby/" + gameId, players);
        }
    }
}
