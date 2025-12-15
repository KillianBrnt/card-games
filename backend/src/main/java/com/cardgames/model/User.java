package com.cardgames.model;

/**
 * Represents a registered user in the system.
 */
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;

    /**
     * Gets the unique identifier of the user.
     *
     * @return The user ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param id The user ID to set.
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Gets the username of the user.
     *
     * @return The username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username The username to set.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the email address of the user.
     *
     * @return The email address.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email The email address to set.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the encrypted password of the user.
     *
     * @return The encrypted password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the encrypted password of the user.
     *
     * @param password The encrypted password to set.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
