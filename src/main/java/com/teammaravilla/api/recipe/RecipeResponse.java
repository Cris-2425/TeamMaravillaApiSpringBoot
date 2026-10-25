package com.teammaravilla.api.recipe;

import com.teammaravilla.api.product.Product;
import java.util.List;

public record RecipeResponse(Integer id, String title, String imageUrl,
                             List<String> productIds, String instructions) {
    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(recipe.getId(), recipe.getTitle(), recipe.getImageUrl(),
                recipe.getProducts().stream().map(Product::getId).toList(), recipe.getInstructions());
    }
}
