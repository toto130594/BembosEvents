package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoRefundResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.MercadoPagoRefundService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MercadoPagoRefundControllerTest {

    @Mock
    private MercadoPagoRefundService refundService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new MercadoPagoRefundController(
                                        refundService))
                        .build();
    }

    @Test
    void debeReembolsarTotal() throws Exception {

        PagoResponse pago =
                new PagoResponse(
                        25,
                        10,
                        new BigDecimal(
                                "150.00"),
                        "MERCADO_PAGO",
                        "REEMBOLSADO",
                        "ORD-1108",
                        LocalDateTime.now(),
                        LocalDateTime.now());

        when(refundService.reembolsarTotal(
                25))
                .thenReturn(
                        new PagoRefundResponse(
                                pago,
                                "MERCADO_PAGO",
                                "ORD-1108",
                                "refunded",
                                "refunded",
                                true));

        mockMvc.perform(
                        post(
                                "/api/backoffice/pagos/25/reembolso"))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.pago.estadoPago")
                                .value(
                                        "REEMBOLSADO"))
                .andExpect(
                        jsonPath("$.orderId")
                                .value(
                                        "ORD-1108"))
                .andExpect(
                        jsonPath("$.providerCallExecuted")
                                .value(
                                        true));

        verify(refundService)
                .reembolsarTotal(
                        25);
    }
}