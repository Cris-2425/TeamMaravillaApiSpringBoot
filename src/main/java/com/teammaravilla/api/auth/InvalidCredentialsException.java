package com.teammaravilla.api.auth;

public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException() {
        super("Credenciales incorrectas");
    }
}
