package com.bembosevents.repository;

import com.bembosevents.entity.RefreshTokenHistory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenHistoryRepository
        extends JpaRepository<RefreshTokenHistory, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select h
            from RefreshTokenHistory h
            join fetch h.sesionUsuario s
            join fetch s.usuario
            where h.refreshTokenHash = :refreshTokenHash
            """)
    Optional<RefreshTokenHistory>
    findByRefreshTokenHashForUpdate(
            @Param("refreshTokenHash")
            String refreshTokenHash
    );
}