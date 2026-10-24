package com.teammaravilla.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice(assignableTypes = {
        LoginController.class,
        CurrentUserController.class
})
public class AuthenticationExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail invalidCredentials() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Credenciales incorrectas"
        );
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HttpMessageNotReadableException.class
    })
    public ProblemDetail invalidRequest() {
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Solicitud de login inválida"
        );
    }
}