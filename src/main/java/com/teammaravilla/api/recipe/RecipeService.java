package com.teammaravilla.api.recipe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class RecipeService {
    private final RecipeRepository recipes;

    public RecipeService(RecipeRepository recipes) {
        this.recipes = recipes;
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> findAll() {
        return recipes.findCatalog().stream().map(RecipeResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RecipeResponse findById(Integer id) {
        return recipes.findWithProductsById(id).map(RecipeResponse::from)
                .orElseThrow(RecipeNotFoundException::new);
    }
}
