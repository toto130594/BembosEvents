package com.bembosevents.security;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityTokenServiceTest {

    private final SecurityTokenService service =
            new SecurityTokenService();

    @Test
    void generateOpaqueTokenProducesUrlSafeToken() {
        String token =
                service.generateOpaqueToken();

        assertEquals(43, token.length());
        assertTrue(
                token.matches(
                        "^[A-Za-z0-9_-]+$"
                )
        );
    }

    @Test
    void generateOpaqueTokenProducesDifferentValues() {
        String first =
                service.generateOpaqueToken();

        String second =
                service.generateOpaqueToken();

        assertNotEquals(first, second);
    }

    @Test
    void generateOpaqueTokenDoesNotRepeatInSmallSample() {
        Set<String> tokens =
                new HashSet<>();

        for (int i = 0; i < 100; i++) {
            assertTrue(
                    tokens.add(
                            service.generateOpaqueToken()
                    )
            );
        }
    }

    @Test
    void hashTokenUsesSha256() {
        assertEquals(
                "ba7816bf8f01cfea414140de5dae2223"
                        + "b00361a396177a9cb410ff61f20015ad",
                service.hashToken("abc")
        );
    }

    @Test
    void hashTokenIsDeterministic() {
        String token =
                service.generateOpaqueToken();

        assertEquals(
                service.hashToken(token),
                service.hashToken(token)
        );
    }

    @Test
    void hashTokenRejectsNullAndBlank() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.hashToken(null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.hashToken("")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.hashToken("   ")
        );
    }

    @Test
    void matchesAcceptsCorrectRawToken() {
        String token =
                service.generateOpaqueToken();

        String hash =
                service.hashToken(token);

        assertTrue(
                service.matches(
                        token,
                        hash
                )
        );
    }

    @Test
    void matchesRejectsWrongToken() {
        String first =
                service.generateOpaqueToken();

        String second =
                service.generateOpaqueToken();

        assertFalse(
                service.matches(
                        second,
                        service.hashToken(first)
                )
        );
    }

    @Test
    void matchesRejectsInvalidInputs() {
        assertFalse(
                service.matches(
                        null,
                        null
                )
        );

        assertFalse(
                service.matches(
                        "",
                        ""
                )
        );

        assertFalse(
                service.matches(
                        "token",
                        "hash-invalido"
                )
        );
    }
}
