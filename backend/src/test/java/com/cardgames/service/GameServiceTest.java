package com.cardgames.service;

import com.cardgames.dto.GameResponse;
import com.cardgames.engine.GameEngineHandler;
import com.cardgames.mapper.GameMapper;
import com.cardgames.model.Game;
import com.cardgames.model.GameStatus;
import com.cardgames.model.User;
import com.cardgames.model.exception.AccessDeniedException;
import com.cardgames.websocket.model.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class GameServiceTest {

    @Mock
    private GameMapper gameMapper;

    @Mock
    private SimpMessageSendingOperations messagingTemplate;

    @Mock
    private GameEngineHandler gameEngineHandler;

    @InjectMocks
    private GameService gameService;

    private User testUser;
    private Game testGame;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testGame = new Game();
        testGame.setId(1L);
        testGame.setCode("ABCDEF");
        testGame.setType("flip-seven");
        testGame.setHostUserId(1L);
        testGame.setStatus(GameStatus.WAITING);
        testGame.setCreatedAt(LocalDateTime.now());
    }

    @Test
    public void testCreateGame() {
        doNothing().when(gameMapper).insertGame(any(Game.class));
        doNothing().when(gameMapper).insertGamePlayer(any());

        GameResponse response = gameService.createGame("flip-seven", testUser);

        assertNotNull(response);
        assertEquals("flip-seven", response.getGameType());
        assertEquals(1L, response.getHostUserId());
        assertNotNull(response.getGameCode());
        assertEquals(GameStatus.WAITING, response.getStatus());

        verify(gameMapper, times(1)).insertGame(any(Game.class));
        verify(gameMapper, times(1)).insertGamePlayer(any());
    }

    @Test
    public void testJoinGame_Success() {
        when(gameMapper.findByCode(anyString())).thenReturn(Optional.of(testGame));
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(false);
        doNothing().when(gameMapper).insertGamePlayer(any());

        GameResponse response = gameService.joinGame("ABCDEF", testUser);

        assertNotNull(response);
        assertEquals(testGame.getId(), response.getGameId());
        verify(gameMapper, times(1)).insertGamePlayer(any());
    }

    @Test
    public void testJoinGame_AlreadyJoined() {
        when(gameMapper.findByCode(anyString())).thenReturn(Optional.of(testGame));
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(true);

        GameResponse response = gameService.joinGame("ABCDEF", testUser);

        assertNotNull(response);
        verify(gameMapper, never()).insertGamePlayer(any());
    }

    @Test
    public void testJoinGame_GameNotFound() {
        when(gameMapper.findByCode(anyString())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> gameService.joinGame("INVALID", testUser));
    }

    @Test
    public void testJoinGame_GameNotWaiting() {
        testGame.setStatus(GameStatus.PLAYING);
        when(gameMapper.findByCode(anyString())).thenReturn(Optional.of(testGame));
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> gameService.joinGame("ABCDEF", testUser));
    }

    @Test
    public void testGetGameInfo_Success() {
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(true);
        when(gameMapper.findById(anyLong())).thenReturn(Optional.of(testGame));

        GameResponse response = gameService.getGameInfo(1L, testUser);

        assertNotNull(response);
        assertEquals(testGame.getId(), response.getGameId());
    }

    @Test
    public void testGetGameInfo_NotPlayer() {
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> gameService.getGameInfo(1L, testUser));
    }

    @Test
    public void testGetGameInfo_GameNotFound() {
        when(gameMapper.isPlayerInGame(anyLong(), anyLong())).thenReturn(true);
        when(gameMapper.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> gameService.getGameInfo(1L, testUser));
    }

    @Test
    public void testStartGame_Success() {
        when(gameMapper.findById(anyLong())).thenReturn(Optional.of(testGame));
        doNothing().when(gameMapper).updateGameStatus(anyLong(), anyString());
        doNothing().when(gameEngineHandler).initializeGame(anyString(), anyLong());

        gameService.startGame(1L, testUser);

        verify(gameMapper, times(1)).updateGameStatus(1L, GameStatus.PLAYING.name());
        verify(gameEngineHandler, times(1)).initializeGame(testGame.getType(), testGame.getId());
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Action.class));
    }

    @Test
    public void testStartGame_NotHost() {
        User otherUser = new User();
        otherUser.setId(2L);
        when(gameMapper.findById(anyLong())).thenReturn(Optional.of(testGame));

        assertThrows(AccessDeniedException.class, () -> gameService.startGame(1L, otherUser));
    }

    @Test
    public void testStartGame_NotWaiting() {
        testGame.setStatus(GameStatus.PLAYING);
        when(gameMapper.findById(anyLong())).thenReturn(Optional.of(testGame));

        assertThrows(RuntimeException.class, () -> gameService.startGame(1L, testUser));
    }
}
