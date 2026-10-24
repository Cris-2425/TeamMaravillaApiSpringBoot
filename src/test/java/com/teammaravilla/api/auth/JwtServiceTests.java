package com.teammaravilla.api.auth;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.util.Base64;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTests {
    @ParameterizedTest
    @ValueSource(strings = {"", "not-base64"})
    void rejectsInvalidSecretConfiguration(String secret) {
        assertThatThrownBy(() -> new JwtService(secret, "PT15M"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("JWT_SECRET debe ser Base64 de 32 bytes aleatorios");
    }

    @Test
    void rejectsShortKeys() {
        String shortKey = Base64.getEncoder().encodeToString(new byte[16]);
        assertThatThrownBy(() -> new JwtService(shortKey, "PT15M"))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"PT0S", "-PT1S", "PT0.5S", "invalid-duration"})
    void rejectsInvalidExpirationConfiguration(String expiration) {
        String secret = Base64.getEncoder().encodeToString(Jwts.SIG.HS256.key().build().getEncoded());
        assertThatThrownBy(() -> new JwtService(secret, expiration))
                .isInstanceOf(IllegalStateException.class);
    }
}
