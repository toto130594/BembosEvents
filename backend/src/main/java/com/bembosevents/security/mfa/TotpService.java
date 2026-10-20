package com.bembosevents.security.mfa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Locale;

@Service
public class TotpService {

    private static final String HMAC_ALGORITHM =
            "HmacSHA1";

    private final SecureRandom secureRandom;

    private final int secretBytes;

    private final int digits;

    private final long periodSeconds;

    private final int allowedWindow;

    private final String issuer;

    @Autowired

    public TotpService(
            @Value("${app.security.mfa.totp.secret-bytes:20}")
            int secretBytes,
            @Value("${app.security.mfa.totp.digits:6}")
            int digits,
            @Value("${app.security.mfa.totp.period-seconds:30}")
            long periodSeconds,
            @Value("${app.security.mfa.totp.allowed-window:1}")
            int allowedWindow,
            @Value("${app.security.mfa.issuer:Bembos Events}")
            String issuer) {

        this(
                new SecureRandom(),
                secretBytes,
                digits,
                periodSeconds,
                allowedWindow,
                issuer);
    }

    TotpService(
            SecureRandom secureRandom,
            int secretBytes,
            int digits,
            long periodSeconds,
            int allowedWindow,
            String issuer) {

        if (secureRandom == null) {
            throw new IllegalArgumentException(
                    "secureRandom requerido");
        }

        if (secretBytes < 20) {
            throw new IllegalArgumentException(
                    "secretBytes debe ser al menos 20");
        }

        if (digits < 6
                || digits > 8) {

            throw new IllegalArgumentException(
                    "digits debe estar entre 6 y 8");
        }

        if (periodSeconds < 1) {
            throw new IllegalArgumentException(
                    "periodSeconds debe ser mayor que cero");
        }

        if (allowedWindow < 0
                || allowedWindow > 2) {

            throw new IllegalArgumentException(
                    "allowedWindow debe estar entre 0 y 2");
        }

        if (issuer == null
                || issuer.isBlank()) {

            throw new IllegalArgumentException(
                    "issuer requerido");
        }

        this.secureRandom =
                secureRandom;

        this.secretBytes =
                secretBytes;

        this.digits =
                digits;

        this.periodSeconds =
                periodSeconds;

        this.allowedWindow =
                allowedWindow;

        this.issuer =
                issuer.trim();
    }

    public String generateSecret() {

        byte[] bytes =
                new byte[secretBytes];

        secureRandom.nextBytes(
                bytes);

        return Base32Codec.encode(
                bytes);
    }

    public String generateCode(
            String base32Secret,
            Instant instant) {

        if (instant == null) {
            throw new IllegalArgumentException(
                    "instant requerido");
        }

        long counter =
                Math.floorDiv(
                        instant.getEpochSecond(),
                        periodSeconds);

        return generateCodeForCounter(
                base32Secret,
                counter);
    }

    public boolean verify(
            String base32Secret,
            String code,
            Instant instant) {

        if (instant == null
                || code == null) {

            return false;
        }

        String normalizedCode =
                code.trim();

        if (
            normalizedCode.length()
                    != digits
            || !normalizedCode.matches(
                    "\\d{" + digits + "}")
        ) {
            return false;
        }

        long currentCounter =
                Math.floorDiv(
                        instant.getEpochSecond(),
                        periodSeconds);

        for (
            int offset = -allowedWindow;
            offset <= allowedWindow;
            offset++
        ) {
            long counter =
                    currentCounter + offset;

            if (counter < 0) {
                continue;
            }

            String expected =
                    generateCodeForCounter(
                            base32Secret,
                            counter);

            if (
                MessageDigest.isEqual(
                        expected.getBytes(
                                StandardCharsets.US_ASCII),
                        normalizedCode.getBytes(
                                StandardCharsets.US_ASCII))
            ) {
                return true;
            }
        }

        return false;
    }

    public String buildOtpAuthUri(
            String accountName,
            String base32Secret) {

        if (accountName == null
                || accountName.isBlank()) {

            throw new IllegalArgumentException(
                    "accountName requerido");
        }

        byte[] ignored =
                Base32Codec.decode(
                        base32Secret);

        if (ignored.length == 0) {
            throw new IllegalArgumentException(
                    "Secreto TOTP invalido");
        }

        String normalizedSecret =
                base32Secret
                        .trim()
                        .replaceAll("\\s+", "")
                        .toUpperCase(
                                Locale.ROOT);

        String label =
                urlEncode(
                        issuer)
                        + ":"
                        + urlEncode(
                                accountName.trim());

        return "otpauth://totp/"
                + label
                + "?secret="
                + normalizedSecret
                + "&issuer="
                + urlEncode(
                        issuer)
                + "&algorithm=SHA1"
                + "&digits="
                + digits
                + "&period="
                + periodSeconds;
    }

    private String generateCodeForCounter(
            String base32Secret,
            long counter) {

        byte[] secret =
                Base32Codec.decode(
                        base32Secret);

        byte[] counterBytes =
                ByteBuffer
                        .allocate(
                                Long.BYTES)
                        .putLong(
                                counter)
                        .array();

        try {
            Mac mac =
                    Mac.getInstance(
                            HMAC_ALGORITHM);

            mac.init(
                    new SecretKeySpec(
                            secret,
                            HMAC_ALGORITHM));

            byte[] hmac =
                    mac.doFinal(
                            counterBytes);

            int offset =
                    hmac[hmac.length - 1]
                            & 0x0f;

            int binary =
                    ((hmac[offset] & 0x7f) << 24)
                            | ((hmac[offset + 1] & 0xff) << 16)
                            | ((hmac[offset + 2] & 0xff) << 8)
                            | (hmac[offset + 3] & 0xff);

            int modulus =
                    powerOfTen(
                            digits);

            int otp =
                    binary
                            % modulus;

            return String.format(
                    Locale.ROOT,
                    "%0" + digits + "d",
                    otp);
        }
        catch (GeneralSecurityException ex) {

            throw new IllegalStateException(
                    "No se pudo calcular TOTP",
                    ex);
        }
    }

    private int powerOfTen(
            int exponent) {

        int result =
                1;

        for (
            int i = 0;
            i < exponent;
            i++
        ) {
            result *= 10;
        }

        return result;
    }

    private String urlEncode(
            String value) {

        return URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8)
                .replace(
                        "+",
                        "%20");
    }
}
