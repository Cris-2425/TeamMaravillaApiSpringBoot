package com.teammaravilla.api.auth;

import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RegistrationService {

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository users,
            PasswordEncoder passwordEncoder
    ) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        if (users.existsByUsername(request.username())) {
            throw new DuplicateUserException(
                    "El nombre de usuario ya existe"
            );
        }

        if (users.existsByEmail(request.email())) {
            throw new DuplicateUserException(
                    "El email ya está registrado"
            );
        }

        String passwordHash =
                passwordEncoder.encode(request.password());

        User user = new User(
                request.username(),
                request.email(),
                passwordHash
        );

        try {
            users.saveAndFlush(user);

        } catch (DataIntegrityViolationException ex) {

            if (isUserUniqueViolation(ex)) {
                throw new DuplicateUserException(
                        "El nombre de usuario o el email ya existe"
                );
            }

            throw ex;
        }

        return new RegisterResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }

    private boolean isUserUniqueViolation(Throwable error) {

        for (Throwable cause = error;
             cause != null;
             cause = cause.getCause()) {

            if (cause instanceof ConstraintViolationException violation) {

                String name = violation.getConstraintName();

                if (name == null) {
                    return false;
                }

                name = name.substring(name.lastIndexOf('.') + 1);

                return name.equals("uk_users_username")
                        || name.equals("uk_users_email");
            }
        }

        return false;
    }
}