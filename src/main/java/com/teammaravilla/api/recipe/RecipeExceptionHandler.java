package com.teammaravilla.api.recipe;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = RecipeController.class)
public class RecipeExceptionHandler {
    @ExceptionHandler(RecipeNotFoundException.class)
    public ProblemDetail notFound() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Receta no encontrada");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail invalidId() {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "El identificador de receta debe ser un número entero");
    }
}
