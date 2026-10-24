package com.teammaravilla.api.auth;

import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class AuthenticationService {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String dummyHash;

    public AuthenticationService(UserRepository users, PasswordEncoder passwords) {
        this.users = users;
        this.passwords = passwords;
        this.dummyHash = passwords.encode(UUID.randomUUID().toString());
    }

    @Transactional(readOnly = true)
    public UserResponse authenticate(LoginRequest request) {
        User user = users.findByUsername(request.username()).orElse(null);
        boolean matches = passwords.matches(request.password(),
                user == null ? dummyHash : user.getPasswordHash());
        if (user == null || !matches) throw new InvalidCredentialsException();
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public UserResponse currentUser(UUID authenticatedUserId) {
        return users.findById(authenticatedUserId).map(UserResponse::from)
                .orElseThrow(InvalidCredentialsException::new);
    }
}
