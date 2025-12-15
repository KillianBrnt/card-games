package com.cardgames.websocket.controller;

import com.cardgames.engine.GameEngineHandler;
import com.cardgames.service.LobbyService;
import com.cardgames.websocket.model.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class ActionController {

    private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

    @Autowired
    private LobbyService lobbyService;

    @Autowired
    private SimpMessageSendingOperations messagingTemplate;

    @Autowired
    private GameEngineHandler gameEngineHandler;

    /**
     * Handles incoming messages from clients, routing them to the appropriate
     * handler (Game Engine or Chat).
     *
     * @param gameId The ID of the game session.
     * @param action The action object sent by the client.
     * @return The original action object.
     */
    @MessageMapping("/action/{gameId}/sendMessage")
    public Action sendMessage(@DestinationVariable String gameId, @Payload Action action) {
        logger.info("Received action from client for game {}: sender={}, payload={}, type={}", gameId,
                action.getSender(),
                action.getPayload(), action.getType());

        if (action.getGameId() == null) {
            action.setGameId(Long.parseLong(gameId));
        }

        if (Action.ActionType.GAME_ACTION.equals(action.getType())
                || Action.ActionType.SYNC_REQUEST.equals(action.getType())) {
            gameEngineHandler.handleAction(action);
        } else {
            messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/chat", action);
        }
        return action;
    }

    /**
     * Handles a user joining the game lobby via WebSocket.
     *
     * @param gameId         The ID of the game session.
     * @param action         The action object representing the join request.
     * @param headerAccessor Accessor for WebSocket session headers.
     * @return The original action object.
     */
    @MessageMapping("/action/{gameId}/addUser")
    public Action addUser(@DestinationVariable String gameId, @Payload Action action,
            SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Received join request for game {}: user={}", gameId, action.getSender());
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", action.getSender());
            headerAccessor.getSessionAttributes().put("gameId", Long.parseLong(gameId));
        }

        Long gId = Long.parseLong(gameId);
        lobbyService.addPlayer(gId, action.getSender());

        messagingTemplate.convertAndSend("/topic/lobby/" + gameId, lobbyService.getPlayers(gId));

        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/chat", action);

        return action;
    }
}
