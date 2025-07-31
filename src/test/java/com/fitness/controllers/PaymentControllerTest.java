package com.fitness.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import com.fitness.dto.PaymentDTO;
import com.fitness.services.interfaces.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class PaymentControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @MockBean PaymentService paymentService;

    @MockBean
    JwtService jwtService;
    @MockBean
    UserDetailsServiceImpl userDetailsService;

    @Test
    @DisplayName("POST /api/payments — success")
    void createPayment_success() throws Exception {
        var req = Map.of("bookingId", 5, "promoCode", "FIT20");
        var dto = new PaymentDTO();
        dto.setClientSecret("sec_123");
        when(paymentService.createPaymentIntent(5L, "FIT20")).thenReturn(dto);

        mvc.perform(post("/api/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientSecret").value("sec_123"));
    }

    @Test
    @DisplayName("POST /api/payments/webhook — no body errors")
    void webhook_endpoint() throws Exception {
        doNothing().when(paymentService).handleWebhook(anyString(), anyString());

        mvc.perform(post("/api/payments/webhook")
                        .content("payload")
                        .header("Stripe-Signature", "sig"))
                .andExpect(status().isOk());
    }
}
