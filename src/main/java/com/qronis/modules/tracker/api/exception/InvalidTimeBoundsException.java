package com.qronis.modules.tracker.api.exception;

public class InvalidTimeBoundsException extends RuntimeException {
    public InvalidTimeBoundsException() {
        super("Horário de término deve ser posterior ao de início");
    }
}
