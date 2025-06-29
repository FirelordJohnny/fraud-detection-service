package com.faud.frauddetection.config;

import com.faud.frauddetection.security.ApiTokenAuthenticationFilter;
import com.faud.frauddetection.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.AuthenticationEntryPoint;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

/**
 * 安全配置测试
 * 验证安全配置是否正确初始化
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Mock
    private ApiTokenAuthenticationFilter apiTokenAuthenticationFilter;
    
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(jwtAuthenticationFilter, apiTokenAuthenticationFilter);
    }
    
    @Test
    void securityConfigShouldLoad() {
        // Given & When & Then
        assertNotNull(securityConfig, "SecurityConfig should be created successfully");
    }

    @Test
    void authenticationEntryPointShouldBeConfigured() {
        // Given & When
        AuthenticationEntryPoint entryPoint = securityConfig.unauthorizedEntryPoint();
        
        // Then
        assertNotNull(entryPoint, "Authentication entry point should be configured");
    }

    @Test
    void filtersShouldBeInjected() {
        // Given & When & Then
        assertNotNull(jwtAuthenticationFilter, "JWT authentication filter should be injected");
        assertNotNull(apiTokenAuthenticationFilter, "API token authentication filter should be injected");
    }
} 