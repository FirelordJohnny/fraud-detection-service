package com.faud.frauddetection.config;

import com.faud.frauddetection.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 安全配置测试
 * 验证安全配置是否正确加载
 */
@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "api.token.internal=test-token",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration"
})
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FraudDetectionProperties fraudDetectionProperties;
    
    @Test
    void securityConfigShouldLoad() {
        // Given & When & Then
        assertNotNull(securityConfig, "SecurityConfig should be loaded");
    }

    @Test
    void securityFilterChainShouldBeConfigured() throws Exception {
        // Given & When & Then
        assertNotNull(securityConfig.filterChain(null), "Security filter chain should be configured");
    }

    @Test
    void authenticationEntryPointShouldBeConfigured() {
        // Given & When & Then
        assertNotNull(securityConfig.unauthorizedEntryPoint(), "Authentication entry point should be configured");
    }
} 