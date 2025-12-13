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
            // Chat or other messages
            messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/chat", action);
        }
        return action;
    }

    @MessageMapping("/action/{gameId}/addUser")
    public Action addUser(@DestinationVariable String gameId, @Payload Action action,
            SimpMessageHeaderAccessor headerAccessor) {
        logger.info("Received join request for game {}: user={}", gameId, action.getSender());
        // Add username and gameId in web socket session
        if (headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", action.getSender());
            headerAccessor.getSessionAttributes().put("gameId", Long.parseLong(gameId));
        }

        Long gId = Long.parseLong(gameId);
        lobbyService.addPlayer(gId, action.getSender());

        // Broadcast user list to specific lobby
        messagingTemplate.convertAndSend("/topic/lobby/" + gameId, lobbyService.getPlayers(gId));

        // Broadcast join message
        messagingTemplate.convertAndSend("/topic/lobby/" + gameId + "/chat", action);

        return action;
    }
}
