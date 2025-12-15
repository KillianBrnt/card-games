package com.cardgames.controller;

import com.cardgames.model.User;
import com.cardgames.model.exception.InvalidCredentialsException;
import com.cardgames.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    /**
     * Registers a new user in the system.
     *
     * @param payload A map containing registration details: email, password, and
     *                username.
     * @return A ResponseEntity containing the registered User.
     */
    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody Map<String, String> payload) {
        User user = userService.registerUser(
                payload.get("email"),
                payload.get("password"),
                payload.get("username"));
        return ResponseEntity.ok(user);
    }

    /**
     * Authenticates a user and establishes a security session.
     *
     * @param payload A map containing login credentials: email and password.
     * @param request The HTTP request to access the session.
     * @return A ResponseEntity containing the authenticated User.
     */
    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody Map<String, String> payload, HttpServletRequest request) {
        User user = userService.verifyUser(
                payload.get("email"),
                payload.get("password"));

        if (user != null) {
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));

            SecurityContext sc = SecurityContextHolder.getContext();
            sc.setAuthentication(auth);

            HttpSession session = request.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, sc);

            return ResponseEntity.ok(user);
        } else {
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }

    /**
     * Logs out the current user and invalidates the session.
     *
     * @param request The HTTP request to access the session.
     * @return A ResponseEntity containing a logout success message.
     */
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        SecurityContextHolder.clearContext();
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok("Logged out successfully");
    }
}
