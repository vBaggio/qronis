package com.qronis.modules.tracker.api.exception;

import org.springframework.modulith.NamedInterface;

@NamedInterface("api")
public class ActiveTimerConflictException extends RuntimeException {
    public ActiveTimerConflictException() {
        super("Já existe um timer ativo. Pare o timer atual antes de iniciar outro.");
    }
}
