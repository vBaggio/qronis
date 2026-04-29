package com.qronis.modules.tracker.api.exception;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public class TimeEntryNotFoundException extends RuntimeException {
    public TimeEntryNotFoundException() {
        super("Lançamento não encontrado");
    }

    public TimeEntryNotFoundException(String message) {
        super(message);
    }
}
