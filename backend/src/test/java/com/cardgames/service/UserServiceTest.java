package com.cardgames.service;

import com.cardgames.mapper.UserMapper;
import com.cardgames.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void getAllUsers() {
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        when(userMapper.findAll()).thenReturn(Arrays.asList(user1, user2));

        List<User> result = userService.getAllUsers();

        assertEquals(2, result.size());
        verify(userMapper).findAll();
    }

    @Test
    void getUserById() {
        User user = new User();
        user.setId(1L);
        when(userMapper.findById(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void createUser() {
        User user = new User();
        user.setPassword("plainPassword");
        when(passwordEncoder.encode("plainPassword")).thenReturn("hashedPassword");

        userService.createUser(user);

        assertEquals("hashedPassword", user.getPassword());
        verify(userMapper).insert(user);
    }

    @Test
    void registerUser_Success() {
        String email = "test@test.com";
        String password = "password";
        String username = "user";

        when(userMapper.findByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn("hashedPassword");

        User result = userService.registerUser(email, password, username);

        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals("hashedPassword", result.getPassword());
        assertEquals(username, result.getUsername());
        verify(userMapper).insert(any(User.class));
    }

    @Test
    void registerUser_EmailExists() {
        String email = "test@test.com";
        when(userMapper.findByEmail(email)).thenReturn(new User());

        assertThrows(RuntimeException.class, () -> 
            userService.registerUser(email, "pass", "user")
        );
        verify(userMapper, never()).insert(any(User.class));
    }

    @Test
    void verifyUser_Success() {
        String email = "test@test.com";
        String password = "password";
        User user = new User();
        user.setEmail(email);
        user.setPassword("hashedPassword");

        when(userMapper.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(true);

        User result = userService.verifyUser(email, password);

        assertNotNull(result);
    }

    @Test
    void verifyUser_Fail() {
        String email = "test@test.com";
        String password = "password";
        User user = new User();
        user.setPassword("hashedPassword");

        when(userMapper.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, "hashedPassword")).thenReturn(false);

        User result = userService.verifyUser(email, password);

        assertNull(result);
    }
}
