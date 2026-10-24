package com.teammaravilla.api.auth;

import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class CurrentUserController {

    private final AuthenticationService authentication;

    public CurrentUserController(AuthenticationService authentication) {
        this.authentication = authentication;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> me(
            @AuthenticationPrincipal UUID userId
    ) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(authentication.currentUser(userId));
    }
}