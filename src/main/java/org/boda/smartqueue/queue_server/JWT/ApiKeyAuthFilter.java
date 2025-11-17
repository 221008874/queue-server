// File: org/boda/smartqueue/queue_server/security/ApiKeyAuthFilter.java (Create this package/file)
package org.boda.smartqueue.queue_server.JWT; // Adjust package if needed

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final String apiKey;

    public ApiKeyAuthFilter(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestApiKey = request.getHeader("X-API-Key"); // Define the header name

        if (apiKey != null && apiKey.equals(requestApiKey)) {
            // Create an authenticated principal object
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    "LOCAL_SERVER", null, java.util.Collections.emptyList()); // Use a generic principal name
            SecurityContextHolder.getContext().setAuthentication(auth);
            // Continue with the filter chain
            filterChain.doFilter(request, response);
        } else {
            // API key is missing or invalid
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("Forbidden: Invalid API Key");
            // Optionally, you could throw an exception here if you have global exception handling
            // throw new BadCredentialsException("Invalid API Key");
        }
    }

    // Skip this filter if the request path doesn't match
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        // This filter is applied based on the path matcher in SecurityConfig, so this might not be strictly necessary
        // but it's good practice to define it if needed elsewhere.
        // For now, let SecurityConfig handle the path matching.
        return false; // Apply the filter to matching requests
    }
}