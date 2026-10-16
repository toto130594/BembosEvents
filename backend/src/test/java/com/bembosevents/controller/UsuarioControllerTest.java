package com.bembosevents.controller;

import com.bembosevents.dto.usuario.UsuarioResponse;
import com.bembosevents.service.UsuarioAdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class UsuarioControllerTest {

    private UsuarioAdminService usuarioAdminService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {

        usuarioAdminService =
                mock(UsuarioAdminService.class);

        UsuarioController controller =
                new UsuarioController(
                        usuarioAdminService
                );

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void listarUsuariosDebeResponder200()
            throws Exception {

        UsuarioResponse response =
                new UsuarioResponse(
                        1,
                        "Maria",
                        "Admin",
                        "admin@bembos.test",
                        "999999999",
                        true,
                        List.of("ADMINISTRADOR"),
                        null,
                        null
                );

        when(usuarioAdminService.listar())
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/usuarios")
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$[0].correo")
                                .value("admin@bembos.test")
                );
    }

    @Test
    void crearUsuarioDebeResponder201()
            throws Exception {

        UsuarioResponse response =
                new UsuarioResponse(
                        2,
                        "Carlos",
                        "Ramirez",
                        "carlos@bembos.test",
                        "999999999",
                        true,
                        List.of("ENCARGADO_LOCAL"),
                        3,
                        "Plaza Norte"
                );

        when(usuarioAdminService.crear(any()))
                .thenReturn(response);

        mockMvc.perform(
                        post("/api/usuarios")
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content("""
                                        {
                                          "nombres": "Carlos",
                                          "apellidos": "Ramirez",
                                          "correo": "carlos@bembos.test",
                                          "telefono": "999999999",
                                          "password": "Segura123",
                                          "rol": "ENCARGADO_LOCAL",
                                          "idSede": 3
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.roles[0]")
                                .value("ENCARGADO_LOCAL")
                )
                .andExpect(
                        jsonPath("$.idSede")
                                .value(3)
                );
    }
}