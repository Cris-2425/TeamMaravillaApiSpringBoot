package com.teammaravilla.api.shoppinglist;

import com.teammaravilla.api.user.User;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.domain.Persistable;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shopping_lists")
public class ShoppingList implements Persistable<UUID> {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, updatable = false, length = 36)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Column(nullable = false, length = 120)
    private String name;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.VARCHAR)
    @Column(nullable = false, length = 6)
    private ListBackground background;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private boolean newEntity = true;

    protected ShoppingList() {
    }

    public ShoppingList(UUID id, User user, String name, ListBackground background) {
        this.id = Objects.requireNonNull(id);
        this.user = Objects.requireNonNull(user);
        this.name = Objects.requireNonNull(name);
        this.background = Objects.requireNonNull(background);
    }

    public void updateDetails(String name, ListBackground background) {
        this.name = Objects.requireNonNull(name);
        this.background = Objects.requireNonNull(background);
    }

    @PrePersist
    private void initializeTimestamps() {
        createdAt = utcNow();
        updatedAt = createdAt;
    }

    @PreUpdate
    private void updateTimestamp() {
        updatedAt = utcNow();
    }

    @PostPersist
    @PostLoad
    private void markNotNew() {
        newEntity = false;
    }

    private static LocalDateTime utcNow() {
        return LocalDateTime.now(ZoneOffset.UTC).truncatedTo(ChronoUnit.MICROS);
    }

    @Override
    public UUID getId() { return id; }

    @Override
    public boolean isNew() { return newEntity; }

    public User getUser() { return user; }
    public String getName() { return name; }
    public ListBackground getBackground() { return background; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
