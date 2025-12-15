package com.cardgames.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class LobbyService {

    private static final Logger logger = LoggerFactory.getLogger(LobbyService.class);
    private static final String LOBBY_PLAYERS_PREFIX = "lobby:players:";

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * Adds a player to the connected players set of a game lobby.
     *
     * @param gameId   The ID of the game.
     * @param username The username of the player to add.
     */
    public void addPlayer(Long gameId, String username) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.info("Adding player to Redis: key={}, user={}", key, username);
        redisTemplate.opsForSet().add(key, username);
    }

    /**
     * Removes a player from the connected players set of a game lobby.
     *
     * @param gameId   The ID of the game.
     * @param username The username of the player to remove.
     */
    public void removePlayer(Long gameId, String username) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.info("Removing player from Redis: key={}, user={}", key, username);
        redisTemplate.opsForSet().remove(key, username);
    }

    /**
     * Retrieves the set of connected players in a game lobby.
     *
     * @param gameId The ID of the game.
     * @return A set of usernames of connected players.
     */
    public Set<String> getPlayers(Long gameId) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.debug("Fetching players from Redis: key={}", key);
        return redisTemplate.opsForSet().members(key);
    }
}
