package com.qronis.modules.identity.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.modulith.NamedInterface;
import org.springframework.web.bind.annotation.ResponseStatus;

@NamedInterface("api")
@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String email) {
        super("Já existe um usuário cadastrado com o e-mail: " + email);
    }
}
