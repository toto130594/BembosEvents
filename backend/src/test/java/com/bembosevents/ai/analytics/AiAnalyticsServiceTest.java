package com.bembosevents.ai.analytics;

import com.bembosevents.ai.orchestration.AiIntentType;
import com.bembosevents.ai.tool.AiToolType;
import com.bembosevents.entity.AiAnalyticsEvent;
import com.bembosevents.repository.AiAnalyticsEventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AiAnalyticsServiceTest {

    @Test
    void persisteSoloMetadataSeguraDeExito() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        AiAnalyticsService service =
                new AiAnalyticsService(
                        repository);

        service.record(
                new AiAnalyticsEventCommand(
                        10,
                        3,
                        AiIntentType.CONSULTAR_DISPONIBILIDAD,
                        AiToolType.CONSULTAR_DISPONIBILIDAD,
                        AiAnalyticsRoute.TOOL,
                        true,
                        24,
                        "debe eliminarse"));

        ArgumentCaptor<AiAnalyticsEvent> captor =
                ArgumentCaptor.forClass(
                        AiAnalyticsEvent.class);

        verify(repository)
                .save(
                        captor.capture());

        AiAnalyticsEvent event =
                captor.getValue();

        assertEquals(
                10,
                event.getIdConversacion());

        assertEquals(
                3,
                event.getIdSede());

        assertEquals(
                "CONSULTAR_DISPONIBILIDAD",
                event.getIntentType());

        assertEquals(
                "CONSULTAR_DISPONIBILIDAD",
                event.getToolType());

        assertEquals(
                "TOOL",
                event.getExecutionRoute());

        assertEquals(
                true,
                event.getExitoso());

        assertEquals(
                24L,
                event.getDurationMs());

        assertNull(
                event.getErrorType());
    }

    @Test
    void persisteTipoDeErrorSinMensaje() {
        AiAnalyticsEventRepository repository =
                mock(
                        AiAnalyticsEventRepository.class);

        AiAnalyticsService service =
                new AiAnalyticsService(
                        repository);

        service.record(
                new AiAnalyticsEventCommand(
                        10,
                        null,
                        AiIntentType.CHAT_GENERAL,
                        null,
                        AiAnalyticsRoute.PROVIDER,
                        false,
                        51,
                        "IllegalStateException"));

        ArgumentCaptor<AiAnalyticsEvent> captor =
                ArgumentCaptor.forClass(
                        AiAnalyticsEvent.class);

        verify(repository)
                .save(
                        captor.capture());

        AiAnalyticsEvent event =
                captor.getValue();

        assertEquals(
                "CHAT_GENERAL",
                event.getIntentType());

        assertNull(
                event.getToolType());

        assertEquals(
                "PROVIDER",
                event.getExecutionRoute());

        assertEquals(
                false,
                event.getExitoso());

        assertEquals(
                "IllegalStateException",
                event.getErrorType());
    }
}