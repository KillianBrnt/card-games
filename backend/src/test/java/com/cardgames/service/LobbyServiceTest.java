package com.cardgames.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

public class LobbyServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @InjectMocks
    private LobbyService lobbyService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Test
    public void testAddPlayer() {
        Long gameId = 1L;
        String username = "testUser";
        String key = "lobby:players:" + gameId;

        lobbyService.addPlayer(gameId, username);

        verify(setOperations, times(1)).add(key, username);
    }

    @Test
    public void testRemovePlayer() {
        Long gameId = 1L;
        String username = "testUser";
        String key = "lobby:players:" + gameId;

        lobbyService.removePlayer(gameId, username);

        verify(setOperations, times(1)).remove(key, username);
    }

    @Test
    public void testGetPlayers() {
        Long gameId = 1L;
        String key = "lobby:players:" + gameId;
        Set<String> expectedPlayers = Set.of("user1", "user2");

        when(setOperations.members(key)).thenReturn(expectedPlayers);

        Set<String> actualPlayers = lobbyService.getPlayers(gameId);

        assertEquals(expectedPlayers, actualPlayers);
        verify(setOperations, times(1)).members(key);
    }
}
