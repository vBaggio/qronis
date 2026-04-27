package com.qronis.modules.tracker.domain.exception;

public class InvalidTimeBoundsException extends RuntimeException {
    public InvalidTimeBoundsException() {
        super("Horário de término deve ser posterior ao de início");
    }
}
