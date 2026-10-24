package com.teammaravilla.api.auth;

import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTests {
    @Mock UserRepository users;
    @Mock PasswordEncoder encoder;
    RegistrationService service;

    @BeforeEach
    void setUp() {
        service = new RegistrationService(users, encoder);
    }

    @ParameterizedTest
    @ValueSource(strings = {"users.uk_users_username", "uk_users_email"})
    void translatesUniqueViolationAfterPrechecks(String constraint) {
        when(encoder.encode("SoloPruebas-123456")).thenReturn("hash-simulado");
        var sql = new SQLException("Duplicate", "23000", 1062);
        var violation = new ConstraintViolationException("Duplicate", sql, constraint);
        when(users.saveAndFlush(any(User.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate", violation));

        assertThatThrownBy(() -> service.register(request()))
                .isInstanceOf(DuplicateUserException.class);
    }

    @Test
    void doesNotDisguiseOtherIntegrityFailuresAsDuplicates() {
        when(encoder.encode("SoloPruebas-123456")).thenReturn("hash-simulado");
        var failure = new DataIntegrityViolationException("Other constraint");
        when(users.saveAndFlush(any(User.class))).thenThrow(failure);

        assertThatThrownBy(() -> service.register(request())).isSameAs(failure);
    }

    private RegisterRequest request() {
        return new RegisterRequest("test_user", "test@example.com", "SoloPruebas-123456");
    }
}
