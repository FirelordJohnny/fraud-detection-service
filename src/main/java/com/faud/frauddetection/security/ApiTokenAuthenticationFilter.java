package com.faud.frauddetection.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * API Token Authentication Filter for internal controllers
 */
@Component
public class ApiTokenAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ApiTokenAuthenticationFilter.class);

    @Value("${api.token.internal:internal-api-token-2024}")
    private String internalApiToken;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        
        // Only apply to internal API paths
        if (!requestPath.startsWith("/internal/")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String apiToken = getApiTokenFromRequest(request);

            if (StringUtils.hasText(apiToken) && validateApiToken(apiToken)) {
                // Create authentication for internal API access
                List<SimpleGrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_INTERNAL_API"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
                );

                UserPrincipal userPrincipal = new UserPrincipal("internal-api", "system", authorities);
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userPrincipal, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Set internal API authentication");
                
            } else {
                logger.warn("Invalid or missing API token for internal API access from IP: {}", 
                    request.getRemoteAddr());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\":\"Invalid or missing API token\"}");
                return;
            }
        } catch (Exception ex) {
            logger.error("Could not set internal API authentication", ex);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\":\"Authentication failed\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getApiTokenFromRequest(HttpServletRequest request) {
        // Check Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        // Check X-API-Token header
        String apiToken = request.getHeader("X-API-Token");
        if (StringUtils.hasText(apiToken)) {
            return apiToken;
        }
        
        // Check query parameter
        return request.getParameter("apiToken");
    }

    private boolean validateApiToken(String token) {
        return internalApiToken.equals(token);
    }
} 