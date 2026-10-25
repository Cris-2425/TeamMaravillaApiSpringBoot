package com.teammaravilla.api.shoppinglist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ShoppingListRepository extends JpaRepository<ShoppingList, UUID> {
    List<ShoppingList> findAllByUser_IdOrderByCreatedAtDescIdAsc(UUID userId);

    Optional<ShoppingList> findByIdAndUser_Id(UUID id, UUID userId);
}
