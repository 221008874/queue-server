// File: org/boda/smartqueue/queue_server/SecurityConfig.java
package org.boda.smartqueue.queue_server;

import org.boda.smartqueue.queue_server.JWT.ApiKeyAuthFilter;
import org.boda.smartqueue.queue_server.JWT.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod; // Add this import
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private UserDetailsService userDetailsService;

    // Define the API key here (in a real app, use environment variables or config files)
    private static final String LOCAL_SERVER_API_KEY = "221008874"; // Replace with a real secret

    // FIRST FILTER CHAIN: For update endpoints that require API key authentication (POST, DELETE, etc.)
    @Bean
    @Order(1) // Highest priority for update paths requiring API key
    public SecurityFilterChain apiUpdateFilterChain(HttpSecurity http) throws Exception {
        http
                // Match ONLY the paths and methods that require the API key from the local server
                .securityMatcher(
                        "/api/users/update-queue-state", // POST
                        "/api/users/ticket-status",      // PUT/PATCH (for status updates)
                        "/api/users/queues/active"       // POST (add), DELETE (remove) - Requires API Key
                        // Add other update paths that require API key here if needed
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ApiKeyAuthFilter(LOCAL_SERVER_API_KEY), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Requires valid API key for matched paths and methods
                );
        return http.build();
    }
    // SECOND FILTER CHAIN: Default chain for other requests (public GETs, JWT protected endpoints)
    @Bean
    @Order(2) // Lower priority than the update chain
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/users/login",
                                "/api/users/forgot-password",
                                "/api/users/reset-password",
                                "/api/users/health",
                                "/api/users/queues/state" // Public GET for overall state
                                // DO NOT include "/api/users/queues/active" here anymore
                                // It's handled by the higher priority chain for updates OR below for specific GET
                        ).permitAll()
                        // **** CRITICAL FIX: Explicitly allow GET requests to /api/users/queues/active (list all) and /api/users/queues/active/{serviceType} (list by service)
                        // This rule must be in this chain (order 2) to take effect after the apiUpdateFilterChain (order 1) handles POST/DELETE
                        .requestMatchers(HttpMethod.GET, "/api/users/queues/active", "/api/users/queues/active/{serviceType}").permitAll()
                        .anyRequest().authenticated() // Everything else requires JWT
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // Make sure 'jwtAuthFilter' is the correct bean name

        return http.build();
    }


    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}