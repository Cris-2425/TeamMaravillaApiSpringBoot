package com.teammaravilla.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthenticationIntegrationTests extends JwtTestSupport {
    private static final String PASSWORD = "  Clave-Test-1234  ";
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtService tokens;
    private User user;

    @BeforeEach
    void createUser() {
        String name = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        user = users.saveAndFlush(new User(name, name + "@example.com", passwords.encode(PASSWORD)));
    }

    @Test
    void loginReturnsValidJwtAndMeUsesItsIdentity() throws Exception {
        String response = login(" " + user.getUsername().toUpperCase(Locale.ROOT) + " ", PASSWORD)
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(cookie().doesNotExist("JSESSIONID"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").value(900))
                .andExpect(jsonPath("$.user.id").value(user.getId().toString()))
                .andReturn().getResponse().getContentAsString();
        JsonNode body = json.readTree(response);
        String token = body.get("accessToken").asText();
        assertThat(token).isNotEqualTo(user.getId().toString());
        assertThat(body.get("user").size()).isEqualTo(3);
        assertThat(response).doesNotContain("password", "passwordHash", PASSWORD, user.getPasswordHash());
        var parsed = Jwts.parser().verifyWith(TEST_KEY).requireIssuer("teammaravilla-api")
                .build().parseSignedClaims(token);
        assertThat(parsed.getHeader().getAlgorithm()).isEqualTo("HS256");
        assertThat(parsed.getPayload().keySet()).containsExactlyInAnyOrder("iss", "sub", "iat", "exp");
        assertThat(parsed.getPayload().getSubject()).isEqualTo(user.getId().toString());
        assertThat(parsed.getPayload().getExpiration().getTime()
                - parsed.getPayload().getIssuedAt().getTime()).isEqualTo(900_000);

        mvc.perform(get("/api/auth/me").param("userId", UUID.randomUUID().toString())
                        .header("Authorization", "bEaReR " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId().toString()))
                .andExpect(jsonPath("$.username").value(user.getUsername()))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertThat(new LoginRequest(user.getUsername(), PASSWORD).toString()).doesNotContain(PASSWORD);
    }

    @Test
    void wrongPasswordAndUnknownUserHaveIdenticalGenericResponses() throws Exception {
        String wrong = login(user.getUsername(), "Incorrecta-123456")
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        String unknown = login("missing_" + UUID.randomUUID().toString().substring(0, 8), PASSWORD)
                .andExpect(status().isUnauthorized()).andReturn().getResponse().getContentAsString();
        assertThat(wrong).isEqualTo(unknown).doesNotContain(PASSWORD, user.getUsername(), user.getPasswordHash());
    }

    @Test
    void passwordIsNeitherTrimmedNorLowercased() throws Exception {
        login(user.getUsername(), PASSWORD.strip()).andExpect(status().isUnauthorized());
        login(user.getUsername(), PASSWORD.toLowerCase(Locale.ROOT)).andExpect(status().isUnauthorized());
    }

    @Test
    void missingTokenReturns401() throws Exception {
        mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Bearer"))
                .andExpect(cookie().doesNotExist("JSESSIONID"));
    }

    @Test
    void malformedTokenReturns401WithoutEchoingToken() throws Exception {
        String invalid = "not-a-valid-token";
        String response = me(invalid).andExpect(status().isUnauthorized())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain(invalid, "Exception", "stackTrace");
    }

    @Test
    void expiredTokenReturns401() throws Exception {
        String token = Jwts.builder().issuer("teammaravilla-api").subject(user.getId().toString())
                .issuedAt(Date.from(Instant.now().minusSeconds(120)))
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(TEST_KEY, Jwts.SIG.HS256).compact();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenWithoutExpirationReturns401() throws Exception {
        String token = Jwts.builder().issuer("teammaravilla-api").subject(user.getId().toString())
                .signWith(TEST_KEY, Jwts.SIG.HS256).compact();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void manipulatedSubjectCannotImpersonateAnotherUser() throws Exception {
        String[] parts = tokens.create(user.getId()).split("\\.");
        var payload = json.createObjectNode().put("iss", "teammaravilla-api")
                .put("sub", UUID.randomUUID().toString()).put("exp", Instant.now().plusSeconds(300).getEpochSecond());
        String manipulated = parts[0] + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(json.writeValueAsBytes(payload)) + "." + parts[2];
        me(manipulated).andExpect(status().isUnauthorized());
    }

    @Test
    void tokenSignedWithAnotherKeyReturns401() throws Exception {
        String token = Jwts.builder().issuer("teammaravilla-api").subject(user.getId().toString())
                .expiration(Date.from(Instant.now().plusSeconds(300)))
                .signWith(Jwts.SIG.HS256.key().build()).compact();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void unsignedTokenReturns401() throws Exception {
        String token = Jwts.builder().issuer("teammaravilla-api").subject(user.getId().toString())
                .expiration(Date.from(Instant.now().plusSeconds(300))).compact();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongIssuerReturns401() throws Exception {
        String token = Jwts.builder().issuer("another-api").subject(user.getId().toString())
                .expiration(Date.from(Instant.now().plusSeconds(300))).signWith(TEST_KEY).compact();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void deletedUserCannotUsePreviouslyIssuedToken() throws Exception {
        String token = tokens.create(user.getId());
        users.delete(user);
        users.flush();
        me(token).andExpect(status().isUnauthorized());
    }

    @Test
    void cookieAndQueryParameterCannotAuthenticate() throws Exception {
        String token = tokens.create(user.getId());
        mvc.perform(get("/api/auth/me").cookie(new Cookie("accessToken", token)))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/me").param("access_token", token)).andExpect(status().isUnauthorized());
    }

    @Test
    void otherRoutesRequireAuthentication() throws Exception {
        mvc.perform(get("/not-public")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/auth/login")).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidLoginRequestDoesNotEchoPassword() throws Exception {
        String response = login("", PASSWORD).andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain(PASSWORD);
    }

    private ResultActions login(String username, String password) throws Exception {
        return mvc.perform(post("/api/auth/login").contentType(APPLICATION_JSON)
                .content(json.writeValueAsBytes(Map.of("username", username, "password", password))));
    }

    private ResultActions me(String token) throws Exception {
        return mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token));
    }
}
