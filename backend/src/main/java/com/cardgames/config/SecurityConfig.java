package com.cardgames.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures the security filter chain.
     * This method defines which URL paths used depend on authentication and which are public.
     * 
     * @param http the HttpSecurity to modify
     * @return the SecurityFilterChain
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery) for now as we are using a stateless REST API aproach (mostly)
            // and for easier testing. In a full production app with cookies, this might be needed.
            .csrf(AbstractHttpConfigurer::disable)
            
            // Define authorization rules
            .authorizeHttpRequests(auth -> auth
                // Allow anyone (unauthenticated) to access /auth/login and /auth/register
                .requestMatchers("/auth/**").permitAll()
                // Require authentication for any other request
                .anyRequest().authenticated()
            )
            
            // We are disabling default httpBasic and formLogin to use our own custom endpoints
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable);
        
        return http.build();
    }

    /**
     * Provides a BCrypt password encoder.
     * BCrypt is a strong hashing function designed for passwords.
     * It includes a salt automatically to protect against rainbow table attacks.
     * 
     * @return a PasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
