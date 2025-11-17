// File: org/boda/smartqueue/queue_server/SecurityConfig.java
package org.boda.smartqueue.queue_server;

import org.boda.smartqueue.queue_server.JWT.ApiKeyAuthFilter;
import org.boda.smartqueue.queue_server.JWT.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order; // Add this import
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
    @Order(1) // Higher priority
    public SecurityFilterChain apiUpdateFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher( // Match only the *updating* paths that need the API key
                        "/api/users/update-queue-state", // POST for queue state
                        "/api/users/ticket-status",     // PATCH/POST for ticket status
                        "/api/users/queues/active"      // POST/DELETE for active queue items
                        // Add other paths that require API key here if needed
                )
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new ApiKeyAuthFilter(LOCAL_SERVER_API_KEY), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Requires valid API key
                );
        return http.build();
    }

    // SECOND FILTER CHAIN: Default chain for other requests (public GETs, JWT protected endpoints)
    @Bean
    @Order(2) // Lower priority
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
                                // It's handled by the higher priority chain for updates
                        ).permitAll()
                        // Explicitly allow GET requests to /api/users/queues/active (list all) and /api/users/queues/active/{serviceType} (list by service)
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/queues/active", "/api/users/queues/active/{serviceType}").permitAll()
                        .anyRequest().authenticated() // Everything else requires JWT
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

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