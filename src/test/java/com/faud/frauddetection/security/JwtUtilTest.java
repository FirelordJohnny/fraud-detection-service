package com.faud.frauddetection.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private final String testUsername = "testuser";
    private final String testUserId = "user123";
    private final List<String> testRoles = Arrays.asList("USER", "ADMIN");

    @BeforeEach
    void setUp() {
        // Set test secret and expiration via reflection
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJWTTokenGeneration12345678901234567890");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400); // 24 hours in seconds
    }

    @Test
    void testGenerateToken_ShouldReturnValidToken() {
        // When
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // Then
        assertThat(token).isNotNull();
        assertThat(token).isNotBlank();
        assertThat(token.split("\\.")).hasSize(3); // JWT has 3 parts separated by dots
    }

    @Test
    void testGetUsernameFromToken_ShouldReturnCorrectUsername() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        String extractedUsername = jwtUtil.getUsernameFromToken(token);

        // Then
        assertThat(extractedUsername).isEqualTo(testUsername);
    }

    @Test
    void testGetUserIdFromToken_ShouldReturnCorrectUserId() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        String extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertThat(extractedUserId).isEqualTo(testUserId);
    }

    @Test
    void testGetRolesFromToken_ShouldReturnCorrectRoles() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        List<String> extractedRoles = jwtUtil.getRolesFromToken(token);

        // Then
        assertThat(extractedRoles).isEqualTo(testRoles);
        assertThat(extractedRoles).containsExactly("USER", "ADMIN");
    }

    @Test
    void testValidateToken_WithValidToken_ShouldReturnTrue() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        Boolean isValid = jwtUtil.validateToken(token, testUsername);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    void testValidateToken_WithWrongUsername_ShouldReturnFalse() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wronguser");

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    void testValidateToken_WithExpiredToken_ShouldReturnFalse() {
        // Given - set very short expiration
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1); // Expired
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When & Then - expired token should cause exception in validateToken
        assertThatThrownBy(() -> jwtUtil.validateToken(token, testUsername))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JWT token has expired");
    }

    @Test
    void testGetExpirationDateFromToken_ShouldReturnValidDate() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When
        Date expiration = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertThat(expiration).isNotNull();
        assertThat(expiration.getTime()).isGreaterThan(System.currentTimeMillis());
    }

    @Test
    void testInvalidToken_ShouldThrowException() {
        // Given
        String invalidToken = "invalid.jwt.token";

        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(invalidToken))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JWT token is malformed");
    }

    @Test
    void testExpiredToken_ShouldThrowException() {
        // Given - create expired token
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", -1000); // Very expired
        String expiredToken = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // Reset expiration to normal
        ReflectionTestUtils.setField(jwtUtil, "jwtExpiration", 86400);

        // When & Then
        assertThatThrownBy(() -> jwtUtil.getUsernameFromToken(expiredToken))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JWT token has expired");
    }

    @Test
    void testGenerateToken_WithEmptyRoles_ShouldWork() {
        // Given
        List<String> emptyRoles = Arrays.asList();

        // When
        String token = jwtUtil.generateToken(testUsername, emptyRoles, testUserId);

        // Then
        assertThat(token).isNotNull();
        assertThat(jwtUtil.getUsernameFromToken(token)).isEqualTo(testUsername);
        assertThat(jwtUtil.getRolesFromToken(token)).isEmpty();
    }

    @Test
    void testValidateToken_WithInvalidToken_ShouldReturnFalse() {
        // Given
        String invalidToken = "clearly.invalid.token";

        // When & Then - invalid token should cause exception in validateToken
        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken, testUsername))
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("JWT token is malformed");
    }

    @Test
    void testTokenRoundTrip_ShouldMaintainAllInformation() {
        // Given
        String token = jwtUtil.generateToken(testUsername, testRoles, testUserId);

        // When - extract all information
        String extractedUsername = jwtUtil.getUsernameFromToken(token);
        String extractedUserId = jwtUtil.getUserIdFromToken(token);
        List<String> extractedRoles = jwtUtil.getRolesFromToken(token);
        Date expiration = jwtUtil.getExpirationDateFromToken(token);

        // Then - verify all information is preserved
        assertThat(extractedUsername).isEqualTo(testUsername);
        assertThat(extractedUserId).isEqualTo(testUserId);
        assertThat(extractedRoles).isEqualTo(testRoles);
        assertThat(expiration).isAfter(new Date());
        assertThat(jwtUtil.validateToken(token, testUsername)).isTrue();
    }
} 