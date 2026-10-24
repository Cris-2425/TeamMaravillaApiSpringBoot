package com.teammaravilla.api.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {
    private final AuthenticationService authentication;
    private final JwtService tokens;

    public LoginController(AuthenticationService authentication, JwtService tokens) {
        this.authentication = authentication;
        this.tokens = tokens;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = authentication.authenticate(request);
        LoginResponse response = new LoginResponse(tokens.create(user.id()), "Bearer", tokens.expiresIn(), user);
        return ResponseEntity.ok().header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header(HttpHeaders.PRAGMA, "no-cache").body(response);
    }
}
