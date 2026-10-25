package com.teammaravilla.api.recipe;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {
    private final RecipeService recipes;

    public RecipeController(RecipeService recipes) {
        this.recipes = recipes;
    }

    @GetMapping
    public List<RecipeResponse> findAll() {
        return recipes.findAll();
    }

    @GetMapping("/{id}")
    public RecipeResponse findById(@PathVariable Integer id) {
        return recipes.findById(id);
    }
}
