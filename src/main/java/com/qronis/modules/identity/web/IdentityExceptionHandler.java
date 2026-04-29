package com.qronis.modules.identity.web;

import com.qronis.modules.identity.domain.exception.UserNotFoundException;
import com.qronis.shared.exception.ErrorResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.qronis.modules.identity")
public class IdentityExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handle(UserNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(ErrorResponseDTO.of(404, "USER_NOT_FOUND", ex.getMessage()));
    }
}
