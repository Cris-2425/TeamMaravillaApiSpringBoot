package com.teammaravilla.api.catalog;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.teammaravilla.api.auth.JwtTestSupport;
import com.teammaravilla.api.product.ProductService;
import com.teammaravilla.api.recipe.RecipeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CatalogSeedIntegrationTests extends JwtTestSupport {
    @Autowired ProductService products;
    @Autowired RecipeService recipes;
    @Autowired ObjectMapper json;

    @Test
    void flywayMatchesAndroidCatalogAndDtosWorkOutsideServiceTransactions() throws Exception {
        try (var input = getClass().getResourceAsStream("/catalog-seed.json")) {
            assertThat(input).isNotNull();
            JsonNode expected = json.readTree(input);
            var productDtos = products.findAll();
            var recipeDtos = recipes.findAll();
            assertThat(json.<JsonNode>valueToTree(productDtos)).isEqualTo(expected.get("products"));
            assertThat(json.<JsonNode>valueToTree(recipeDtos)).isEqualTo(expected.get("recipes"));
            assertThat(json.<JsonNode>valueToTree(recipes.findById(1))).isEqualTo(expected.get("recipes").get(0));
        }
    }
}
