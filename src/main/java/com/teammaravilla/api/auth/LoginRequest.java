package com.teammaravilla.api.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public record LoginRequest(
        @NotBlank @Size(min = 3, max = 30) @Pattern(regexp = "[a-z0-9._-]+") String username,
        @NotBlank @Size(max = 72) String password
) {
    public LoginRequest {
        username = username == null ? null : username.strip().toLowerCase(Locale.ROOT);
    }

    @JsonIgnore
    @AssertTrue(message = "Contraseña demasiado larga")
    public boolean isPasswordWithinBcryptLimit() {
        return password == null || password.getBytes(StandardCharsets.UTF_8).length <= 72;
    }

    @Override
    public String toString() { return "LoginRequest[REDACTED]"; }
}
