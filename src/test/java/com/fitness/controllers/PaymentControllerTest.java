package com.fitness.controllers;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitness.config.security.JwtService;
import com.fitness.config.security.UserDetailsServiceImpl;
import com.fitness.dto.PaymentDTO;
import com.fitness.dto.PaymentFilter;
import com.fitness.enums.PaymentStatus;
import com.fitness.services.interfaces.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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
    @Test @DisplayName("GET /api/payments (admin search)")
    void searchPayments() throws Exception {
        // page with single DTO
        var dto = new PaymentDTO();
        dto.setId(10L);
        dto.setStatus(PaymentStatus.SUCCEEDED);
        Page<PaymentDTO> page =
                new PageImpl<>(List.of(dto), PageRequest.of(0, 20), 1);

        when(paymentService.search(any(PaymentFilter.class), any())).thenReturn(page);

        mvc.perform(get("/api/payments")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id").value(10));
    }

    @Test @DisplayName("GET /api/payments/{id} – admin")
    void getPayment() throws Exception {
        var dto = new PaymentDTO();
        dto.setId(42L);
        when(paymentService.getPayment(42L)).thenReturn(dto);

        mvc.perform(get("/api/payments/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42));
    }

    @Test @DisplayName("POST /api/payments/{id}/refund – admin")
    void refundPayment() throws Exception {
        doNothing().when(paymentService).refund(7L, 3000);

        mvc.perform(post("/api/payments/7/refund")
                        .param("amountCents", "3000"))
                .andExpect(status().isOk());

        verify(paymentService).refund(7L, 3000);
    }
}
