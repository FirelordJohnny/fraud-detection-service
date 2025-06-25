package com.faud.frauddetection.internal.controller;

import com.faud.frauddetection.entity.FraudDetectionResultEntity;
import com.faud.frauddetection.security.JwtUtil;
import com.faud.frauddetection.service.FraudDetectionResultService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalFraudAnalysisController.class)
@AutoConfigureMockMvc(addFilters = false) // Disable security filters for this test
class InternalFraudAnalysisControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FraudDetectionResultService resultService;

    @MockBean
    private JwtUtil jwtUtil;

    @Test
    void testGetAllResults() throws Exception {
        FraudDetectionResultEntity result = new FraudDetectionResultEntity();
        result.setId(1L);
        result.setTransactionId("txn-123");
        result.setFraud(true);

        when(resultService.getAllResults()).thenReturn(Collections.singletonList(result));

        mockMvc.perform(get("/internal/fraud-analysis/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value("txn-123"));
    }

    @Test
    void testGetResultById_Found() throws Exception {
        FraudDetectionResultEntity result = new FraudDetectionResultEntity();
        result.setId(1L);
        result.setTransactionId("txn-123");

        when(resultService.getResultById(1L)).thenReturn(Optional.of(result));

        mockMvc.perform(get("/internal/fraud-analysis/results/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactionId").value("txn-123"));
    }

    @Test
    void testGetResultById_NotFound() throws Exception {
        when(resultService.getResultById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/internal/fraud-analysis/results/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllResults_Empty() throws Exception {
        when(resultService.getAllResults()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/internal/fraud-analysis/results"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetHealth() throws Exception {
        mockMvc.perform(get("/internal/fraud-analysis/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Internal Fraud Analysis API is healthy"));
    }
} 