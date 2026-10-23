package com.bembosevents.repository;

import com.bembosevents.entity.AiAnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface AiAnalyticsEventRepository
        extends JpaRepository<AiAnalyticsEvent, Long> {

    List<AiAnalyticsEvent>
    findAllByFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
            LocalDateTime desdeInclusive,
            LocalDateTime hastaExclusive);

    List<AiAnalyticsEvent>
    findAllByIdSedeAndFechaEventoGreaterThanEqualAndFechaEventoLessThanOrderByFechaEventoAsc(
            Integer idSede,
            LocalDateTime desdeInclusive,
            LocalDateTime hastaExclusive);
}