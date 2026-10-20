package com.bembosevents.security.mfa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class MfaRecoveryCodeService {

    private final SecureRandom secureRandom;

    private final int codeCount;

    private final int randomBytes;

    @Autowired

    public MfaRecoveryCodeService(
            @Value("${app.security.mfa.recovery-code.count:8}")
            int codeCount,
            @Value("${app.security.mfa.recovery-code.random-bytes:10}")
            int randomBytes) {

        this(
                new SecureRandom(),
                codeCount,
                randomBytes);
    }

    MfaRecoveryCodeService(
            SecureRandom secureRandom,
            int codeCount,
            int randomBytes) {

        if (secureRandom == null) {
            throw new IllegalArgumentException(
                    "secureRandom requerido");
        }

        if (codeCount < 1
                || codeCount > 20) {

            throw new IllegalArgumentException(
                    "codeCount debe estar entre 1 y 20");
        }

        if (randomBytes < 10) {
            throw new IllegalArgumentException(
                    "randomBytes debe ser al menos 10");
        }

        this.secureRandom =
                secureRandom;

        this.codeCount =
                codeCount;

        this.randomBytes =
                randomBytes;
    }

    public List<MfaRecoveryCodeMaterial>
            generateBatch() {

        Set<String> rawCodes =
                new LinkedHashSet<>();

        while (
            rawCodes.size()
                    < codeCount
        ) {
            rawCodes.add(
                    generateRawCode());
        }

        List<MfaRecoveryCodeMaterial> result =
                new ArrayList<>();

        for (String rawCode : rawCodes) {

            result.add(
                    new MfaRecoveryCodeMaterial(
                            rawCode,
                            hash(
                                    rawCode)));
        }

        return List.copyOf(
                result);
    }

    public String hash(
            String rawCode) {

        String normalized =
                normalize(
                        rawCode);

        try {
            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256");

            byte[] hash =
                    digest.digest(
                            normalized.getBytes(
                                    StandardCharsets.UTF_8));

            return HexFormat
                    .of()
                    .formatHex(
                            hash);
        }
        catch (NoSuchAlgorithmException ex) {

            throw new IllegalStateException(
                    "SHA-256 no disponible",
                    ex);
        }
    }

    public boolean matches(
            String rawCode,
            String expectedHash) {

        if (rawCode == null
                || rawCode.isBlank()
                || expectedHash == null
                || !expectedHash.matches(
                        "[0-9a-fA-F]{64}")) {

            return false;
        }

        byte[] actual =
                HexFormat
                        .of()
                        .parseHex(
                                hash(
                                        rawCode));

        byte[] expected =
                HexFormat
                        .of()
                        .parseHex(
                                expectedHash
                                        .toLowerCase(
                                                Locale.ROOT));

        return MessageDigest.isEqual(
                actual,
                expected);
    }

    private String generateRawCode() {

        byte[] random =
                new byte[randomBytes];

        secureRandom.nextBytes(
                random);

        String value =
                Base32Codec
                        .encode(
                                random)
                        .toUpperCase(
                                Locale.ROOT);

        return group(
                value,
                4);
    }

    private String normalize(
            String rawCode) {

        if (rawCode == null
                || rawCode.isBlank()) {

            throw new IllegalArgumentException(
                    "Recovery code requerido");
        }

        String normalized =
                rawCode
                        .replace("-", "")
                        .replaceAll("\\s+", "")
                        .toUpperCase(
                                Locale.ROOT);

        if (!normalized.matches(
                "[A-Z2-7]+")) {

            throw new IllegalArgumentException(
                    "Recovery code invalido");
        }

        return normalized;
    }

    private String group(
            String value,
            int groupSize) {

        StringBuilder result =
                new StringBuilder();

        for (
            int i = 0;
            i < value.length();
            i++
        ) {
            if (
                i > 0
                        && i % groupSize
                        == 0
            ) {
                result.append(
                        '-');
            }

            result.append(
                    value.charAt(i));
        }

        return result.toString();
    }
}
