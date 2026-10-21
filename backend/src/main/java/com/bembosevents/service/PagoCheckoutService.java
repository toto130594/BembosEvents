package com.bembosevents.service;

import com.bembosevents.dto.pago.IniciarPagoCheckoutRequest;
import com.bembosevents.dto.pago.PagoCheckoutResponse;

public interface PagoCheckoutService {

    PagoCheckoutResponse iniciarCheckout(
            String correoCliente,
            Integer idReserva,
            IniciarPagoCheckoutRequest request);
}