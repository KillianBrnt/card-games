package com.cardgames.controller;

import com.cardgames.model.User;
import com.cardgames.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable Security filters so we test logic
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_Success() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@test.com");
        payload.put("password", "pass");
        payload.put("username", "user");

        when(userService.registerUser(anyString(), anyString(), anyString())).thenReturn(new User());

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void register_Failure() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@test.com");
        payload.put("password", "pass");
        payload.put("username", "user");

        when(userService.registerUser(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_Success() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@test.com");
        payload.put("password", "pass");

        User user = new User();
        user.setEmail("test@test.com");

        when(userService.verifyUser(anyString(), anyString())).thenReturn(user);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk());
    }

    @Test
    void login_Failure() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("email", "test@test.com");
        payload.put("password", "wrong");

        when(userService.verifyUser(anyString(), anyString())).thenReturn(null);

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().is4xxClientError());
    }
}
