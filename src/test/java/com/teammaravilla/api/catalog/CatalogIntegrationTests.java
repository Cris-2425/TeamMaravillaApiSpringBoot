package com.teammaravilla.api.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teammaravilla.api.auth.JwtService;
import com.teammaravilla.api.auth.JwtTestSupport;
import com.teammaravilla.api.product.*;
import com.teammaravilla.api.recipe.*;
import com.teammaravilla.api.user.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CatalogIntegrationTests extends JwtTestSupport {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired ProductRepository products;
    @Autowired RecipeRepository recipes;
    @Autowired UserRepository users;
    @Autowired JwtService tokens;

    @Test
    void allCatalogRoutesRequireJwt() throws Exception {
        for (String path : List.of("/api/products", "/api/recipes", "/api/recipes/1")) {
            mvc.perform(get(path)).andExpect(status().isUnauthorized());
        }
    }

    @Test
    void productsReturnOnlyPublicFieldsWithStableIdAndCategory() throws Exception {
        Product product = product(ProductCategory.FRUITS);
        JsonNode body = read("/api/products", token());
        JsonNode item = find(body, product.getId());
        assertThat(item.size()).isEqualTo(4);
        assertThat(item.get("name").asText()).isEqualTo(product.getName());
        assertThat(item.get("category").asText()).isEqualTo("FRUITS");
        assertThat(item.get("imageUrl").isNull()).isTrue();
    }

    @Test
    void recipesListAndDetailReturnOrderedProductIdsWithoutEntities() throws Exception {
        Product first = product(ProductCategory.OTHER);
        Product second = product(ProductCategory.DAIRY);
        Recipe recipe = recipe(List.of(second, first));
        String bearer = token();
        JsonNode listed = find(read("/api/recipes", bearer), recipe.getId().toString());
        JsonNode detail = read("/api/recipes/" + recipe.getId(), bearer);
        assertThat(detail).isEqualTo(listed);
        assertThat(detail.size()).isEqualTo(5);
        assertThat(detail.get("id").isIntegralNumber()).isTrue();
        assertThat(detail.get("title").asText()).isEqualTo(recipe.getTitle());
        assertThat(detail.get("instructions").asText()).isEqualTo(recipe.getInstructions());
        assertThat(detail.get("imageUrl").isNull()).isTrue();
        assertThat(detail.get("productIds")).isEqualTo(json.valueToTree(List.of(second.getId(), first.getId())));
        assertThat(detail.has("products")).isFalse();
    }

    @Test
    void recipeWithoutIngredientsReturnsEmptyArray() throws Exception {
        Recipe recipe = recipe(List.of());
        JsonNode detail = read("/api/recipes/" + recipe.getId(), token());
        assertThat(detail.get("productIds").isArray()).isTrue();
        assertThat(detail.get("productIds").size()).isZero();
    }

    @Test
    void missingRecipeReturnsSafe404() throws Exception {
        mvc.perform(get("/api/recipes/-1").header("Authorization", "Bearer " + token()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Receta no encontrada"))
                .andExpect(jsonPath("$.stackTrace").doesNotExist());
    }

    @Test
    void malformedRecipeIdReturnsSafe400() throws Exception {
        mvc.perform(get("/api/recipes/not-a-number").header("Authorization", "Bearer " + token()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El identificador de receta debe ser un número entero"));
    }

    private String token() {
        String name = "u" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        User user = users.saveAndFlush(new User(name, name + "@example.com",
                "$2a$10$9N8N35BVs5TLqGL3pspAte5OWWA2a2aZIs.EGp7At7txYakFERMue"));
        return tokens.create(user.getId());
    }

    private Product product(ProductCategory category) {
        return products.saveAndFlush(new Product("test_" + UUID.randomUUID(), "Producto de prueba", category, null));
    }

    private Recipe recipe(List<Product> ingredients) {
        int id;
        do {
            id = ThreadLocalRandom.current().nextInt(1_000_000, Integer.MAX_VALUE);
        } while (recipes.existsById(id));
        return recipes.saveAndFlush(new Recipe(id, "Receta de prueba", null, "Preparar.", ingredients));
    }

    private JsonNode read(String path, String token) throws Exception {
        return json.readTree(mvc.perform(get(path).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsByteArray());
    }

    private JsonNode find(JsonNode array, String id) {
        assertThat(array.isArray()).isTrue();
        for (JsonNode item : array) {
            if (item.get("id").asText().equals(id)) return item;
        }
        throw new AssertionError("No se encontró el elemento de prueba " + id);
    }
}
