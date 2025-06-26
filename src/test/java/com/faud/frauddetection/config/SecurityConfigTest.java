package com.faud.frauddetection.config;

import com.faud.frauddetection.security.ApiTokenAuthenticationFilter;
import com.faud.frauddetection.security.JwtAuthenticationFilter;
import com.faud.frauddetection.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {SecurityConfigTest.TestController.class}, includeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private FraudDetectionProperties fraudDetectionProperties;
    
    @RestController
    static class TestController {
        @GetMapping("/api/v1/rules")
        public String getRules() {
            return "rules";
        }

        @GetMapping("/health")
        public String health() {
            return "ok";
        }
        
        @GetMapping("/actuator/prometheus")
        public String prometheus() {
            return "metrics";
        }
    }
    
    @Test
    void whenRequestingPublicHealthEndpoint_shouldSucceed() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk());
    }

    @Test
    void whenRequestingInternalEndpoint_withCorrectRole_shouldSucceed() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk());
    }
    
    @Test
    void whenRequestingInternalEndpoint_withoutAuth_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenRequestingFraudRulesEndpoint_withAuth_shouldSucceed() throws Exception {
        mockMvc.perform(get("/api/v1/rules").with(user("user")))
                .andExpect(status().isOk());
    }

    @Test
    void whenRequestingFraudRulesEndpoint_withoutAuth_shouldBeUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/rules"))
                .andExpect(status().isUnauthorized());
    }
} 