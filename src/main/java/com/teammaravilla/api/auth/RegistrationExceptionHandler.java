package com.teammaravilla.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class RegistrationExceptionHandler {

    @ExceptionHandler(DuplicateUserException.class)
    public ProblemDetail duplicate(DuplicateUserException ex) {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                ex.getMessage()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail invalidRequest() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Username: 3-30 caracteres (letras ASCII, números, punto, guion o guion bajo); "
                        + "email válido de hasta 254 caracteres; contraseña: 12-72 caracteres "
                        + "y máximo 72 bytes UTF-8. Los campos son obligatorios."
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail unreadableRequest() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "El cuerpo debe ser un objeto JSON válido con username, email y password"
        );
    }
}