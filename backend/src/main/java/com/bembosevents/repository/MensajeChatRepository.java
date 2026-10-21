package com.bembosevents.repository;

import com.bembosevents.entity.MensajeChat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeChatRepository
        extends JpaRepository<MensajeChat, Integer> {

    List<MensajeChat>
    findByConversacion_IdConversacionOrderByFechaEnvioAscIdMensajeChatAsc(
            Integer idConversacion
    );
}