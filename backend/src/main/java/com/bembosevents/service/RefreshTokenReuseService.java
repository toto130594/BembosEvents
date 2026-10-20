package com.bembosevents.service;

import com.bembosevents.domain.TipoEventoSeguridad;
import com.bembosevents.entity.RefreshTokenHistory;
import com.bembosevents.entity.SesionUsuario;
import com.bembosevents.repository.RefreshTokenHistoryRepository;
import com.bembosevents.repository.SesionUsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class RefreshTokenReuseService {

    private final RefreshTokenHistoryRepository
            refreshTokenHistoryRepository;

    private final SesionUsuarioRepository
            sesionUsuarioRepository;

    private final SecurityAuditService
            securityAuditService;

    public RefreshTokenReuseService(
            RefreshTokenHistoryRepository
                    refreshTokenHistoryRepository,
            SesionUsuarioRepository
                    sesionUsuarioRepository,
            SecurityAuditService
                    securityAuditService) {

        this.refreshTokenHistoryRepository =
                refreshTokenHistoryRepository;

        this.sesionUsuarioRepository =
                sesionUsuarioRepository;

        this.securityAuditService =
                securityAuditService;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public boolean detectAndRevokeIfReused(
            String refreshTokenHash) {

        if (refreshTokenHash == null
                || refreshTokenHash.isBlank()) {

            return false;
        }

        Optional<RefreshTokenHistory> historyOptional =
                refreshTokenHistoryRepository
                        .findByRefreshTokenHashForUpdate(
                                refreshTokenHash);

        if (historyOptional.isEmpty()) {
            return false;
        }

        RefreshTokenHistory history =
                historyOptional.get();

        if (history.getSesionUsuario() == null
                || history.getSesionUsuario()
                        .getIdSesionUsuario() == null) {

            throw new IllegalStateException(
                    "Historial refresh sin sesion asociada");
        }

        Long idSesionUsuario =
                history.getSesionUsuario()
                        .getIdSesionUsuario();

        SesionUsuario sesion =
                sesionUsuarioRepository
                        .findByIdSesionUsuarioForUpdate(
                                idSesionUsuario)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Sesion asociada a refresh historico no encontrada"));

        LocalDateTime now =
                LocalDateTime.now(
                        ZoneOffset.UTC);

        if (sesion.getFechaRevocacion() == null) {

            sesion.setFechaRevocacion(
                    now);

            sesionUsuarioRepository.save(
                    sesion);
        }

        /*
         * Esta auditoria participa en la transaccion de refresh.
         * Por tanto revocacion + auditoria hacen commit juntas
         * y no hace rollback cuando refresh
         * devuelve posteriormente un 401 generico.
         */
        securityAuditService.record(
                sesion.getUsuario(),
                TipoEventoSeguridad.REFRESH_TOKEN_REUTILIZADO,
                false,
                "Reutilizacion de refresh token detectada; sesion revocada");

        return true;
    }
}