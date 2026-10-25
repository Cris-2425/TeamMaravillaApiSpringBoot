package com.teammaravilla.api.shoppinglist;

import com.teammaravilla.api.user.User;
import com.teammaravilla.api.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate", showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ShoppingListRepositoryTests {
    private static final String HASH =
            "$2a$10$9N8N35BVs5TLqGL3pspAte5OWWA2a2aZIs.EGp7At7txYakFERMue";

    @Autowired ShoppingListRepository lists;
    @Autowired UserRepository users;
    @Autowired EntityManager em;
    @Autowired JdbcTemplate jdbc;

    @Test
    void preservesClientIdOwnerBackgroundAndCreationTime() {
        User owner = user();
        UUID clientId = UUID.randomUUID();
        ShoppingList list = new ShoppingList(clientId, owner, "Compra semanal", ListBackground.FONDO2);
        assertThat(list.isNew()).isTrue();
        lists.saveAndFlush(list);
        em.clear();

        ShoppingList loaded = lists.findByIdAndUser_Id(clientId, owner.getId()).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(clientId);
        assertThat(loaded.getUser().getId()).isEqualTo(owner.getId());
        assertThat(loaded.getName()).isEqualTo("Compra semanal");
        assertThat(loaded.getBackground()).isEqualTo(ListBackground.FONDO2);
        assertThat(loaded.isNew()).isFalse();
        assertThat(loaded.getCreatedAt()).isNotNull().isEqualTo(loaded.getUpdatedAt());
    }

    @Test
    void ownerQueriesExcludeOtherUsersLists() {
        User owner = user();
        User other = user();
        ShoppingList first = lists.saveAndFlush(new ShoppingList(UUID.randomUUID(), owner,
                "Primera", ListBackground.FONDO1));
        ShoppingList second = lists.saveAndFlush(new ShoppingList(UUID.randomUUID(), other,
                "Segunda", ListBackground.FONDO4));
        em.clear();

        assertThat(lists.findAllByUser_IdOrderByCreatedAtDescIdAsc(owner.getId()))
                .extracting(ShoppingList::getId).containsExactly(first.getId());
        assertThat(lists.findAllByUser_IdOrderByCreatedAtDescIdAsc(other.getId()))
                .extracting(ShoppingList::getId).containsExactly(second.getId());
        assertThat(lists.findByIdAndUser_Id(first.getId(), other.getId())).isEmpty();
        assertThat(lists.findByIdAndUser_Id(UUID.randomUUID(), owner.getId())).isEmpty();
    }

    @Test
    void newEntityWithExistingIdIsRejectedInsteadOfOverwritingIt() {
        User owner = user();
        User other = user();
        UUID id = UUID.randomUUID();
        lists.saveAndFlush(new ShoppingList(id, owner, "Original", ListBackground.FONDO1));
        em.clear();

        ShoppingList duplicate = new ShoppingList(id, em.getReference(User.class, other.getId()),
                "No debe sustituirla", ListBackground.FONDO2);
        assertThatThrownBy(() -> lists.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void updatePreservesCreationTimeAndAdvancesUpdatedAt() {
        User owner = user();
        ShoppingList list = lists.saveAndFlush(new ShoppingList(UUID.randomUUID(), owner,
                "Antes", ListBackground.FONDO1));
        LocalDateTime createdAt = list.getCreatedAt();
        jdbc.update("update shopping_lists set updated_at = '2000-01-01 00:00:00' where id = ?",
                list.getId().toString());
        em.clear();
        ShoppingList loaded = lists.findByIdAndUser_Id(list.getId(), owner.getId()).orElseThrow();
        LocalDateTime previousUpdate = loaded.getUpdatedAt();
        loaded.updateDetails("Después", ListBackground.FONDO3);
        lists.flush();
        em.clear();

        ShoppingList updated = lists.findByIdAndUser_Id(list.getId(), owner.getId()).orElseThrow();
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isAfter(previousUpdate);
        assertThat(updated.getName()).isEqualTo("Después");
        assertThat(updated.getBackground()).isEqualTo(ListBackground.FONDO3);
    }

    @Test
    void databaseRejectsMissingOwner() {
        assertThatThrownBy(() -> insertRaw(UUID.randomUUID(), "Compra", "FONDO1"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsUnknownBackground() {
        User owner = user();
        assertThatThrownBy(() -> insertRaw(owner.getId(), "Compra", "FONDO5"))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseInstanceOf(SQLException.class)
                .hasMessageContaining("ck_shopping_lists_background");
    }

    @Test
    void databaseRejectsBlankName() {
        User owner = user();
        assertThatThrownBy(() -> insertRaw(owner.getId(), "   ", "FONDO1"))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseInstanceOf(SQLException.class)
                .hasMessageContaining("ck_shopping_lists_name");
    }

    @Test
    void deletingListDoesNotDeleteOwner() {
        User owner = user();
        ShoppingList list = lists.saveAndFlush(new ShoppingList(UUID.randomUUID(), owner,
                "Temporal", ListBackground.FONDO1));
        lists.delete(list);
        lists.flush();
        em.clear();
        assertThat(lists.findByIdAndUser_Id(list.getId(), owner.getId())).isEmpty();
        assertThat(users.findById(owner.getId())).isPresent();
    }

    private User user() {
        String name = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        return users.saveAndFlush(new User(name, name + "@example.com", HASH));
    }

    private void insertRaw(UUID ownerId, String name, String background) {
        jdbc.update("""
                insert into shopping_lists (id, user_id, name, background, created_at, updated_at)
                values (?, ?, ?, ?, UTC_TIMESTAMP(6), UTC_TIMESTAMP(6))
                """, UUID.randomUUID().toString(), ownerId.toString(), name, background);
    }
}
