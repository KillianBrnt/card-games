package com.cardgames.controller;

import com.cardgames.dto.CreateGameRequest;
import com.cardgames.dto.GameResponse;
import com.cardgames.dto.JoinGameRequest;
import com.cardgames.model.GameStatus;
import com.cardgames.model.User;
import com.cardgames.service.GameService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(GameController.class)
@AutoConfigureMockMvc(addFilters = false)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GameService gameService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        // Mock Security Context
        User user = new User();
        user.setId(1L);
        user.setUsername("testPlayer");

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(user);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    public void testCreateGame() throws Exception {
        CreateGameRequest request = new CreateGameRequest();
        request.setGameType("flip-seven");

        GameResponse response = new GameResponse(1L, "CODE12", GameStatus.WAITING, "flip-seven", 1L);

        when(gameService.createGame(anyString(), any(User.class))).thenReturn(response);

        mockMvc.perform(post("/game/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1))
                .andExpect(jsonPath("$.gameCode").value("CODE12"));
    }

    @Test
    public void testJoinGame() throws Exception {
        JoinGameRequest request = new JoinGameRequest();
        request.setGameCode("CODE12");

        GameResponse response = new GameResponse(1L, "CODE12", GameStatus.WAITING, "flip-seven", 1L);

        when(gameService.joinGame(anyString(), any(User.class))).thenReturn(response);

        mockMvc.perform(post("/game/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));
    }

    @Test
    public void testGetGameInfo() throws Exception {
        GameResponse response = new GameResponse(1L, "CODE12", GameStatus.WAITING, "flip-seven", 1L);

        when(gameService.getGameInfo(anyLong(), any(User.class))).thenReturn(response);

        mockMvc.perform(get("/game/info")
                .param("gameId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value(1));
    }

    @Test
    public void testStartGame() throws Exception {
        doNothing().when(gameService).startGame(anyLong(), any(User.class));

        mockMvc.perform(post("/game/1/start"))
                .andExpect(status().isOk());
    }
}
