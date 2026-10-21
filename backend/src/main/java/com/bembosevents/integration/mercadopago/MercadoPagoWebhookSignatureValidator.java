package com.bembosevents.integration.mercadopago;

import com.bembosevents.config.MercadoPagoProperties;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

@Component
public class MercadoPagoWebhookSignatureValidator {

    private static final String HMAC_ALGORITHM =
            "HmacSHA256";

    private final MercadoPagoProperties properties;

    public MercadoPagoWebhookSignatureValidator(
            MercadoPagoProperties properties) {

        this.properties =
                Objects.requireNonNull(properties);
    }

    public void validar(
            String xSignature,
            String xRequestId,
            String dataId) {

        if (xSignature == null
                || xSignature.isBlank()) {

            throw invalidSignature();
        }

        if (xRequestId == null
                || xRequestId.isBlank()) {

            throw invalidSignature();
        }

        if (dataId == null
                || dataId.isBlank()) {

            throw invalidSignature();
        }

        if (!properties.hasWebhookSecret()) {
            throw new IllegalStateException(
                    "MERCADOPAGO_WEBHOOK_SECRET_TEST no configurado");
        }

        SignatureParts parts =
                parse(
                        xSignature);

        String normalizedDataId =
                dataId
                        .trim()
                        .toLowerCase(
                                Locale.ROOT);

        String manifest =
                "id:"
                        + normalizedDataId
                        + ";request-id:"
                        + xRequestId.trim()
                        + ";ts:"
                        + parts.timestamp()
                        + ";";

        byte[] calculated =
                hmacSha256(
                        properties.getWebhookSecret(),
                        manifest);

        byte[] received =
                decodeHex(
                        parts.signature());

        if (!MessageDigest.isEqual(
                calculated,
                received)) {

            throw invalidSignature();
        }
    }

    private SignatureParts parse(
            String xSignature) {

        String timestamp =
                null;

        String signature =
                null;

        for (String part : xSignature.split(",")) {

            String trimmed =
                    part.trim();

            int separator =
                    trimmed.indexOf('=');

            if (separator <= 0
                    || separator == trimmed.length() - 1) {

                continue;
            }

            String key =
                    trimmed
                            .substring(
                                    0,
                                    separator)
                            .trim();

            String value =
                    trimmed
                            .substring(
                                    separator + 1)
                            .trim();

            if ("ts".equals(key)) {
                timestamp =
                        value;
            }
            else if ("v1".equals(key)) {
                signature =
                        value;
            }
        }

        if (timestamp == null
                || timestamp.isBlank()
                || signature == null
                || signature.isBlank()) {

            throw invalidSignature();
        }

        return new SignatureParts(
                timestamp,
                signature);
    }

    private byte[] hmacSha256(
            String secret,
            String manifest) {

        try {
            Mac mac =
                    Mac.getInstance(
                            HMAC_ALGORITHM);

            mac.init(
                    new SecretKeySpec(
                            secret.getBytes(
                                    StandardCharsets.UTF_8),
                            HMAC_ALGORITHM));

            return mac.doFinal(
                    manifest.getBytes(
                            StandardCharsets.UTF_8));
        }
        catch (Exception ex) {
            throw new IllegalStateException(
                    "No se pudo validar la firma de Mercado Pago",
                    ex);
        }
    }

    private byte[] decodeHex(
            String value) {

        try {
            return HexFormat
                    .of()
                    .parseHex(
                            value);
        }
        catch (IllegalArgumentException ex) {
            throw invalidSignature();
        }
    }

    private BadCredentialsException invalidSignature() {

        return new BadCredentialsException(
                "Firma webhook Mercado Pago invalida");
    }

    private record SignatureParts(
            String timestamp,
            String signature) {
    }
}