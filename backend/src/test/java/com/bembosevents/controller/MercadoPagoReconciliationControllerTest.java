package com.bembosevents.controller;

import com.bembosevents.dto.pago.PagoReconciliationItemResponse;
import com.bembosevents.dto.pago.PagoReconciliationResponse;
import com.bembosevents.service.MercadoPagoReconciliationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class MercadoPagoReconciliationControllerTest {

    @Mock
    private MercadoPagoReconciliationService reconciliationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        mockMvc =
                MockMvcBuilders
                        .standaloneSetup(
                                new MercadoPagoReconciliationController(
                                        reconciliationService))
                        .build();
    }

    @Test
    void debeReconciliarUnPago() throws Exception {

        when(reconciliationService.reconciliarPago(
                25))
                .thenReturn(
                        new PagoReconciliationItemResponse(
                                25,
                                "ORD-1109",
                                "PENDIENTE",
                                "processed",
                                "accredited",
                                "APROBADO",
                                "ACTUALIZADO",
                                "Estado local sincronizado con Mercado Pago"));

        mockMvc.perform(
                        post(
                                "/api/backoffice/pagos/mercado-pago/25/reconciliar"))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.resultado")
                                .value(
                                        "ACTUALIZADO"))
                .andExpect(
                        jsonPath("$.estadoObjetivo")
                                .value(
                                        "APROBADO"));

        verify(reconciliationService)
                .reconciliarPago(
                        25);
    }

    @Test
    void debeReconciliarLote() throws Exception {

        when(reconciliationService.reconciliarLote())
                .thenReturn(
                        new PagoReconciliationResponse(
                                1,
                                1,
                                0,
                                0,
                                0,
                                0,
                                List.of(
                                        new PagoReconciliationItemResponse(
                                                25,
                                                "ORD-1109",
                                                "PENDIENTE",
                                                "processed",
                                                "accredited",
                                                "APROBADO",
                                                "ACTUALIZADO",
                                                "Estado local sincronizado con Mercado Pago"))));

        mockMvc.perform(
                        post(
                                "/api/backoffice/pagos/mercado-pago/reconciliar"))
                .andExpect(
                        status().isOk())
                .andExpect(
                        jsonPath("$.total")
                                .value(
                                        1))
                .andExpect(
                        jsonPath("$.actualizados")
                                .value(
                                        1));

        verify(reconciliationService)
                .reconciliarLote();
    }
}