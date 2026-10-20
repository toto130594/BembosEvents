package com.bembosevents.security.mfa;

import java.io.ByteArrayOutputStream;
import java.util.Locale;

final class Base32Codec {

    private static final char[] ALPHABET =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
                    .toCharArray();

    private Base32Codec() {
    }

    static String encode(
            byte[] input) {

        if (input == null
                || input.length == 0) {

            throw new IllegalArgumentException(
                    "input requerido");
        }

        StringBuilder output =
                new StringBuilder();

        int buffer =
                0;

        int bitsLeft =
                0;

        for (byte value : input) {

            buffer =
                    (buffer << 8)
                            | (value & 0xff);

            bitsLeft += 8;

            while (bitsLeft >= 5) {

                int index =
                        (buffer >>
                                (bitsLeft - 5))
                                & 0x1f;

                bitsLeft -= 5;

                output.append(
                        ALPHABET[index]);
            }
        }

        if (bitsLeft > 0) {

            int index =
                    (buffer <<
                            (5 - bitsLeft))
                            & 0x1f;

            output.append(
                    ALPHABET[index]);
        }

        return output.toString();
    }

    static byte[] decode(
            String encoded) {

        if (encoded == null
                || encoded.isBlank()) {

            throw new IllegalArgumentException(
                    "Base32 requerido");
        }

        String normalized =
                encoded
                        .trim()
                        .replace("=", "")
                        .replaceAll("\\s+", "")
                        .toUpperCase(
                                Locale.ROOT);

        ByteArrayOutputStream output =
                new ByteArrayOutputStream();

        int buffer =
                0;

        int bitsLeft =
                0;

        for (int i = 0;
             i < normalized.length();
             i++) {

            int value =
                    decodeCharacter(
                            normalized.charAt(i));

            buffer =
                    (buffer << 5)
                            | value;

            bitsLeft += 5;

            if (bitsLeft >= 8) {

                bitsLeft -= 8;

                output.write(
                        (buffer >> bitsLeft)
                                & 0xff);
            }
        }

        byte[] result =
                output.toByteArray();

        if (result.length == 0) {
            throw new IllegalArgumentException(
                    "Base32 invalido");
        }

        return result;
    }

    private static int decodeCharacter(
            char value) {

        if (value >= 'A'
                && value <= 'Z') {

            return value - 'A';
        }

        if (value >= '2'
                && value <= '7') {

            return value - '2' + 26;
        }

        throw new IllegalArgumentException(
                "Base32 invalido");
    }
}
