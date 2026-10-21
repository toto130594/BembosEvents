package com.bembosevents.integration.mercadopago;

import com.bembosevents.config.MercadoPagoProperties;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MercadoPagoWebhookSignatureValidatorTest {

    private static final String SECRET =
            "test-webhook-secret";

    private static final String DATA_ID =
            "ORD-TEST-1105";

    private static final String REQUEST_ID =
            "req-test-1105";

    private static final String VALID_SIGNATURE =
            "ts=1742505638683,"
                    + "v1=6ed727c232f9d01244044dc85404640d"
                    + "54287b753536abca5b788e72fc493ddf";

    @Test
    void debeAceptarFirmaValida() {

        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(
                        propertiesConSecret());

        assertDoesNotThrow(
                () -> validator.validar(
                        VALID_SIGNATURE,
                        REQUEST_ID,
                        DATA_ID));
    }

    @Test
    void debeRechazarFirmaInvalida() {

        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(
                        propertiesConSecret());

        assertThrows(
                BadCredentialsException.class,
                () -> validator.validar(
                        "ts=1742505638683,"
                                + "v1=aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
                                + "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
                        REQUEST_ID,
                        DATA_ID));
    }

    @Test
    void debeRechazarFirmaAusente() {

        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(
                        propertiesConSecret());

        assertThrows(
                BadCredentialsException.class,
                () -> validator.validar(
                        null,
                        REQUEST_ID,
                        DATA_ID));
    }

    @Test
    void debeFallarCerradoSinSecretServidor() {

        MercadoPagoProperties properties =
                new MercadoPagoProperties();

        properties.setWebhookSecret("");

        MercadoPagoWebhookSignatureValidator validator =
                new MercadoPagoWebhookSignatureValidator(
                        properties);

        assertThrows(
                IllegalStateException.class,
                () -> validator.validar(
                        VALID_SIGNATURE,
                        REQUEST_ID,
                        DATA_ID));
    }

    private MercadoPagoProperties propertiesConSecret() {

        MercadoPagoProperties properties =
                new MercadoPagoProperties();

        properties.setWebhookSecret(
                SECRET);

        return properties;
    }
}