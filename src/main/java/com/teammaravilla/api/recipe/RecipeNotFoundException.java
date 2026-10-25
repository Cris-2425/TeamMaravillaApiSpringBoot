package com.teammaravilla.api.recipe;

public class RecipeNotFoundException extends RuntimeException {
    public RecipeNotFoundException() {
        super("Receta no encontrada");
    }
}
