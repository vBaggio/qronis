package com.qronis.modules.identity.domain.exception;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String userId) {
        super("Usuário não encontrado: " + userId);
    }
}
