package com.teammaravilla.api.catalog;

import com.teammaravilla.api.product.*;
import com.teammaravilla.api.recipe.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DataAccessException;
import java.sql.SQLException;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import static org.assertj.core.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.hibernate.ddl-auto=validate", showSql = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CatalogPersistenceTests {
    @Autowired ProductRepository products;
    @Autowired RecipeRepository recipes;
    @Autowired EntityManager em;
    @Autowired EntityManagerFactory factory;
    @Autowired JdbcTemplate jdbc;

    @Test
    void productKeepsExplicitIdCategoryAndNullableImage() {
        String id = "test_" + UUID.randomUUID();
        products.saveAndFlush(new Product(id, "Nombre distinto del ID", ProductCategory.DAIRY, null));
        em.clear();
        Product loaded = products.findById(id).orElseThrow();
        assertThat(loaded.getId()).isEqualTo(id);
        assertThat(loaded.getName()).isEqualTo("Nombre distinto del ID");
        assertThat(loaded.getCategory()).isEqualTo(ProductCategory.DAIRY);
        assertThat(loaded.getImageUrl()).isNull();
    }

    @Test
    void databaseRejectsUnknownCategory() {
        assertThatThrownBy(() -> jdbc.update(
                "insert into products (id, name, category) values (?, ?, ?)",
                "test_" + UUID.randomUUID(), "Producto", "INVALID"))
                .isInstanceOf(DataAccessException.class)
                .hasRootCauseInstanceOf(SQLException.class)
                .hasMessageContaining("ck_products_category");
    }

    @Test
    void recipePreservesProductOrderAfterReload() {
        Product first = product();
        Product second = product();
        int id = recipeId();
        recipes.saveAndFlush(new Recipe(id, "Receta de prueba", null, "Preparar.", List.of(second, first)));
        em.clear();
        Recipe loaded = recipes.findWithProductsById(id).orElseThrow();
        assertThat(loaded.getTitle()).isEqualTo("Receta de prueba");
        assertThat(loaded.getInstructions()).isEqualTo("Preparar.");
        assertThat(loaded.getImageUrl()).isNull();
        assertThat(loaded.getProducts()).extracting(Product::getId)
                .containsExactly(second.getId(), first.getId());
    }

    @Test
    void catalogFetchesAllIngredientsInOneQuery() {
        Product shared = product();
        recipes.saveAndFlush(new Recipe(recipeId(), "Primera", null, "Preparar.", List.of(shared)));
        recipes.saveAndFlush(new Recipe(recipeId(), "Segunda", null, "Preparar.", List.of(shared, product())));
        em.flush();
        em.clear();
        var stats = factory.unwrap(SessionFactory.class).getStatistics();
        boolean enabled = stats.isStatisticsEnabled();
        stats.setStatisticsEnabled(true);
        stats.clear();
        try {
            List<Recipe> catalog = recipes.findCatalog();
            catalog.forEach(recipe -> recipe.getProducts().forEach(Product::getName));
            assertThat(stats.getPrepareStatementCount()).isEqualTo(1);
        } finally {
            stats.setStatisticsEnabled(enabled);
        }
    }

    @Test
    void foreignKeyRejectsUnknownProduct() {
        int id = recipeId();
        recipes.saveAndFlush(new Recipe(id, "Receta", null, "Preparar.", List.of()));
        assertThatThrownBy(() -> jdbc.update(
                "insert into recipe_products (recipe_id, product_id, position) values (?, ?, 0)",
                id, "missing_" + UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void databaseRejectsDuplicateIngredient() {
        Product product = product();
        int id = recipeId();
        recipes.saveAndFlush(new Recipe(id, "Receta", null, "Preparar.", List.of(product)));
        assertThatThrownBy(() -> jdbc.update(
                "insert into recipe_products (recipe_id, product_id, position) values (?, ?, 1)",
                id, product.getId())).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletingRecipeDoesNotDeleteSharedProduct() {
        Product product = product();
        Recipe recipe = recipes.saveAndFlush(new Recipe(recipeId(), "Receta", null,
                "Preparar.", List.of(product)));
        recipes.delete(recipe);
        recipes.flush();
        em.clear();
        assertThat(products.findById(product.getId())).isPresent();
        assertThat(jdbc.queryForObject("select count(*) from recipe_products where recipe_id = ?",
                Integer.class, recipe.getId())).isZero();
    }

    private Product product() {
        return products.saveAndFlush(new Product("test_" + UUID.randomUUID(),
                "Producto", ProductCategory.OTHER, null));
    }

    private int recipeId() {
        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
        } while (recipes.existsById(id));
        return id;
    }
}
