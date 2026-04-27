package com.qronis.modules.tracker.domain.exception;

public class TimeEntryNotFoundException extends RuntimeException {
    public TimeEntryNotFoundException() {
        super("Lançamento não encontrado");
    }

    public TimeEntryNotFoundException(String message) {
        super(message);
    }
}
