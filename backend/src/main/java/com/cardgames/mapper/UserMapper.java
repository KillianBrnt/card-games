package com.cardgames.mapper;

import com.cardgames.model.User;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * Mapper interface for User-related database operations.
 */
@Mapper
public interface UserMapper {

    /**
     * Retrieves all users from the database.
     *
     * @return A list of all users.
     */
    List<User> findAll();

    /**
     * Finds a user by their ID.
     *
     * @param id The user ID.
     * @return The user if found, or null regarding MyBatis behavior.
     */
    User findById(Long id);

    /**
     * Finds a user by their email address.
     *
     * @param email The email address.
     * @return The user if found.
     */
    User findByEmail(String email);

    /**
     * Inserts a new user into the database.
     *
     * @param user The user to insert.
     */
    void insert(User user);
}
