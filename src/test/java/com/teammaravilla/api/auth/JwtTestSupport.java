package com.teammaravilla.api.auth;

import io.jsonwebtoken.Jwts;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import javax.crypto.SecretKey;
import java.util.Base64;

public abstract class JwtTestSupport {
    protected static final SecretKey TEST_KEY = Jwts.SIG.HS256.key().build();

    @DynamicPropertySource
    static void jwtProperties(DynamicPropertyRegistry properties) {
        properties.add("security.jwt.secret", () -> Base64.getEncoder().encodeToString(TEST_KEY.getEncoded()));
        properties.add("security.jwt.expiration", () -> "PT15M");
    }
}
