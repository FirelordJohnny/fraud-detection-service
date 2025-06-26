package com.faud.frauddetection.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.impl.DefaultClaims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
    }

    private String generateTestToken() {
        return "test-jwt-token";
    }

    @Test
    void doFilterInternal_shouldAuthenticateWithValidJwt() throws ServletException, IOException {
        String token = generateTestToken();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtil.getUserIdFromToken(token)).thenReturn("user123");
        List<String> roles = Arrays.asList("ROLE_USER", "ROLE_VIEWER");
        when(jwtUtil.getRolesFromToken(token)).thenReturn(roles);
        when(jwtUtil.validateToken(token, "testuser")).thenReturn(true);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertTrue(authentication.isAuthenticated());
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        assertEquals("testuser", principal.getUsername());
        assertEquals("user123", principal.getUserId());
        assertEquals(2, principal.getAuthorities().size());
        assertTrue(principal.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWithInvalidJwt() throws ServletException, IOException {
        String token = generateTestToken();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn("testuser");
        when(jwtUtil.validateToken(token, "testuser")).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void doFilterInternal_shouldSkipWhenNoJwtInHeader() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }
    
    @Test
    void doFilterInternal_shouldSkipWhenHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        String token = generateTestToken();
        request.addHeader("Authorization", token); // No "Bearer " prefix

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).getUsernameFromToken(anyString());
    }
    
    @Test
    void doFilterInternal_shouldHandleJwtParsingExceptionGracefully() throws ServletException, IOException {
        String token = generateTestToken();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.getUsernameFromToken(token)).thenThrow(new RuntimeException("JWT parsing failed"));

        // The filter should catch the exception and continue the chain without setting authentication
        assertDoesNotThrow(() -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
    
    @Test
    void doFilterInternal_shouldNotOverwriteExistingAuthentication() throws ServletException, IOException {
        // Pre-set an authentication object
        UserPrincipal existingPrincipal = new UserPrincipal("existingUser", "user000", Collections.singletonList(new SimpleGrantedAuthority("ROLE_PRE_AUTH")));
        UsernamePasswordAuthenticationToken existingAuth = new UsernamePasswordAuthenticationToken(existingPrincipal, null, existingPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);
        
        String token = generateTestToken();
        request.addHeader("Authorization", "Bearer " + token);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        var finalAuthentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(finalAuthentication);
        assertEquals("existingUser", ((UserPrincipal)finalAuthentication.getPrincipal()).getUsername());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }

    @Test
    void doFilterInternal_shouldNotAuthenticateWhenUsernameIsNull() throws ServletException, IOException {
        String token = generateTestToken();
        request.addHeader("Authorization", "Bearer " + token);

        when(jwtUtil.getUsernameFromToken(token)).thenReturn(null);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtUtil, never()).validateToken(anyString(), anyString());
    }
} 