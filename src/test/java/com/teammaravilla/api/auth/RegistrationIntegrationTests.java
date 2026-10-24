package com.teammaravilla.api.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RegistrationIntegrationTests {
    private static final String PASSWORD = "SoloPruebas-123456";

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder encoder;

    @Test
    void registersWithNormalizedIdentityAndStoresOnlyBcrypt() throws Exception {
        String name = uniqueName();
        RegisterRequest request = new RegisterRequest(" " + name.toUpperCase(Locale.ROOT) + " ",
                " " + name.toUpperCase(Locale.ROOT) + "@EXAMPLE.COM ", PASSWORD);
        JsonNode response = json.readTree(send(request)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(name))
                .andExpect(jsonPath("$.email").value(name + "@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist())
                .andReturn().getResponse().getContentAsString());

        assertThat(response.size()).isEqualTo(3);
        User saved = users.findById(UUID.fromString(response.get("id").asText())).orElseThrow();
        assertThat(saved.getPasswordHash()).isNotEqualTo(PASSWORD).hasSize(60).startsWith("$2a$12$");
        assertThat(encoder.matches(PASSWORD, saved.getPasswordHash())).isTrue();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(request.toString()).doesNotContain(PASSWORD);
    }

    @Test
    void rejectsDuplicateUsername() throws Exception {
        String name = uniqueName();
        create(name, name + "@example.com", PASSWORD);
        send(new RegisterRequest(name.toUpperCase(Locale.ROOT), uniqueName() + "@example.com", PASSWORD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rejectsDuplicateEmail() throws Exception {
        String email = uniqueName() + "@example.com";
        create(uniqueName(), email, PASSWORD);
        send(new RegisterRequest(uniqueName(), email.toUpperCase(Locale.ROOT), PASSWORD))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @ParameterizedTest
    @MethodSource("invalidRequests")
    void rejectsInvalidRequestWithoutWriting(RegisterRequest request) throws Exception {
        long count = users.count();
        String response = send(request).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andReturn().getResponse().getContentAsString();
        assertThat(users.count()).isEqualTo(count);
        if (request.password() != null) assertThat(response).doesNotContain(request.password());
    }

    static Stream<RegisterRequest> invalidRequests() {
        return Stream.of(
                new RegisterRequest(" ", "valid@example.com", PASSWORD),
                new RegisterRequest("ab", "valid@example.com", PASSWORD),
                new RegisterRequest("valid_user", "incorrecto", PASSWORD),
                new RegisterRequest("valid_user", "valid@example.com", "short"),
                new RegisterRequest("valid_user", "valid@example.com", null),
                new RegisterRequest("valid_user", "valid@example.com", "é".repeat(37))
        );
    }

    @Test
    void acceptsExactly72Utf8Bytes() throws Exception {
        String name = uniqueName();
        String password = "é".repeat(36);
        User saved = create(name, name + "@example.com", password);
        assertThat(encoder.matches(password, saved.getPasswordHash())).isTrue();
    }

    @Test
    void usesDifferentSaltsForTheSamePassword() throws Exception {
        String first = uniqueName();
        String second = uniqueName();
        User a = create(first, first + "@example.com", PASSWORD);
        User b = create(second, second + "@example.com", PASSWORD);
        assertThat(a.getPasswordHash()).isNotEqualTo(b.getPasswordHash());
        assertThat(encoder.matches(PASSWORD, b.getPasswordHash())).isTrue();
    }

    @Test
    void rejectsMalformedJsonWithoutEchoingItsContents() throws Exception {
        String response = mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                        .content("{\"password\":\"" + PASSWORD + "\","))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        assertThat(response).doesNotContain(PASSWORD);
    }

    private User create(String name, String email, String password) throws Exception {
        String body = send(new RegisterRequest(name, email, password))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return users.findById(UUID.fromString(json.readTree(body).get("id").asText())).orElseThrow();
    }

    private ResultActions send(RegisterRequest request) throws Exception {
        return mvc.perform(post("/api/auth/register").contentType(APPLICATION_JSON)
                .content(json.writeValueAsBytes(request)));
    }

    private static String uniqueName() {
        return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
