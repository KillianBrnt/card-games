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

    public void addPlayer(Long gameId, String username) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.info("Adding player to Redis: key={}, user={}", key, username);
        redisTemplate.opsForSet().add(key, username);
    }

    public void removePlayer(Long gameId, String username) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.info("Removing player from Redis: key={}, user={}", key, username);
        redisTemplate.opsForSet().remove(key, username);
    }

    public Set<String> getPlayers(Long gameId) {
        String key = LOBBY_PLAYERS_PREFIX + gameId;
        logger.debug("Fetching players from Redis: key={}", key);
        return redisTemplate.opsForSet().members(key);
    }
}
