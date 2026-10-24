package com.teammaravilla.api.auth;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    private final AuthenticationService authentication;

    public LoginController(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    @PostMapping("/login")
    public UserResponse login(@Valid @RequestBody LoginRequest request) {
        return authentication.authenticate(request);
    }
}