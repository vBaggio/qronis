package com.qronis.modules.tracker.api.exception;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public class InvalidTimeBoundsException extends RuntimeException {
    public InvalidTimeBoundsException() {
        super("Horário de término deve ser posterior ao de início");
    }
}
