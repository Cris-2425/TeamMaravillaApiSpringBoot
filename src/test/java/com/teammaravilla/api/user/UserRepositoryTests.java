package com.teammaravilla.api.user;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate", showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTests {
    private static final String HASH =
            "$2a$10$9N8N35BVs5TLqGL3pspAte5OWWA2a2aZIs.EGp7At7txYakFERMue";

    @Autowired UserRepository users;
    @Autowired EntityManager entityManager;

    @Test
    void persistsAndReloadsUser() {
        String name = uniqueName();
        User saved = users.saveAndFlush(new User(name, name + "@example.com", HASH));
        UUID id = saved.getId();
        entityManager.clear();

        User loaded = users.findById(id).orElseThrow();
        assertThat(loaded.getUsername()).isEqualTo(name);
        assertThat(loaded.getPasswordHash()).isEqualTo(HASH);
        assertThat(loaded.getCreatedAt()).isNotNull();
        assertThat(users.existsByEmail(name + "@example.com")).isTrue();
    }

    @Test
    void databaseRejectsDuplicateUsernameIgnoringCase() {
        String name = uniqueName();
        users.saveAndFlush(new User(name, name + "@example.com", HASH));
        assertThatThrownBy(() -> users.saveAndFlush(
                new User(name.toUpperCase(java.util.Locale.ROOT), uniqueName() + "@example.com", HASH)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDuplicateEmailIgnoringCase() {
        String email = uniqueName() + "@example.com";
        users.saveAndFlush(new User(uniqueName(), email, HASH));
        assertThatThrownBy(() -> users.saveAndFlush(
                new User(uniqueName(), email.toUpperCase(java.util.Locale.ROOT), HASH)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private static String uniqueName() {
        return "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    }
}
