package com.bembosevents.controller;

import com.bembosevents.dto.pago.IniciarPagoCheckoutRequest;
import com.bembosevents.dto.pago.PagoCheckoutResponse;
import com.bembosevents.dto.pago.PagoResponse;
import com.bembosevents.service.PagoCheckoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
class PagoCheckoutControllerTest {

    @Mock
    private PagoCheckoutService pagoCheckoutService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new PagoCheckoutController(
                                        pagoCheckoutService))
                        .build();
    }

    @Test
    void debeIniciarCheckoutCliente() throws Exception {

        PagoResponse pago =
                new PagoResponse(
                        25,
                        10,
                        new BigDecimal("150.00"),
                        "MERCADO_PAGO",
                        "PENDIENTE",
                        "ORD-123",
                        null,
                        LocalDateTime.now());

        when(pagoCheckoutService.iniciarCheckout(
                "cliente@testuser.com",
                10,
                new IniciarPagoCheckoutRequest(
                        new BigDecimal("150.00"))))
                .thenReturn(
                        new PagoCheckoutResponse(
                                pago,
                                "MERCADO_PAGO",
                                "ORD-123",
                                "created",
                                "https://mercadopago.test/checkout/ORD-123"));

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "cliente@testuser.com",
                        "N/A",
                        java.util.List.of());

        mockMvc.perform(
                        post(
                                "/api/reservas/10/pagos/checkout")
                                .principal(auth)
                                .contentType(
                                        MediaType.APPLICATION_JSON)
                                .content(
                                        """
                                        {
                                          "monto": 150.00
                                        }
                                        """))
                .andExpect(
                        status().isCreated())
                .andExpect(
                        jsonPath("$.orderId")
                                .value("ORD-123"))
                .andExpect(
                        jsonPath("$.orderStatus")
                                .value("created"))
                .andExpect(
                        jsonPath("$.checkoutUrl")
                                .value(
                                        "https://mercadopago.test/checkout/ORD-123"))
                .andExpect(
                        jsonPath("$.pago.estadoPago")
                                .value("PENDIENTE"));

        verify(pagoCheckoutService)
                .iniciarCheckout(
                        "cliente@testuser.com",
                        10,
                        new IniciarPagoCheckoutRequest(
                                new BigDecimal("150.00")));
    }
}