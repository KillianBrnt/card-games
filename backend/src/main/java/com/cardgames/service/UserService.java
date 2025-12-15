package com.cardgames.service;

import com.cardgames.mapper.UserMapper;
import com.cardgames.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Retrieves a list of all users in the system.
     *
     * @return A list of User objects.
     */
    public List<User> getAllUsers() {
        return userMapper.findAll();
    }

    /**
     * Retrieves a specific user by their unique ID.
     *
     * @param id The ID of the user to retrieve.
     * @return The User object if found, or null otherwise.
     */
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * Creates a new user with an encoded password.
     *
     * @param user The user object containing registration details.
     * @return The created User object.
     */
    public User createUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userMapper.insert(user);
        return user;
    }

    /**
     * Registers a new user with the provided email, password, and username.
     *
     * @param email    The email address of the user.
     * @param password The raw password of the user.
     * @param username The display name of the user.
     * @return The newly created User object.
     */
    public User registerUser(String email, String password, String username) {
        if (userMapper.findByEmail(email) != null) {
            throw new RuntimeException("Email already exists");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.insert(user);
        return user;
    }

    /**
     * Verifies a user's credentials against the stored password.
     *
     * @param email    The email address of the user.
     * @param password The raw password provided for verification.
     * @return The User object if credentials match, or null/exception otherwise.
     */
    public User verifyUser(String email, String password) {
        User user = userMapper.findByEmail(email);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            return user;
        }
        return null;
    }
}
