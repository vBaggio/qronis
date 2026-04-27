package com.qronis.modules.tracker.domain.exception;

public class ActiveTimerConflictException extends RuntimeException {
    public ActiveTimerConflictException() {
        super("Já existe um timer ativo. Pare o timer atual antes de iniciar outro.");
    }
}
