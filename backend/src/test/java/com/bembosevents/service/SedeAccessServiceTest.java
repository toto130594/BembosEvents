package com.bembosevents.service;

import com.bembosevents.entity.Sede;
import com.bembosevents.entity.UsuarioSede;
import com.bembosevents.repository.UsuarioSedeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SedeAccessServiceTest {

    @Mock
    private UsuarioSedeRepository usuarioSedeRepository;

    @Test
    void administradorPuedeAccederCualquierSede() {

        SedeAccessService service =
                new SedeAccessService(
                        usuarioSedeRepository
                );

        Authentication authentication =
                autenticacion(
                        "admin@bembos.test",
                        "ROLE_ADMINISTRADOR"
                );

        assertTrue(
                service.puedeAccederSede(
                        authentication,
                        999
                )
        );

        verifyNoInteractions(
                usuarioSedeRepository
        );
    }

    @Test
    void encargadoPuedeAccederSuSede() {

        SedeAccessService service =
                new SedeAccessService(
                        usuarioSedeRepository
                );

        Authentication authentication =
                autenticacion(
                        "encargado@bembos.test",
                        "ROLE_ENCARGADO_LOCAL"
                );

        when(
                usuarioSedeRepository
                        .existsByUsuario_CorreoIgnoreCaseAndSede_IdSedeAndActivoTrue(
                                "encargado@bembos.test",
                                2
                        )
        ).thenReturn(true);

        assertTrue(
                service.puedeAccederSede(
                        authentication,
                        2
                )
        );
    }

    @Test
    void encargadoNoPuedeAccederOtraSede() {

        SedeAccessService service =
                new SedeAccessService(
                        usuarioSedeRepository
                );

        Authentication authentication =
                autenticacion(
                        "encargado@bembos.test",
                        "ROLE_ENCARGADO_LOCAL"
                );

        when(
                usuarioSedeRepository
                        .existsByUsuario_CorreoIgnoreCaseAndSede_IdSedeAndActivoTrue(
                                "encargado@bembos.test",
                                5
                        )
        ).thenReturn(false);

        assertFalse(
                service.puedeAccederSede(
                        authentication,
                        5
                )
        );
    }

    @Test
    void obtenerSedeAsignadaDevuelveId() {

        SedeAccessService service =
                new SedeAccessService(
                        usuarioSedeRepository
                );

        Sede sede = new Sede();
        sede.setIdSede(3);

        UsuarioSede asignacion =
                new UsuarioSede();

        asignacion.setSede(sede);
        asignacion.setActivo(true);

        when(
                usuarioSedeRepository
                        .findByUsuario_CorreoIgnoreCaseAndActivoTrue(
                                "encargado@bembos.test"
                        )
        ).thenReturn(
                Optional.of(asignacion)
        );

        assertEquals(
                3,
                service.obtenerSedeAsignada(
                        "encargado@bembos.test"
                )
        );
    }

    private Authentication autenticacion(
            String correo,
            String rol) {

        return new UsernamePasswordAuthenticationToken(
                correo,
                null,
                List.of(
                        new SimpleGrantedAuthority(
                                rol
                        )
                )
        );
    }
}