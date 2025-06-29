package com.faud.frauddetection.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiTokenAuthenticationFilterTest {

    @InjectMocks
    private ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockFilterChain filterChain;

    private static final String TEST_INTERNAL_TOKEN = "test-internal-api-token";

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        filterChain = new MockFilterChain();
        SecurityContextHolder.clearContext();
        // Inject the test token into the filter instance
        ReflectionTestUtils.setField(apiTokenAuthenticationFilter, "internalApiToken", TEST_INTERNAL_TOKEN);
    }

    @Test
    void doFilterInternal_shouldSkipFilterForNonInternalPath() throws ServletException, IOException {
        request.setRequestURI("/api/some_endpoint");

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Verify that the filter chain continued and no security context was set
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        // Check if doFilter was called on the chain
        assertNotNull(filterChain.getRequest());
    }

    @Test
    void doFilterInternal_shouldAuthenticateWithValidBearerToken() throws ServletException, IOException {
        request.setRequestURI("/internal/health");
        request.addHeader("Authorization", "Bearer " + TEST_INTERNAL_TOKEN);

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("internal-api", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilterInternal_shouldAuthenticateWithValidXApiToken() throws ServletException, IOException {
        request.setRequestURI("/internal/status");
        request.addHeader("X-API-Token", TEST_INTERNAL_TOKEN);

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("internal-api", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilterInternal_shouldAuthenticateWithValidQueryParamToken() throws ServletException, IOException {
        request.setRequestURI("/internal/metrics");
        request.addParameter("apiToken", TEST_INTERNAL_TOKEN);

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("internal-api", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Test
    void doFilterInternal_shouldPrioritizeBearerToken() throws ServletException, IOException {
        request.setRequestURI("/internal/data");
        request.addHeader("Authorization", "Bearer " + TEST_INTERNAL_TOKEN);
        request.addHeader("X-API-Token", "wrong-token");
        request.addParameter("apiToken", "another-wrong-token");

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals("internal-api", SecurityContextHolder.getContext().getAuthentication().getName());
        assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }

    @Test
    void doFilterInternal_shouldFailWithInvalidToken() throws ServletException, IOException {
        request.setRequestURI("/internal/action");
        request.addHeader("Authorization", "Bearer invalid-token");

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing API token"));
    }

    @Test
    void doFilterInternal_shouldFailWithMissingToken() throws ServletException, IOException {
        request.setRequestURI("/internal/action");

        apiTokenAuthenticationFilter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getContentAsString().contains("Invalid or missing API token"));
    }

    @Test
    void doFilterInternal_shouldHandleGenericException() throws ServletException, IOException {
        // Create a request that will cause the header parsing to throw an exception
        HttpServletRequest failingRequest = mock(HttpServletRequest.class);
        when(failingRequest.getRequestURI()).thenReturn("/internal/action");
        when(failingRequest.getHeader("Authorization")).thenThrow(new RuntimeException("Header parsing failed"));

        MockHttpServletResponse testResponse = new MockHttpServletResponse();

        apiTokenAuthenticationFilter.doFilterInternal(failingRequest, testResponse, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(HttpServletResponse.SC_UNAUTHORIZED, testResponse.getStatus());
        assertTrue(testResponse.getContentAsString().contains("Authentication failed"));
    }

} 