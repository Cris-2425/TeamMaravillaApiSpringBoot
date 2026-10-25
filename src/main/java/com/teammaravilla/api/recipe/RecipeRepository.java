package com.teammaravilla.api.recipe;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository extends JpaRepository<Recipe, Integer> {
    @Query("select distinct r from Recipe r left join fetch r.products order by r.id")
    List<Recipe> findCatalog();

    @Query("select r from Recipe r left join fetch r.products where r.id = :id")
    Optional<Recipe> findWithProductsById(@Param("id") Integer id);
}
