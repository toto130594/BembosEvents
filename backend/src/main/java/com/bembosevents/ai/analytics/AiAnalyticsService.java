package com.bembosevents.ai.analytics;

import com.bembosevents.entity.AiAnalyticsEvent;
import com.bembosevents.repository.AiAnalyticsEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AiAnalyticsService {

    private final AiAnalyticsEventRepository repository;

    public AiAnalyticsService(
            AiAnalyticsEventRepository repository) {

        this.repository =
                repository;
    }

    @Transactional(
            propagation = Propagation.REQUIRES_NEW)
    public void record(
            AiAnalyticsEventCommand command) {

        if (command == null) {
            throw new IllegalArgumentException(
                    "El evento analitico es obligatorio");
        }

        AiAnalyticsEvent event =
                new AiAnalyticsEvent();

        event.setIdConversacion(
                command.idConversacion());

        event.setIdSede(
                command.idSede());

        event.setIntentType(
                command.intentType()
                        .name());

        event.setToolType(
                command.toolType() == null
                        ? null
                        : command.toolType()
                                .name());

        event.setExecutionRoute(
                command.route()
                        .name());

        event.setExitoso(
                command.exitoso());

        event.setDurationMs(
                command.durationMs());

        event.setErrorType(
                command.errorType());

        event.setFechaEvento(
                LocalDateTime.now());

        repository.save(
                event);
    }
}