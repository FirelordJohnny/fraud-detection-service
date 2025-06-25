package com.faud.frauddetection.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.faud.frauddetection.dto.FraudRuleDto;
import com.faud.frauddetection.entity.FraudRule;
import com.faud.frauddetection.service.FraudRuleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test cases for Fraud Rule Controller
 */
@WebMvcTest(FraudRuleController.class)
class FraudRuleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudRuleService fraudRuleService;

    @Autowired
    private ObjectMapper objectMapper;

    private FraudRule testRule;
    private FraudRuleDto testRuleDto;

    @BeforeEach
    void setUp() {
        testRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testRuleDto = FraudRuleDto.builder()
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .build();
    }

    @Test
    void getAllRules_ShouldReturnAllRules() throws Exception {
        // Given
        List<FraudRule> rules = Arrays.asList(testRule);
        when(fraudRuleService.getAllFraudRules()).thenReturn(rules);

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-rules"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is(1)))
            .andExpect(jsonPath("$[0].ruleName", is("TEST_AMOUNT_RULE")))
            .andExpect(jsonPath("$[0].ruleType", is("AMOUNT")))
            .andExpect(jsonPath("$[0].enabled", is(true)));

        verify(fraudRuleService).getAllFraudRules();
    }

    @Test
    void getRuleById_ExistingRule_ShouldReturnRule() throws Exception {
        // Given
        when(fraudRuleService.getFraudRuleById(1L)).thenReturn(Optional.of(testRule));

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-rules/1"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.ruleName", is("TEST_AMOUNT_RULE")))
            .andExpect(jsonPath("$.ruleType", is("AMOUNT")));

        verify(fraudRuleService).getFraudRuleById(1L);
    }

    @Test
    void getRuleById_NonExistentRule_ShouldReturnNotFound() throws Exception {
        // Given
        when(fraudRuleService.getFraudRuleById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/fraud-rules/999"))
            .andExpect(status().isNotFound());

        verify(fraudRuleService).getFraudRuleById(999L);
    }

    @Test
    void createRule_ValidRule_ShouldCreateAndReturnRule() throws Exception {
        // Given
        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(testRule);

        // When & Then
        mockMvc.perform(post("/api/v1/fraud-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRuleDto)))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(1)))
            .andExpect(jsonPath("$.ruleName", is("TEST_AMOUNT_RULE")))
            .andExpect(jsonPath("$.ruleType", is("AMOUNT")));

        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void createRule_InvalidRule_ShouldReturnBadRequest() throws Exception {
        // Given - invalid rule with missing required fields
        FraudRuleDto invalidRule = FraudRuleDto.builder()
            .ruleType("AMOUNT")
            // Missing ruleName
            .enabled(true)
            .build();

        // When & Then
        mockMvc.perform(post("/api/v1/fraud-rules")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRule)))
            .andExpect(status().isBadRequest());

        verify(fraudRuleService, never()).createFraudRule(any());
    }

    @Test
    void updateRule_ValidRule_ShouldUpdateAndReturnRule() throws Exception {
        // Given
        FraudRule updatedRule = FraudRule.builder()
            .id(1L)
            .ruleName("UPDATED_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Updated description")
            .thresholdValue(BigDecimal.valueOf(15000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.4))
            .priority(1)
            .build();

        when(fraudRuleService.updateFraudRule(eq(1L), any(FraudRule.class))).thenReturn(updatedRule);

        FraudRuleDto updateDto = FraudRuleDto.builder()
            .ruleName("UPDATED_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Updated description")
            .thresholdValue(BigDecimal.valueOf(15000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.4))
            .priority(1)
            .build();

        // When & Then
        mockMvc.perform(put("/api/v1/fraud-rules/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.ruleName", is("UPDATED_AMOUNT_RULE")))
            .andExpect(jsonPath("$.thresholdValue", is(15000)));

        verify(fraudRuleService).updateFraudRule(eq(1L), any(FraudRule.class));
    }

    @Test
    void deleteRule_ExistingRule_ShouldDeleteRule() throws Exception {
        // Given
        doNothing().when(fraudRuleService).deleteFraudRule(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/fraud-rules/1"))
            .andExpect(status().isNoContent());

        verify(fraudRuleService).deleteFraudRule(1L);
    }

    @Test
    void toggleRule_ExistingRule_ShouldToggleStatus() throws Exception {
        // Given
        FraudRule toggledRule = FraudRule.builder()
            .id(1L)
            .ruleName("TEST_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Test amount rule")
            .thresholdValue(BigDecimal.valueOf(10000))
            .enabled(false) // Toggled to false
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .build();

        when(fraudRuleService.getFraudRuleById(1L)).thenReturn(Optional.of(testRule));
        when(fraudRuleService.updateFraudRule(eq(1L), any(FraudRule.class))).thenReturn(toggledRule);

        // When & Then
        mockMvc.perform(patch("/api/v1/fraud-rules/1/toggle"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.enabled", is(false)));

        verify(fraudRuleService).getFraudRuleById(1L);
        verify(fraudRuleService).updateFraudRule(eq(1L), any(FraudRule.class));
    }

    @Test
    void toggleRule_NonExistentRule_ShouldReturnNotFound() throws Exception {
        // Given
        when(fraudRuleService.getFraudRuleById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(patch("/api/v1/fraud-rules/999/toggle"))
            .andExpect(status().isNotFound());

        verify(fraudRuleService).getFraudRuleById(999L);
        verify(fraudRuleService, never()).updateFraudRule(any(), any());
    }

    @Test
    void quickCreateRule_ValidParameters_ShouldCreateRule() throws Exception {
        // Given
        FraudRule quickRule = FraudRule.builder()
            .id(2L)
            .ruleName("QUICK_AMOUNT_RULE")
            .ruleType("AMOUNT")
            .description("Quick created AMOUNT rule")
            .thresholdValue(BigDecimal.valueOf(25000))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.3))
            .priority(1)
            .build();

        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(quickRule);

        // When & Then
        mockMvc.perform(post("/api/v1/fraud-rules/quick-create")
                .param("ruleType", "AMOUNT")
                .param("ruleName", "QUICK_AMOUNT_RULE")
                .param("threshold", "25000")
                .param("description", "Quick test rule"))
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", is(2)))
            .andExpect(jsonPath("$.ruleName", is("QUICK_AMOUNT_RULE")))
            .andExpect(jsonPath("$.ruleType", is("AMOUNT")))
            .andExpect(jsonPath("$.thresholdValue", is(25000)));

        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }

    @Test
    void quickCreateRule_MissingParameters_ShouldReturnBadRequest() throws Exception {
        // When & Then
        mockMvc.perform(post("/api/v1/fraud-rules/quick-create")
                .param("ruleType", "AMOUNT")
                // Missing ruleName and threshold
                )
            .andExpect(status().isBadRequest());

        verify(fraudRuleService, never()).createFraudRule(any());
    }

    @Test
    void quickCreateRule_WithoutDescription_ShouldUseDefaultDescription() throws Exception {
        // Given
        FraudRule quickRule = FraudRule.builder()
            .id(3L)
            .ruleName("AUTO_DESC_RULE")
            .ruleType("FREQUENCY")
            .description("Quick created FREQUENCY rule")
            .thresholdValue(BigDecimal.valueOf(10))
            .enabled(true)
            .riskWeight(BigDecimal.valueOf(0.25))
            .priority(1)
            .build();

        when(fraudRuleService.createFraudRule(any(FraudRule.class))).thenReturn(quickRule);

        // When & Then
        mockMvc.perform(post("/api/v1/fraud-rules/quick-create")
                .param("ruleType", "FREQUENCY")
                .param("ruleName", "AUTO_DESC_RULE")
                .param("threshold", "10"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.description", containsString("Quick created FREQUENCY rule")));

        verify(fraudRuleService).createFraudRule(any(FraudRule.class));
    }
} 